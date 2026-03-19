package com.rexme.plugins.nasm;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

public interface NasmTypes {
    IElementType COMMENT = new IElementType("NASM Comment", NasmLanguage.INSTANCE);
    TokenSet COMMENTS = TokenSet.create(COMMENT);
}
