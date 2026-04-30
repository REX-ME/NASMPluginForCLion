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

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.Grouper;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.rexme.plugins.nasm.psi.NasmFile;
import com.rexme.plugins.nasm.psi.NasmLabelDef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NasmStructureViewModel
        extends StructureViewModelBase
        implements StructureViewModel.ElementInfoProvider {

    public NasmStructureViewModel(@NotNull NasmFile file, @Nullable Editor editor) {
        super(file, editor, new NasmStructureViewElement(file));
        withSuitableClasses(NasmLabelDef.class);
    }

    // Offer alphabetical sort as an option
    @Override
    public Sorter @NotNull [] getSorters() {
        return new Sorter[]{Sorter.ALPHA_SORTER};
    }

    @Override public Grouper  @NotNull [] getGroupers()  { return Grouper.EMPTY_ARRAY;  }
    @Override public Filter   @NotNull [] getFilters()   { return Filter.EMPTY_ARRAY;   }

    // Non-local labels can have local-label children
    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) { return false; }

    // Local labels are always leaves
    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        Object val = element.getValue();
        if (val instanceof NasmLabelDef def) {
            String name = def.getName();
            return name != null && name.startsWith(".");
        }
        return false;
    }
}
