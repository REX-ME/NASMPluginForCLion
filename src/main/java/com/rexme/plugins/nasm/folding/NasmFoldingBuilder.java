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

package com.rexme.plugins.nasm.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.rexme.plugins.nasm.psi.*;
import com.rexme.plugins.nasm.util.NasmUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Code folding for NASM assembly.
 *
 * <h3>Fold regions</h3>
 * <ol>
 *   <li><b>Procedure bodies</b> — from the colon of a non-local label to the
 *       line before the next non-local label (or EOF).  Example:
 *       {@code funcA: ; funcA ...} collapses the body.</li>
 *   <li><b>Section blocks</b> — from the end of a {@code section .text}
 *       directive to just before the next {@code section} directive (or EOF).
 *       Example: {@code section .data ; section .data ...}</li>
 * </ol>
 *
 * <p>Both kinds are <em>not</em> collapsed by default; the user can collapse
 * them manually or via Ctrl+Shift+[-].</p>
 */
public final class NasmFoldingBuilder extends FoldingBuilderEx {

    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(
            @NotNull PsiElement root,
            @NotNull Document document,
            boolean quick) {

        if (!(root instanceof NasmFile file)) return FoldingDescriptor.EMPTY;

        List<FoldingDescriptor> descriptors = new ArrayList<>();

        // ── 1. Procedure-body folds ───────────────────────────────────────────
        List<NasmLabelDef> globalDefs = NasmUtil.findGlobalLabelDefs(file);
        for (int i = 0; i < globalDefs.size(); i++) {
            NasmLabelDef current = globalDefs.get(i);
            int foldStart = current.getTextRange().getEndOffset();

            int foldEnd;
            if (i + 1 < globalDefs.size()) {
                NasmLabelDef next = globalDefs.get(i + 1);
                int nextLine = document.getLineNumber(next.getTextOffset());
                foldEnd = nextLine > 0
                        ? document.getLineStartOffset(nextLine) - 1
                        : next.getTextOffset() - 1;
            } else {
                foldEnd = root.getTextRange().getEndOffset();
            }

            addFold(descriptors, current.getNode(), document, foldStart, foldEnd);
        }

        // ── 2. Section-block folds ────────────────────────────────────────────
        List<PsiElement> sections = new ArrayList<>(
                PsiTreeUtil.findChildrenOfType(file, NasmCompositeElementImpl.class)
                        .stream()
                        .filter(e -> e.getNode().getElementType() == NasmElementTypes.SECTION_DECL)
                        .toList());

        for (int i = 0; i < sections.size(); i++) {
            PsiElement sec = sections.get(i);
            int foldStart = sec.getTextRange().getEndOffset();
            int foldEnd;
            if (i + 1 < sections.size()) {
                int nextLine = document.getLineNumber(sections.get(i + 1).getTextOffset());
                foldEnd = nextLine > 0
                        ? document.getLineStartOffset(nextLine) - 1
                        : sections.get(i + 1).getTextOffset() - 1;
            } else {
                foldEnd = root.getTextRange().getEndOffset();
            }
            addFold(descriptors, sec.getNode(), document, foldStart, foldEnd);
        }

        return descriptors.toArray(FoldingDescriptor.EMPTY);
    }

    // ── Placeholder text ──────────────────────────────────────────────────────

    @Override
    public @Nullable String getPlaceholderText(@NotNull ASTNode node) {
        PsiElement psi = node.getPsi();

        if (psi instanceof NasmLabelDef def) {
            String name = def.getName();
            return name != null ? " \u2026 ; " + name : " \u2026";
        }

        if (psi instanceof NasmCompositeElementImpl c
         && c.getNode().getElementType() == NasmElementTypes.SECTION_DECL) {
            String text = c.getText().trim();
            // Show "section .data …"
            return "  \u2026 ; " + text.replaceAll("\\s+", " ");
        }

        return " \u2026";
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static void addFold(@NotNull List<FoldingDescriptor> out,
                                @NotNull ASTNode anchor,
                                @NotNull Document doc,
                                int start,
                                int end) {
        if (end <= start) return;
        int startLine = doc.getLineNumber(Math.min(start, doc.getTextLength() - 1));
        int endLine   = doc.getLineNumber(Math.min(end,   doc.getTextLength() - 1));
        if (endLine <= startLine) return;                  // nothing to fold
        int clampedEnd = Math.min(end, doc.getTextLength());
        if (clampedEnd <= start) return;
        out.add(new FoldingDescriptor(anchor, new TextRange(start, clampedEnd)));
    }
}
