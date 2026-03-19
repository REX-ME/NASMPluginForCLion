package com.rexme.plugins.nasm;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public class NasmParserDefinition implements ParserDefinition{
    public static final IFileElementType FILE = new IFileElementType(NasmLanguage.INSTANCE);

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new NasmLexer();
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return NasmTypes.COMMENTS;
    }

    @Override public IFileElementType getFileNodeType() { return FILE; }
    @NotNull @Override public TokenSet getStringLiteralElements() { return TokenSet.EMPTY; }
    @NotNull @Override public PsiParser createParser(Project project) {
        return (root, builder) -> {
            PsiBuilder.Marker rootMarker = builder.mark();
            // Solange Daten da sind, konsumiere sie einfach
            while (!builder.eof()) {
                builder.advanceLexer();
            }
            rootMarker.done(root);
            return builder.getTreeBuilt();
        };
    }
    @Override public PsiFile createFile(FileViewProvider viewProvider) {
        return new com.intellij.extapi.psi.PsiFileBase(viewProvider, NasmLanguage.INSTANCE) {
            @NotNull @Override public com.intellij.openapi.fileTypes.FileType getFileType() { return NasmFileType.INSTANCE; }
        };
    }
    @Override public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) { return SpaceRequirements.MAY; }
    @NotNull @Override public PsiElement createElement(ASTNode node) { return new com.intellij.psi.impl.source.tree.CompositePsiElement(node.getElementType()){}; }
}
