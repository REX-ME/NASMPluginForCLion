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

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.rexme.plugins.nasm.psi.NasmTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Enables bracket matching for {@code []} and {@code ()} in NASM files. */
public final class NasmBraceMatcher implements PairedBraceMatcher {

    private static final BracePair[] PAIRS = {
            new BracePair(NasmTokenTypes.LBRACKET, NasmTokenTypes.RBRACKET, false),
            new BracePair(NasmTokenTypes.LPAREN,   NasmTokenTypes.RPAREN,   false),
            new BracePair(NasmTokenTypes.LBRACE,   NasmTokenTypes.RBRACE,   false),
    };

    @Override public BracePair @NotNull [] getPairs() { return PAIRS; }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType,
                                                   @Nullable IElementType contextType) {
        return true;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
