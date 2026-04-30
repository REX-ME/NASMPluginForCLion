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
import com.intellij.psi.PsiReference;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.IncorrectOperationException;
import com.rexme.plugins.nasm.reference.NasmReference;
import com.rexme.plugins.nasm.util.NasmElementFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PSI element for a label reference  (LABEL_REF node in the AST).
 *
 * <p>{@link #getReference()} returns a {@link NasmReference} that navigates to
 * the matching {@link NasmLabelDef} in the same file.</p>
 *
 * <p>Implementing {@link NasmLabelRef} / {@link NasmNamedElement}
 * enables rename-refactoring: when the user renames a label definition,
 * all NasmLabelRef elements with the same name in the file are renamed too.</p>
 */
public class NasmLabelRefImpl extends NasmCompositeElementImpl implements NasmLabelRef {

    public NasmLabelRefImpl(@NotNull ASTNode node) {
        super(node);
    }

    // ── PsiReference  (GoTo Definition) ───────────────────────────────────────

    @Override
    public @Nullable PsiReference getReference() {
        return new NasmReference(this);
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
        return getNode().findChildByType(TokenSet.create(NasmTokenTypes.IDENTIFIER));
    }
}
