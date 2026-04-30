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

package com.rexme.plugins.nasm.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.util.ProcessingContext;
import com.rexme.plugins.nasm.NasmLanguage;
import com.rexme.plugins.nasm.psi.NasmElementTypes;
import com.rexme.plugins.nasm.psi.NasmTokenTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Registers extra PSI reference providers for NASM files.
 *
 * <h3>%include file navigation</h3>
 * Provides Ctrl+Click (GoTo) on string literals inside
 * {@code %include "path/to/file.asm"} lines:
 * <pre>
 *   %include "macros.asm"    ; Ctrl+Click on "macros.asm" → opens the file
 * </pre>
 *
 * <p>The {@code FileReferenceSet} handles relative paths (relative to the
 * current file's directory), absolute paths, and auto-completes file names.</p>
 */
public final class NasmReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {

        // ── %include "path" navigation ────────────────────────────────────────
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(NasmTokenTypes.STRING)
                        .withLanguage(NasmLanguage.INSTANCE),
                new IncludeStringReferenceProvider()
        );
    }

    // ── Provider ──────────────────────────────────────────────────────────────

    private static final class IncludeStringReferenceProvider extends PsiReferenceProvider {

        @Override
        public PsiReference @NotNull [] getReferencesByElement(
                @NotNull PsiElement element,
                @NotNull ProcessingContext context) {

            // The STRING token must be inside a PREPROC_STMT whose text
            // starts with "%include" (case-insensitive).
            PsiElement parent = element.getParent();
            if (parent == null) return PsiReference.EMPTY_ARRAY;
            if (parent.getNode().getElementType() != NasmElementTypes.PREPROC_STMT)
                return PsiReference.EMPTY_ARRAY;

            String parentText = parent.getText().trim();
            if (!parentText.toLowerCase().startsWith("%include"))
                return PsiReference.EMPTY_ARRAY;

            // The STRING token text is  "path/to/file.asm"  (with quotes).
            // Strip the surrounding quote characters.
            String raw = element.getText();
            if (raw.length() < 2) return PsiReference.EMPTY_ARRAY;

            // Offset 1 to skip the leading quote inside the element range.
            return new FileReferenceSet(raw.substring(1, raw.length() - 1),
                    element,
                    1,          // offset of the path inside the element text
                    null,
                    true        // case-sensitive
            ).getAllReferences();
        }
    }
}
