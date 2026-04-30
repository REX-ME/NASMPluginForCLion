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

package com.rexme.plugins.nasm.spellcheck;

import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import com.rexme.plugins.nasm.psi.NasmTokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Suppresses spell-checking on NASM assembly tokens.
 *
 * <p>Mnemonics, register names, labels, and numeric literals are not
 * natural-language words and would generate hundreds of false positives.
 * Only {@code STRING} literals retain spell-checking so that
 * user-visible strings in {@code .data} sections are still checked.</p>
 */
public final class NasmSpellcheckStrategy extends SpellcheckingStrategy {

    @Override
    public @NotNull Tokenizer<?> getTokenizer(@NotNull PsiElement element) {
        var tokenType = element.getNode().getElementType();

        // Only spell-check string content
        if (tokenType == NasmTokenTypes.STRING) {
            return super.getTokenizer(element); // default string tokenizer
        }

        // Suppress everything else: labels, mnemonics, registers, comments, …
        return EMPTY_TOKENIZER;
    }

    @Override
    public boolean isMyContext(@NotNull PsiElement element) {
        // Activate for all elements in NASM files
        return element.getContainingFile() instanceof com.rexme.plugins.nasm.psi.NasmFile;
    }
}
