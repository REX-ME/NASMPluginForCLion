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

package com.rexme.plugins.nasm.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.IncorrectOperationException;
import com.rexme.plugins.nasm.util.NasmElementFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PSI element for a label definition  (LABEL_DEF node in the AST).
 * The first IDENTIFIER child holds the label name; the COLON follows it.
 *
 * Implementing {@link NasmLabelDef} (which extends {@link com.intellij.psi.PsiNameIdentifierOwner})
 * gives us rename-refactoring for free via the IntelliJ platform.
 */
public class NasmLabelDefImpl extends NasmCompositeElementImpl implements NasmLabelDef {

    public NasmLabelDefImpl(@NotNull ASTNode node) {
        super(node);
    }

    // ── PsiNamedElement / PsiNameIdentifierOwner ──────────────────────────────

    @Override
    public @Nullable String getName() {
        ASTNode id = nameNode();
        return id != null ? id.getText() : null;
    }

    @Override
    public PsiElement setName(@NotNull String newName) throws IncorrectOperationException {
        ASTNode id = nameNode();
        if (id == null) throw new IncorrectOperationException("No name node");
        ASTNode newId = NasmElementFactory.createIdentifierNode(getProject(), newName);
        getNode().replaceChild(id, newId);
        return this;
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        ASTNode id = nameNode();
        return id != null ? id.getPsi() : null;
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private @Nullable ASTNode nameNode() {
        return getNode().findChildByType(
                TokenSet.create(NasmTokenTypes.IDENTIFIER));
    }
}
