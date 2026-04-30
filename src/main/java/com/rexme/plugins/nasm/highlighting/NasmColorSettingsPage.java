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

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.rexme.plugins.nasm.NasmIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public final class NasmColorSettingsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] DESCRIPTORS = {
            new AttributesDescriptor("Comment",           NasmSyntaxHighlighter.COMMENT),
            new AttributesDescriptor("Instruction",       NasmSyntaxHighlighter.INSTRUCTION),
            new AttributesDescriptor("Register",          NasmSyntaxHighlighter.REGISTER),
            new AttributesDescriptor("Size specifier",    NasmSyntaxHighlighter.SIZE_SPECIFIER),
            new AttributesDescriptor("Directive keyword", NasmSyntaxHighlighter.KEYWORD),
            new AttributesDescriptor("Number literal",    NasmSyntaxHighlighter.NUMBER),
            new AttributesDescriptor("String literal",    NasmSyntaxHighlighter.STRING),
            new AttributesDescriptor("Section name",      NasmSyntaxHighlighter.SECTION_NAME),
            new AttributesDescriptor("Preprocessor",      NasmSyntaxHighlighter.PREPROC),
            new AttributesDescriptor("Operator",          NasmSyntaxHighlighter.OPERATOR),
            new AttributesDescriptor("Label definition",  NasmSyntaxHighlighter.LABEL_DEFINITION),
            new AttributesDescriptor("Label reference",   NasmSyntaxHighlighter.LABEL_REFERENCE),
            new AttributesDescriptor("Bad character",     NasmSyntaxHighlighter.BAD_CHARACTER),
            new AttributesDescriptor("rel / abs modifier",NasmSyntaxHighlighter.REL_ABS),
    };

    @Override public @Nullable Icon getIcon()                    { return NasmIcons.FILE; }
    @Override public @NotNull SyntaxHighlighter getHighlighter() { return new NasmSyntaxHighlighter(); }

    @Override
    public @NotNull @NonNls String getDemoText() {
        return """
                ; NASM x86-64 demo file
                %define SYSCALL_EXIT 60

                section .data
                    msg     db "Hello, World!", 10  ; newline
                    msgLen  equ $ - msg

                section .bss
                    buf     resb 256
                    count   resq 1

                section .text
                    global _start

                _start:
                    mov     rax, 1          ; sys_write
                    mov     rdi, 1          ; stdout
                    lea     rsi, [msg]
                    mov     rdx, msgLen
                    syscall

                    xor     eax, eax
                    test    rax, rax
                    jz      .done
                    jmp     _start

                .done:
                    mov     rax, SYSCALL_EXIT
                    xor     rdi, rdi
                    syscall
                """;
    }

    /**
     * Maps custom tag names used in {@link #getDemoText()} to attribute keys.
     * (We rely on the lexer/annotator for semantic colouring instead of manual
     * tags, so this map is empty.)
     */
    @Override
    public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @Override public AttributesDescriptor @NotNull [] getAttributeDescriptors() { return DESCRIPTORS; }
    @Override public ColorDescriptor @NotNull []      getColorDescriptors()      { return ColorDescriptor.EMPTY_ARRAY; }
    @Override public @NotNull String                  getDisplayName()           { return "NASM Assembly"; }
}
