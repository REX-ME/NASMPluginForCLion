package com.rexme.plugins.nasm;

import com.intellij.lang.Language;

public class NasmLanguage extends Language{
    public static final NasmLanguage INSTANCE = new NasmLanguage();
    private NasmLanguage() {
        super("NASM");
    }
}
