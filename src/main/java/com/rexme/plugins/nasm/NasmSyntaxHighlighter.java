package com.rexme.plugins.nasm;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class NasmSyntaxHighlighter extends SyntaxHighlighterBase {
    // Hier definierst du den "Style" – wir erben einfach das Standard-Grau für Kommentare
    public static final TextAttributesKey NASM_COMMENT =
            createTextAttributesKey("NASM_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new NasmLexer(); // Nutzt deinen Lexer, um ; zu finden
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (tokenType.equals(NasmTypes.COMMENT)) {
            return new TextAttributesKey[]{NASM_COMMENT};
        }
        return new TextAttributesKey[0];
    }
}
