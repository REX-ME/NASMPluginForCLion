/*
 * Copyright (C) 2026 REX-ME
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://gnu.org>.
 */

package com.rexme.plugins.nasm.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import com.rexme.plugins.nasm.psi.NasmElementTypes;
import com.rexme.plugins.nasm.psi.NasmTokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Hand-written recursive-descent parser for NASM assembly.
 *
 * <pre>
 * file      ::= statement*
 * statement ::= ( label_def? body? comment? ) NEWLINE
 *             | PREPROC_DIRECTIVE rest-of-line NEWLINE
 *             | EOF
 *
 * label_def ::= IDENTIFIER COLON              (explicit colon — any label)
 *             | IDENTIFIER db/dw/dd/dq/res…   (data/bss label, no colon)
 *             | IDENTIFIER equ expr            (constant, no colon)
 *
 * Local labels start with '.' (e.g. ".loop:"). Their NASM-canonical
 * full name is currentGlobalLabel + ".loop" — resolution happens in
 * NasmUtil, not here.
 * </pre>
 */
public final class NasmParser implements PsiParser, LightPsiParser {

    @Override
    public @NotNull ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder builder) {
        parseLight(root, builder);
        return builder.getTreeBuilt();
    }

    @Override
    public void parseLight(IElementType root, PsiBuilder builder) {
        builder.setDebugMode(false);
        Marker file = builder.mark();
        while (!builder.eof()) {
            parseStatement(builder);
        }
        file.done(root);
    }

    // ── Statement ─────────────────────────────────────────────────────────────

    private void parseStatement(PsiBuilder b) {
        IElementType tt = b.getTokenType();

        if (tt == NasmTokenTypes.NEWLINE) { b.advanceLexer(); return; }

        Marker stmt = b.mark();

        // preprocessor line  (%define, %macro, %include …)
        if (tt == NasmTokenTypes.PREPROC_DIRECTIVE) {
            consumeToEndOfLine(b);
            eatComment(b);
            eatNewline(b);
            stmt.done(NasmElementTypes.PREPROC_STMT);
            return;
        }

        // ── Optional label at start of line ───────────────────────────────────
        // Case A:  name:        explicit colon
        // Case B:  name db/…    data label without colon
        // Case C:  name equ     constant without colon
        if (tt == NasmTokenTypes.IDENTIFIER) {
            IElementType next = peekNext(b);
            if (next == NasmTokenTypes.COLON) {
                parseLabelDef(b);         // consumes IDENTIFIER + COLON
            } else if (isNoColonLabelKw(next)) {
                parseLabelDefNoColon(b);  // consumes IDENTIFIER only
            }
        }

        tt = b.getTokenType();

        if (tt == NasmTokenTypes.SECTION_KW) {
            parseSectionDecl(b, stmt); return;
        }
        if (tt == NasmTokenTypes.GLOBAL_KW
         || tt == NasmTokenTypes.EXTERN_KW
         || tt == NasmTokenTypes.COMMON_KW) {
            parseExportDecl(b, stmt); return;
        }
        if (NasmTokenTypes.DATA_KEYWORDS.contains(tt)) {
            parseDataDecl(b, stmt); return;
        }
        if (NasmTokenTypes.RES_KEYWORDS.contains(tt)) {
            parseResDecl(b, stmt); return;
        }
        if (tt == NasmTokenTypes.EQU_KW) {
            parseEquDecl(b, stmt); return;
        }
        if (tt == NasmTokenTypes.INSTRUCTION || tt == NasmTokenTypes.TIMES_KW) {
            parseInstructionStmt(b, stmt); return;
        }

        if (tt != null && tt != NasmTokenTypes.NEWLINE && tt != NasmTokenTypes.COMMENT) {
            if (tt == NasmTokenTypes.IDENTIFIER) {
                Marker ref = b.mark();
                b.advanceLexer();
                ref.done(NasmElementTypes.LABEL_REF);
            }
            consumeToEndOfLine(b);
        }
        eatComment(b);
        eatNewline(b);
        stmt.done(NasmElementTypes.MISC_STMT);
    }

    // ── Label definitions ─────────────────────────────────────────────────────

    private void parseLabelDef(PsiBuilder b) {
        Marker m = b.mark();
        b.advanceLexer(); // IDENTIFIER
        b.advanceLexer(); // COLON
        m.done(NasmElementTypes.LABEL_DEF);
    }

    private void parseLabelDefNoColon(PsiBuilder b) {
        Marker m = b.mark();
        b.advanceLexer(); // IDENTIFIER only — data keyword follows separately
        m.done(NasmElementTypes.LABEL_DEF);
    }

    // ── Section ───────────────────────────────────────────────────────────────

    private void parseSectionDecl(PsiBuilder b, Marker stmt) {
        b.advanceLexer();
        consumeToEndOfLine(b);
        eatComment(b);
        eatNewline(b);
        stmt.done(NasmElementTypes.SECTION_DECL);
    }

    // ── global / extern / common ──────────────────────────────────────────────

    private void parseExportDecl(PsiBuilder b, Marker stmt) {
        b.advanceLexer();
        while (b.getTokenType() == NasmTokenTypes.IDENTIFIER
            || b.getTokenType() == NasmTokenTypes.COMMA) {
            if (b.getTokenType() == NasmTokenTypes.IDENTIFIER) {
                Marker ref = b.mark();
                b.advanceLexer();
                ref.done(NasmElementTypes.LABEL_REF);
            } else {
                b.advanceLexer();
            }
        }
        eatComment(b);
        eatNewline(b);
        stmt.done(NasmElementTypes.EXPORT_DECL);
    }

    // ── Data / reserve / equ ─────────────────────────────────────────────────

    private void parseDataDecl(PsiBuilder b, Marker stmt) {
        b.advanceLexer();
        parseOperandList(b);
        eatComment(b);
        eatNewline(b);
        stmt.done(NasmElementTypes.DATA_DECL);
    }

    private void parseResDecl(PsiBuilder b, Marker stmt) {
        b.advanceLexer();
        parseOperand(b);
        eatComment(b);
        eatNewline(b);
        stmt.done(NasmElementTypes.RES_DECL);
    }

    private void parseEquDecl(PsiBuilder b, Marker stmt) {
        b.advanceLexer();
        parseOperand(b);
        eatComment(b);
        eatNewline(b);
        stmt.done(NasmElementTypes.EQU_DECL);
    }

    // ── Instruction ───────────────────────────────────────────────────────────

    private void parseInstructionStmt(PsiBuilder b, Marker stmt) {
        if (b.getTokenType() == NasmTokenTypes.TIMES_KW) {
            b.advanceLexer();
            parseOperand(b); // TIMES count (this OPERAND child is counted separately)
        }
        // primary mnemonic
        if (b.getTokenType() == NasmTokenTypes.INSTRUCTION) b.advanceLexer();
        // rep / lock / … prefix(es) before actual mnemonic
        while (b.getTokenType() == NasmTokenTypes.INSTRUCTION) b.advanceLexer();

        if (!isEndOfLine(b.getTokenType())) {
            parseOperandList(b);
        }
        eatComment(b);
        eatNewline(b);
        stmt.done(NasmElementTypes.INSTRUCTION_STMT);
    }

    // ── Operands ─────────────────────────────────────────────────────────────

    private void parseOperandList(PsiBuilder b) {
        parseOperand(b);
        while (b.getTokenType() == NasmTokenTypes.COMMA) {
            b.advanceLexer();
            parseOperand(b);
        }
    }

    private void parseOperand(PsiBuilder b) {
        Marker op = b.mark();
        while (b.getTokenType() == NasmTokenTypes.SIZE_SPECIFIER) b.advanceLexer();

        if (b.getTokenType() == NasmTokenTypes.LBRACKET) {
            Marker mem = b.mark();
            b.advanceLexer();
            parseMemoryExpr(b);
            if (b.getTokenType() == NasmTokenTypes.RBRACKET) b.advanceLexer();
            mem.done(NasmElementTypes.MEMORY_OPERAND);
            op.done(NasmElementTypes.OPERAND);
            return;
        }
        if (!isEndOfLine(b.getTokenType()) && b.getTokenType() != NasmTokenTypes.COMMA) {
            parseExpr(b);
        }
        op.done(NasmElementTypes.OPERAND);
    }

    private void parseMemoryExpr(PsiBuilder b) {
        while (b.getTokenType() != null
            && b.getTokenType() != NasmTokenTypes.RBRACKET
            && !isEndOfLine(b.getTokenType())) {
            parseAtom(b);
            IElementType tt = b.getTokenType();
            if (tt == NasmTokenTypes.PLUS || tt == NasmTokenTypes.MINUS
             || tt == NasmTokenTypes.STAR || tt == NasmTokenTypes.COLON) {
                b.advanceLexer();
            } else break;
        }
    }

    private void parseExpr(PsiBuilder b) {
        parseAtom(b);
        while (true) {
            IElementType tt = b.getTokenType();
            if (tt == NasmTokenTypes.PLUS   || tt == NasmTokenTypes.MINUS
             || tt == NasmTokenTypes.STAR   || tt == NasmTokenTypes.SLASH
             || tt == NasmTokenTypes.AMP    || tt == NasmTokenTypes.PIPE
             || tt == NasmTokenTypes.CARET  || tt == NasmTokenTypes.SHL
             || tt == NasmTokenTypes.SHR    || tt == NasmTokenTypes.PERCENT) {
                b.advanceLexer();
                parseAtom(b);
            } else break;
        }
    }

    /**
     * Atom — the lowest-level token in an expression.
     * Bare identifiers (not registers/size-specs) become LABEL_REF nodes.
     * This includes dot-local refs like {@code .loop} and fully qualified
     * ones like {@code funcA.loop} (which the lexer already scans as one token).
     */
    private void parseAtom(PsiBuilder b) {
        IElementType tt = b.getTokenType();
        if (tt == null || isEndOfLine(tt)
         || tt == NasmTokenTypes.COMMA
         || tt == NasmTokenTypes.RBRACKET
         || tt == NasmTokenTypes.RPAREN) return;

        if (tt == NasmTokenTypes.SIZE_SPECIFIER || tt == NasmTokenTypes.REL_KW || tt == NasmTokenTypes.ABS_KW) {
            b.advanceLexer();
            parseAtom(b); // consume what follows (e.g. "something" after "rel")
            return;
        }

        if (tt == NasmTokenTypes.REGISTER
         || tt == NasmTokenTypes.NUMBER
         || tt == NasmTokenTypes.STRING
         || tt == NasmTokenTypes.DOLLAR
         || tt == NasmTokenTypes.DOLLAR_DOLLAR) {
            b.advanceLexer(); return;
        }
        if (tt == NasmTokenTypes.MINUS || tt == NasmTokenTypes.TILDE
         || tt == NasmTokenTypes.BANG) {
            b.advanceLexer(); parseAtom(b); return;
        }
        if (tt == NasmTokenTypes.LPAREN) {
            b.advanceLexer(); parseExpr(b);
            if (b.getTokenType() == NasmTokenTypes.RPAREN) b.advanceLexer();
            return;
        }
        if (tt == NasmTokenTypes.IDENTIFIER) {
            Marker ref = b.mark();
            b.advanceLexer();
            ref.done(NasmElementTypes.LABEL_REF);
            return;
        }
        b.advanceLexer(); // consume unknown token to avoid infinite loop
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isNoColonLabelKw(IElementType t) {
        return NasmTokenTypes.DATA_KEYWORDS.contains(t)
            || NasmTokenTypes.RES_KEYWORDS.contains(t)
            || t == NasmTokenTypes.EQU_KW;
    }

    private IElementType peekNext(PsiBuilder b) { return b.lookAhead(1); }

    private void consumeToEndOfLine(PsiBuilder b) {
        while (b.getTokenType() != null
            && b.getTokenType() != NasmTokenTypes.NEWLINE
            && b.getTokenType() != NasmTokenTypes.COMMENT) {
            b.advanceLexer();
        }
    }

    private boolean isEndOfLine(IElementType tt) {
        return tt == NasmTokenTypes.NEWLINE || tt == NasmTokenTypes.COMMENT || tt == null;
    }

    private void eatComment(PsiBuilder b) {
        if (b.getTokenType() == NasmTokenTypes.COMMENT) b.advanceLexer();
    }

    private void eatNewline(PsiBuilder b) {
        if (b.getTokenType() == NasmTokenTypes.NEWLINE) b.advanceLexer();
    }
}
