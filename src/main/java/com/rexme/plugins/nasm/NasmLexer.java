package com.rexme.plugins.nasm;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NasmLexer extends LexerBase {
    private CharSequence buffer;
    private int startOffset;
    private int endOffset;
    private int currentOffset;
    private IElementType tokenType;

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.currentOffset = startOffset;
        advance();
    }

    @Override
    public void advance() {
        if (currentOffset >= endOffset) {
            tokenType = null;
            return;
        }

        startOffset = currentOffset;
        char c = buffer.charAt(currentOffset);

        if (c == ';') {
            // Kommentar-Logik: Alles bis zum Zeilenende konsumieren
            while (currentOffset < endOffset && buffer.charAt(currentOffset) != '\n') {
                currentOffset++;
            }
            tokenType = NasmTypes.COMMENT;
        } else if (Character.isWhitespace(c)) {
            // Whitespace-Logik
            while (currentOffset < endOffset && Character.isWhitespace(buffer.charAt(currentOffset))) {
                currentOffset++;
            }
            tokenType = TokenType.WHITE_SPACE;
        } else {
            // Alles andere als ein einzelnes Zeichen behandeln (verhindert Endlosschleifen!)
            currentOffset++;
            tokenType = TokenType.WHITE_SPACE; // Vorerst als Whitespace, damit der Parser nicht abstürzt
        }
    }

    @Override public int getState() { return 0; }
    @Nullable @Override public IElementType getTokenType() { return tokenType; }
    @Override public int getTokenStart() { return startOffset; }
    @Override public int getTokenEnd() { return currentOffset; }
    @Override public int getBufferEnd() { return endOffset; }
    @NotNull @Override public CharSequence getBufferSequence() { return buffer; }
}
