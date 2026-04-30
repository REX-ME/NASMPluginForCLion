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

import com.intellij.icons.AllIcons;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.rexme.plugins.nasm.NasmIcons;
import com.rexme.plugins.nasm.psi.*;
import com.rexme.plugins.nasm.util.NasmUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tree element for the NASM Structure View panel.
 *
 * <h3>Tree structure</h3>
 * <pre>
 *   NasmFile
 *   ├── _start         [function icon]
 *   │   └── .done      [local-label icon, indented under _start]
 *   ├── error          [function icon]
 *   │   ├── .loop
 *   │   └── .done
 *   ├── msg            [variable icon — data label]
 *   └── buf            [variable icon — bss label]
 * </pre>
 *
 * <p>Section context is shown as a location string, e.g. "[.text]",
 * using the nearest preceding {@code SECTION_DECL} node.</p>
 */
public final class NasmStructureViewElement extends PsiTreeElementBase<PsiElement> {

    NasmStructureViewElement(@NotNull PsiElement element) {
        super(element);
    }

    // ── Children ──────────────────────────────────────────────────────────────

    @Override
    public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
        PsiElement el = getElement();
        if (el == null) return List.of();

        // File root: top-level (non-dot) labels only
        if (el instanceof NasmFile file) {
            return NasmUtil.findGlobalLabelDefs(file).stream()
                    .map(NasmStructureViewElement::new)
                    .collect(Collectors.toList());
        }

        // Non-local label: return local (.xxx) children
        if (el instanceof NasmLabelDef def) {
            String name = def.getName();
            if (name == null || name.startsWith(".")) return List.of();
            return NasmUtil.findLocalLabelsUnder(def).stream()
                    .map(NasmStructureViewElement::new)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    // ── Presentation ──────────────────────────────────────────────────────────

    @Override
    public @Nullable String getPresentableText() {
        PsiElement el = getElement();
        if (el instanceof NasmFile f) return f.getName();
        if (el instanceof NasmLabelDef def) return def.getName();
        return null;
    }

    /**
     * Returns the section this label belongs to, shown greyed-out in the tree
     * as a "location" string (e.g. {@code [.data]}).
     */
    @Override
    public @Nullable String getLocationString() {
        PsiElement el = getElement();
        if (!(el instanceof NasmLabelDef def)) return null;
        return findSectionFor(def);
    }

    @Override
    public @Nullable Icon getIcon(boolean open) {
        PsiElement el = getElement();
        if (el instanceof NasmFile) return NasmIcons.FILE;

        if (el instanceof NasmLabelDef def) {
            String name  = def.getName();
            String section = findSectionFor(def);

            if (name != null && name.startsWith(".")) {
                // local label
                return AllIcons.Nodes.Field;
            }
            // Data/BSS variables vs code labels
            if (".data".equals(section) || ".bss".equals(section)
             || "data".equals(section)  || "bss".equals(section)) {
                return AllIcons.Nodes.Variable;
            }
            return AllIcons.Nodes.Function;
        }
        return null;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Scans backwards from {@code def} to find the most recently declared
     * section name, returned as e.g. {@code ".text"} or {@code ".data"}.
     */
    private static @Nullable String findSectionFor(@NotNull NasmLabelDef def) {
        PsiElement el = def;
        while (el != null) {
            PsiElement prev = el.getPrevSibling();
            if (prev == null) { el = el.getParent(); continue; }
            el = prev;

            // Walk into this sibling's last child recursively to find SECTION_DECL
            PsiElement sectionDecl = findSectionDecl(prev);
            if (sectionDecl != null) {
                return extractSectionName(sectionDecl.getText());
            }
        }
        return null;
    }

    private static @Nullable PsiElement findSectionDecl(@NotNull PsiElement el) {
        if (el instanceof NasmCompositeElementImpl composite
         && composite.getNode().getElementType() == NasmElementTypes.SECTION_DECL) {
            return composite;
        }
        // Check last child
        PsiElement last = el.getLastChild();
        if (last != null) return findSectionDecl(last);
        return null;
    }

    /**
     * Extracts the section name from a {@code section .text} directive text.
     * Returns just {@code ".text"}.
     */
    private static @Nullable String extractSectionName(@NotNull String text) {
        // "section .text" / "segment .data" / "section .bss exec"
        String[] parts = text.trim().split("\\s+");
        if (parts.length >= 2) return parts[1].toLowerCase();
        return null;
    }
}
