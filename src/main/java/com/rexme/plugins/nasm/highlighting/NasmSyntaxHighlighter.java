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

package com.rexme.plugins.nasm.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.rexme.plugins.nasm.lexer.NasmLexer;
import com.rexme.plugins.nasm.psi.NasmTokenTypes;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public final class NasmSyntaxHighlighter extends SyntaxHighlighterBase {

    // ── Attribute keys ────────────────────────────────────────────────────────

    public static final TextAttributesKey COMMENT =
            createTextAttributesKey("NASM_COMMENT",
                    DefaultLanguageHighlighterColors.LINE_COMMENT);

    public static final TextAttributesKey LABEL_DEFINITION =
            createTextAttributesKey("NASM_LABEL_DEF",
                    DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);

    public static final TextAttributesKey LABEL_REFERENCE =
            createTextAttributesKey("NASM_LABEL_REF",
                    DefaultLanguageHighlighterColors.NUMBER);

    public static final TextAttributesKey INSTRUCTION =
            createTextAttributesKey("NASM_INSTRUCTION",
                    DefaultLanguageHighlighterColors.KEYWORD);

    public static final TextAttributesKey REGISTER =
            createTextAttributesKey("NASM_REGISTER",
                    DefaultLanguageHighlighterColors.LOCAL_VARIABLE);

    /**
     * byte / word / dword / qword / tword / oword / yword / zword
     * Default: uses the KEYWORD fallback colour; the bundled colour-scheme
     * files (NasmDefault.xml / NasmDarcula.xml) override this to yellow.
     */
    public static final TextAttributesKey SIZE_SPECIFIER =
            createTextAttributesKey("NASM_SIZE_SPECIFIER",
                    DefaultLanguageHighlighterColors.KEYWORD);

    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("NASM_KEYWORD",
                    DefaultLanguageHighlighterColors.KEYWORD);

    public static final TextAttributesKey NUMBER =
            createTextAttributesKey("NASM_NUMBER",
                    DefaultLanguageHighlighterColors.NUMBER);

    public static final TextAttributesKey STRING =
            createTextAttributesKey("NASM_STRING",
                    DefaultLanguageHighlighterColors.STRING);

    public static final TextAttributesKey SECTION_NAME =
            createTextAttributesKey("NASM_SECTION_NAME",
                    DefaultLanguageHighlighterColors.CLASS_NAME);

    public static final TextAttributesKey PREPROC =
            createTextAttributesKey("NASM_PREPROC",
                    DefaultLanguageHighlighterColors.METADATA);

    public static final TextAttributesKey OPERATOR =
            createTextAttributesKey("NASM_OPERATOR",
                    DefaultLanguageHighlighterColors.OPERATION_SIGN);

    public static final TextAttributesKey BAD_CHARACTER =
            createTextAttributesKey("NASM_BAD_CHARACTER",
                    HighlighterColors.BAD_CHARACTER);

    public static final TextAttributesKey REL_ABS =
            createTextAttributesKey("NASM_ABS_REL",
                    DefaultLanguageHighlighterColors.KEYWORD);

    // ── Empty arrays for readability ──────────────────────────────────────────
    private static final TextAttributesKey[] EMPTY  = TextAttributesKey.EMPTY_ARRAY;
    private static final TextAttributesKey[] CMT    = {COMMENT};
    private static final TextAttributesKey[] INSTR  = {INSTRUCTION};
    private static final TextAttributesKey[] REG    = {REGISTER};
    private static final TextAttributesKey[] SZ     = {SIZE_SPECIFIER};
    private static final TextAttributesKey[] KW     = {KEYWORD};
    private static final TextAttributesKey[] NUM    = {NUMBER};
    private static final TextAttributesKey[] STR    = {STRING};
    private static final TextAttributesKey[] PP     = {PREPROC};
    private static final TextAttributesKey[] OP     = {OPERATOR};
    private static final TextAttributesKey[] BAD    = {BAD_CHARACTER};

    // ── SyntaxHighlighterBase ─────────────────────────────────────────────────

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new NasmLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {

        if (tokenType == NasmTokenTypes.COMMENT)             return CMT;
        if (tokenType == NasmTokenTypes.INSTRUCTION)         return INSTR;
        if (tokenType == NasmTokenTypes.REGISTER)            return REG;
        if (tokenType == NasmTokenTypes.SIZE_SPECIFIER)      return SZ;
        if (tokenType == NasmTokenTypes.NUMBER)              return NUM;
        if (tokenType == NasmTokenTypes.STRING)              return STR;
        if (tokenType == NasmTokenTypes.PREPROC_DIRECTIVE)   return PP;
        if (tokenType == NasmTokenTypes.BAD_CHARACTER)       return BAD;
        if (tokenType == NasmTokenTypes.REL_KW || tokenType == NasmTokenTypes.ABS_KW) return new TextAttributesKey[] {REL_ABS};

        // Operators & punctuation
        if (tokenType == NasmTokenTypes.PLUS    || tokenType == NasmTokenTypes.MINUS
         || tokenType == NasmTokenTypes.STAR    || tokenType == NasmTokenTypes.SLASH
         || tokenType == NasmTokenTypes.COLON   || tokenType == NasmTokenTypes.COMMA
         || tokenType == NasmTokenTypes.AMP     || tokenType == NasmTokenTypes.PIPE
         || tokenType == NasmTokenTypes.CARET   || tokenType == NasmTokenTypes.TILDE
         || tokenType == NasmTokenTypes.SHL     || tokenType == NasmTokenTypes.SHR
         || tokenType == NasmTokenTypes.LBRACKET|| tokenType == NasmTokenTypes.RBRACKET
         || tokenType == NasmTokenTypes.DOLLAR  || tokenType == NasmTokenTypes.DOLLAR_DOLLAR)
            return OP;

        // All directive/declaration keywords
        if (NasmTokenTypes.ALL_KEYWORDS.contains(tokenType)) return KW;

        // Identifiers: label defs / refs get semantic colouring via the
        // NasmAnnotator — at the token level they are not yet distinguishable.
        return EMPTY;
    }
}
