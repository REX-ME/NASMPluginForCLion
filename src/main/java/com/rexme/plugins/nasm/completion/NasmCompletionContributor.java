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

package com.rexme.plugins.nasm.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import com.rexme.plugins.nasm.NasmLanguage;
import com.rexme.plugins.nasm.psi.NasmTokenTypes;
import com.rexme.plugins.nasm.util.NasmUtil;
import com.rexme.plugins.nasm.psi.NasmLabelDef;
import org.jetbrains.annotations.NotNull;

/**
 * Provides label-name completion in operand positions.
 *
 * When the user types Ctrl+Space after an instruction, all label definitions
 * from the current file appear in the completion popup.
 */
public final class NasmCompletionContributor extends CompletionContributor {
    public NasmCompletionContributor() {
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(NasmTokenTypes.IDENTIFIER)
                        .withLanguage(NasmLanguage.INSTANCE),
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(
                            @NotNull CompletionParameters parameters,
                            @NotNull ProcessingContext context,
                            @NotNull CompletionResultSet result) {
                        var file = parameters.getOriginalFile();
                        int caretOffset = parameters.getOffset();
                        String currentCtx = "";
                        for (NasmLabelDef def : NasmUtil.findAllLabelDefs(file)) {
                            if (def.getTextOffset() >= caretOffset) break;
                            String n = def.getName();
                            if (n != null && !n.startsWith(".")) currentCtx = n;
                        }
                        final String ctx = currentCtx;

                        for (String fullName : NasmUtil.allDefinedLabelNames(file)) {
                            String display = (!ctx.isEmpty() && fullName.startsWith(ctx + "."))
                                    ? fullName.substring(ctx.length())  // "funcA.done" → ".done"
                                    : fullName;
                            result.addElement(LookupElementBuilder.create(display)
                                    .withTypeText("label").withBoldness(false));
                        }
                    }
                }
        );
    }
}
