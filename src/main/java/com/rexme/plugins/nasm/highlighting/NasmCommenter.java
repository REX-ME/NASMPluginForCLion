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

import com.intellij.lang.CodeDocumentationAwareCommenter;
import com.intellij.psi.PsiComment;
import com.intellij.psi.tree.IElementType;
import com.rexme.plugins.nasm.psi.NasmTokenTypes;
import org.jetbrains.annotations.Nullable;

/** Teaches the IDE how to toggle NASM line comments with Ctrl+/. */
public final class NasmCommenter implements CodeDocumentationAwareCommenter {

    @Override public @Nullable String getLineCommentPrefix()           { return "; "; }
    @Override public @Nullable String getBlockCommentPrefix()          { return null; }
    @Override public @Nullable String getBlockCommentSuffix()          { return null; }
    @Override public @Nullable String getCommentedBlockCommentPrefix() { return null; }
    @Override public @Nullable String getCommentedBlockCommentSuffix() { return null; }
    @Override public @Nullable IElementType getLineCommentTokenType()  { return NasmTokenTypes.COMMENT; }
    @Override public @Nullable IElementType getBlockCommentTokenType() { return null; }
    @Override public @Nullable IElementType getDocumentationCommentTokenType() { return null; }
    @Override public @Nullable String getDocumentationCommentPrefix()  { return null; }
    @Override public @Nullable String getDocumentationCommentLinePrefix(){ return null; }
    @Override public @Nullable String getDocumentationCommentSuffix()  { return null; }
    @Override public boolean isDocumentationComment(PsiComment element){ return false; }
}
