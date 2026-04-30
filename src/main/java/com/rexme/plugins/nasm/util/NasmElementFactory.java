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

package com.rexme.plugins.nasm.util;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.rexme.plugins.nasm.NasmFileType;
import com.rexme.plugins.nasm.psi.NasmFile;
import com.rexme.plugins.nasm.psi.NasmTokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Utility to synthesise PSI / AST fragments needed during refactoring.
 */
public final class NasmElementFactory {

    private NasmElementFactory() {}

    /**
     * Creates a dummy NASM file containing a single label definition,
     * then returns the IDENTIFIER token inside it.
     * Used by rename to replace identifier leaf nodes.
     */
    public static @NotNull ASTNode createIdentifierNode(
            @NotNull Project project, @NotNull String name) {
        // Dummy file: "name:\n  nop\n"
        String src = name + ":\n  nop\n";
        NasmFile dummy = (NasmFile) PsiFileFactory.getInstance(project)
                .createFileFromText("dummy.asm", NasmFileType.INSTANCE, src);
        ASTNode root = dummy.getNode();
        // Walk down to first IDENTIFIER token
        ASTNode id = findFirst(root, NasmTokenTypes.IDENTIFIER);
        if (id == null) throw new IllegalStateException("Cannot create identifier node for: " + name);
        return id.copyElement();
    }

    private static ASTNode findFirst(ASTNode node, com.intellij.psi.tree.IElementType type) {
        if (node.getElementType() == type) return node;
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            ASTNode found = findFirst(child, type);
            if (found != null) return found;
        }
        return null;
    }
}
