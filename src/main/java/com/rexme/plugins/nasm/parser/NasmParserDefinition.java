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

package com.rexme.plugins.nasm.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.rexme.plugins.nasm.NasmLanguage;
import com.rexme.plugins.nasm.lexer.NasmLexer;
import com.rexme.plugins.nasm.psi.*;
import org.jetbrains.annotations.NotNull;

public final class NasmParserDefinition implements ParserDefinition {

    public static final IFileElementType FILE =
            new IFileElementType(NasmLanguage.INSTANCE);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new NasmLexer();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new NasmParser();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return NasmTokenTypes.COMMENTS;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return NasmTokenTypes.STRINGS;
    }

    @Override
    public @NotNull TokenSet getWhitespaceTokens() {
        return NasmTokenTypes.WHITESPACES;
    }

    /**
     * Factory method — maps AST node types to concrete PSI element classes.
     */
    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        final com.intellij.psi.tree.IElementType type = node.getElementType();

        if (type == NasmElementTypes.LABEL_DEF) return new NasmLabelDefImpl(node);
        if (type == NasmElementTypes.LABEL_REF) return new NasmLabelRefImpl(node);

        return new NasmCompositeElementImpl(node);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new NasmFile(viewProvider);
    }
}
