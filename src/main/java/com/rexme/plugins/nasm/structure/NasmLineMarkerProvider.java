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

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.rexme.plugins.nasm.psi.NasmLabelDef;
import com.rexme.plugins.nasm.psi.NasmLabelRef;
import com.rexme.plugins.nasm.psi.NasmTokenTypes;
import com.rexme.plugins.nasm.util.NasmUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Gutter icon on every {@link NasmLabelDef} that has at least one reference.
 *
 * <p>Clicking the icon opens a popup listing every usage of the label in the
 * file.  Clicking a popup entry navigates to that usage.</p>
 */
public final class NasmLineMarkerProvider implements LineMarkerProvider {

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        // Only process the IDENTIFIER leaf that carries the label name
        if (element.getNode().getElementType() != NasmTokenTypes.IDENTIFIER) return null;
        if (!(element.getParent() instanceof NasmLabelDef def)) return null;

        String name = def.getName();
        if (name == null || name.isEmpty()) return null;

        var file = def.getContainingFile();
        if (file == null) return null;

        String fullName = NasmUtil.fullNameOf(file, def);

        // Collect all references that resolve to this label
        List<NasmLabelRef> usages = NasmUtil.findAllLabelRefs(file).stream()
                .filter(ref -> {
                    String rn = ref.getName();
                    return rn != null && fullName.equals(NasmUtil.resolveRefFullName(file, ref, rn));
                })
                .collect(Collectors.toList());

        if (usages.isEmpty()) return null;

        // NavigationGutterIconBuilder provides the click → popup behaviour for free
        return NavigationGutterIconBuilder
                .create(AllIcons.Gutter.OverridenMethod)
                .setTargets(usages)
                .setTooltipText("Usages of '" + fullName + "'")
                .setPopupTitle("Usages of '" + fullName + "'")
                .createLineMarkerInfo(element);
    }
}
