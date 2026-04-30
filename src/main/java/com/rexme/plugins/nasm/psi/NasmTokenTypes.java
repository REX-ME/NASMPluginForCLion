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

package com.rexme.plugins.nasm.psi;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

public interface NasmTokenTypes {

    // ── Structural ──────────────────────────────────────────────────────────────
    IElementType WHITE_SPACE         = TokenType.WHITE_SPACE;
    IElementType NEWLINE             = new NasmTokenType("NEWLINE");
    IElementType BAD_CHARACTER       = TokenType.BAD_CHARACTER;

    // ── Comments ────────────────────────────────────────────────────────────────
    IElementType COMMENT             = new NasmTokenType("COMMENT");

    // ── Literals ────────────────────────────────────────────────────────────────
    IElementType NUMBER              = new NasmTokenType("NUMBER");
    IElementType STRING              = new NasmTokenType("STRING");

    // ── Identifier / references ──────────────────────────────────────────────
    IElementType IDENTIFIER          = new NasmTokenType("IDENTIFIER");

    // ── Registers ───────────────────────────────────────────────────────────────
    IElementType REGISTER            = new NasmTokenType("REGISTER");

    // ── Size specifiers ──────────────────────────────────────────────────────────
    IElementType SIZE_SPECIFIER      = new NasmTokenType("SIZE_SPECIFIER");

    // ── Instructions ────────────────────────────────────────────────────────────
    IElementType INSTRUCTION         = new NasmTokenType("INSTRUCTION");

    // ── Preprocessor ────────────────────────────────────────────────────────────
    IElementType PREPROC_DIRECTIVE   = new NasmTokenType("PREPROC_DIRECTIVE");

    // ── Keywords ────────────────────────────────────────────────────────────────
    IElementType SECTION_KW          = new NasmTokenType("SECTION");
    IElementType GLOBAL_KW           = new NasmTokenType("GLOBAL");
    IElementType EXTERN_KW           = new NasmTokenType("EXTERN");
    IElementType COMMON_KW           = new NasmTokenType("COMMON");
    IElementType DB_KW               = new NasmTokenType("DB");
    IElementType DW_KW               = new NasmTokenType("DW");
    IElementType DD_KW               = new NasmTokenType("DD");
    IElementType DQ_KW               = new NasmTokenType("DQ");
    IElementType DX_KW               = new NasmTokenType("DX"); // dt/do/ddq/dy/dz
    IElementType RESB_KW             = new NasmTokenType("RESB");
    IElementType RESW_KW             = new NasmTokenType("RESW");
    IElementType RESD_KW             = new NasmTokenType("RESD");
    IElementType RESQ_KW             = new NasmTokenType("RESQ");
    IElementType RESX_KW             = new NasmTokenType("RESX"); // rest/reso/resy/resz
    IElementType EQU_KW              = new NasmTokenType("EQU");
    IElementType TIMES_KW            = new NasmTokenType("TIMES");
    IElementType BITS_KW             = new NasmTokenType("BITS");
    IElementType ALIGN_KW            = new NasmTokenType("ALIGN");
    IElementType INCBIN_KW           = new NasmTokenType("INCBIN");
    IElementType STRUC_KW            = new NasmTokenType("STRUC");
    IElementType ABSOLUTE_KW         = new NasmTokenType("ABSOLUTE");
    IElementType DEFAULT_KW          = new NasmTokenType("DEFAULT");

    // ── Punctuation ─────────────────────────────────────────────────────────────
    IElementType COLON               = new NasmTokenType("COLON");
    IElementType COMMA               = new NasmTokenType("COMMA");
    IElementType LBRACKET            = new NasmTokenType("LBRACKET");
    IElementType RBRACKET            = new NasmTokenType("RBRACKET");
    IElementType LPAREN              = new NasmTokenType("LPAREN");
    IElementType RPAREN              = new NasmTokenType("RPAREN");
    IElementType LBRACE              = new NasmTokenType("LBRACE");
    IElementType RBRACE              = new NasmTokenType("RBRACE");
    IElementType PLUS                = new NasmTokenType("PLUS");
    IElementType MINUS               = new NasmTokenType("MINUS");
    IElementType STAR                = new NasmTokenType("STAR");
    IElementType SLASH               = new NasmTokenType("SLASH");
    IElementType PERCENT             = new NasmTokenType("PERCENT");
    IElementType TILDE               = new NasmTokenType("TILDE");
    IElementType CARET               = new NasmTokenType("CARET");
    IElementType AMP                 = new NasmTokenType("AMP");
    IElementType PIPE                = new NasmTokenType("PIPE");
    IElementType BANG                = new NasmTokenType("BANG");
    IElementType LT                  = new NasmTokenType("LT");
    IElementType GT                  = new NasmTokenType("GT");
    IElementType SHL                 = new NasmTokenType("SHL");
    IElementType SHR                 = new NasmTokenType("SHR");
    IElementType DOLLAR              = new NasmTokenType("DOLLAR");
    IElementType DOLLAR_DOLLAR       = new NasmTokenType("DOLLAR_DOLLAR");
    IElementType REL_KW              = new NasmTokenType("REL");
    IElementType ABS_KW              = new NasmTokenType("ABS");

    // ── Token sets ───────────────────────────────────────────────────────────────
    TokenSet COMMENTS     = TokenSet.create(COMMENT);
    TokenSet STRINGS      = TokenSet.create(STRING);
    TokenSet WHITESPACES  = TokenSet.create(WHITE_SPACE);

    TokenSet DATA_KEYWORDS = TokenSet.create(DB_KW, DW_KW, DD_KW, DQ_KW, DX_KW);
    TokenSet RES_KEYWORDS  = TokenSet.create(RESB_KW, RESW_KW, RESD_KW, RESQ_KW, RESX_KW);
    TokenSet ALL_KEYWORDS  = TokenSet.create(
            SECTION_KW, GLOBAL_KW, EXTERN_KW, COMMON_KW,
            DB_KW, DW_KW, DD_KW, DQ_KW, DX_KW,
            RESB_KW, RESW_KW, RESD_KW, RESQ_KW, RESX_KW,
            EQU_KW, TIMES_KW, BITS_KW, ALIGN_KW, INCBIN_KW,
            STRUC_KW, ABSOLUTE_KW, DEFAULT_KW
    );
}
