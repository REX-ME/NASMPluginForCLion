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

import com.intellij.psi.tree.IElementType;

public interface NasmElementTypes {

    // ── Top-level constructs ────────────────────────────────────────────────────
    IElementType STATEMENT           = new NasmElementType("STATEMENT");

    // ── Label definition  (e.g.  myLabel:) ────────────────────────────────────
    IElementType LABEL_DEF           = new NasmElementType("LABEL_DEF");

    // ── Label reference  (e.g.  jmp myLabel) ──────────────────────────────────
    IElementType LABEL_REF           = new NasmElementType("LABEL_REF");

    // ── Instruction + operands ──────────────────────────────────────────────────
    IElementType INSTRUCTION_STMT    = new NasmElementType("INSTRUCTION_STMT");
    IElementType OPERAND             = new NasmElementType("OPERAND");
    IElementType MEMORY_OPERAND      = new NasmElementType("MEMORY_OPERAND");

    // ── Data declarations  (db / dw / dd …) ────────────────────────────────────
    IElementType DATA_DECL           = new NasmElementType("DATA_DECL");

    // ── Reserve declarations  (resb / resw / …) ────────────────────────────────
    IElementType RES_DECL            = new NasmElementType("RES_DECL");

    // ── EQU constant ───────────────────────────────────────────────────────────
    IElementType EQU_DECL            = new NasmElementType("EQU_DECL");

    // ── Section directive ───────────────────────────────────────────────────────
    IElementType SECTION_DECL        = new NasmElementType("SECTION_DECL");

    // ── global / extern / common ────────────────────────────────────────────────
    IElementType EXPORT_DECL         = new NasmElementType("EXPORT_DECL");

    // ── Preprocessor line (%define, %macro …) ──────────────────────────────────
    IElementType PREPROC_STMT        = new NasmElementType("PREPROC_STMT");

    // ── Catch-all for unrecognised / misc lines ─────────────────────────────────
    IElementType MISC_STMT           = new NasmElementType("MISC_STMT");
}
