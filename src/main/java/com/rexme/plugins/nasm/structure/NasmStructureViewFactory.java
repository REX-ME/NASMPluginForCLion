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

package com.rexme.plugins.nasm.structure;

import com.intellij.ide.structureView.*;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.rexme.plugins.nasm.psi.NasmFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NasmStructureViewFactory implements PsiStructureViewFactory {

    @Override
    public @Nullable StructureViewBuilder getStructureViewBuilder(@NotNull PsiFile psiFile) {
        if (!(psiFile instanceof NasmFile nasmFile)) return null;

        return new TreeBasedStructureViewBuilder() {
            @Override
            public @NotNull StructureViewModel createStructureViewModel(@Nullable Editor editor) {
                return new NasmStructureViewModel(nasmFile, editor);
            }
        };
    }
}
