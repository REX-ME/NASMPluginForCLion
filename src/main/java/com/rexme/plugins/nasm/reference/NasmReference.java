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

package com.rexme.plugins.nasm.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import com.rexme.plugins.nasm.psi.NasmLabelRefImpl;
import com.rexme.plugins.nasm.util.NasmUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves a {@link NasmLabelRefImpl} to its {@link com.rexme.plugins.nasm.psi.NasmLabelDef}.
 *
 * <p>Handles local labels correctly:
 * <ul>
 *   <li>{@code jmp .loop}        → resolves to the nearest preceding
 *       {@code parentLabel.loop} definition</li>
 *   <li>{@code jmp error.loop}   → resolves to {@code error.loop} directly</li>
 *   <li>{@code jmp myFunc}       → resolves to {@code myFunc} directly</li>
 * </ul>
 * </p>
 */
public final class NasmReference extends PsiReferenceBase<NasmLabelRefImpl> {

    public NasmReference(@NotNull NasmLabelRefImpl element) {
        super(element, new TextRange(0, element.getTextLength()));
    }

    @Override
    public @Nullable PsiElement resolve() {
        String name = myElement.getName();
        if (name == null || name.isEmpty()) return null;
        PsiFile file = myElement.getContainingFile();
        if (file == null) return null;

        // Resolve local (.xxx) and fully-qualified (parent.local) names
        String fullName = NasmUtil.resolveRefFullName(file, myElement, name);
        return NasmUtil.findLabelDef(file, fullName);
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newName) {
        return super.handleElementRename(newName);
    }

    @Override
    public Object @NotNull [] getVariants() {
        return EMPTY_ARRAY;
    }
}
