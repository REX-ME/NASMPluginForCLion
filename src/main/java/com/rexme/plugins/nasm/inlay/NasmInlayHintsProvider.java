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

package com.rexme.plugins.nasm.inlay;

import com.intellij.codeInsight.hints.*;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.rexme.plugins.nasm.NasmLanguage;
import com.rexme.plugins.nasm.psi.NasmFile;
import com.rexme.plugins.nasm.psi.NasmTokenTypes;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

/**
 * Inlay hints that show the bit-width of registers inline.
 *
 * <p>Example: {@code mov rax, rbx} renders as
 * {@code mov rax«64», rbx«64»} — the hints appear after each register
 * name in a small grey typeface so you can quickly see operand sizes
 * without hovering.</p>
 *
 * <p>Hints can be toggled in
 * <em>Settings → Editor → Inlay Hints → NASM Assembly → Register width</em>.</p>
 */
@SuppressWarnings("UnstableApiUsage")
public final class NasmInlayHintsProvider
        implements InlayHintsProvider<NasmInlayHintsProvider.Settings> {

    // ── Register bit-width table ──────────────────────────────────────────────
    private static final Map<String, String> REG_BITS = buildRegBitMap();

    private static Map<String, String> buildRegBitMap() {
        var m = new java.util.HashMap<String, String>();
        // 64-bit
        for (String r : new String[]{"rax","rbx","rcx","rdx","rsi","rdi","rsp","rbp",
                "r8","r9","r10","r11","r12","r13","r14","r15","rip","rflags"})
            m.put(r, "64");
        // 32-bit
        for (String r : new String[]{"eax","ebx","ecx","edx","esi","edi","esp","ebp",
                "r8d","r9d","r10d","r11d","r12d","r13d","r14d","r15d","eip","eflags"})
            m.put(r, "32");
        // 16-bit
        for (String r : new String[]{"ax","bx","cx","dx","si","di","sp","bp",
                "r8w","r9w","r10w","r11w","r12w","r13w","r14w","r15w","ip","flags"})
            m.put(r, "16");
        // 8-bit
        for (String r : new String[]{"al","bl","cl","dl","ah","bh","ch","dh",
                "sil","dil","spl","bpl",
                "r8b","r9b","r10b","r11b","r12b","r13b","r14b","r15b"})
            m.put(r, "8");
        // XMM 128, YMM 256, ZMM 512, MMX 64
        for (int i = 0; i <= 15; i++) {
            m.put("xmm" + i, "128");
            m.put("ymm" + i, "256");
            m.put("zmm" + i, "512");
        }
        for (int i = 0; i <= 7; i++) m.put("mm" + i, "64");
        return Map.copyOf(m);
    }

    // ── Settings ──────────────────────────────────────────────────────────────

    public static final class Settings {
        public boolean showRegisterWidth = true;
    }

    @Override
    public @NotNull Settings createSettings() {
        return new Settings();
    }

    // ── Provider metadata ─────────────────────────────────────────────────────

    private static final SettingsKey<Settings> KEY =
            new SettingsKey<>("nasm.register.width");

    @Override public @NotNull SettingsKey<Settings> getKey()         { return KEY; }
    @Override public @Nls @NotNull String getName()                  { return "Register width"; }
    @Override public @Nullable String getPreviewText()               { return "mov rax, rbx\nadd ecx, edx"; }

    @Override
    public @NotNull ImmediateConfigurable createConfigurable(@NotNull Settings settings) {
        return changeListener -> {
            JPanel panel = new JPanel();
            JCheckBox cb = new JCheckBox("Show register bit-width hints", settings.showRegisterWidth);
            cb.addActionListener(e -> {
                settings.showRegisterWidth = cb.isSelected();
                changeListener.settingsChanged();
            });
            panel.add(cb);
            return panel;
        };
    }

    @Override
    public boolean isLanguageSupported(@NotNull Language language) {
        return language.is(NasmLanguage.INSTANCE);
    }

    // ── Collector ─────────────────────────────────────────────────────────────

    @Override
    public @Nullable InlayHintsCollector getCollectorFor(
            @NotNull PsiFile file,
            @NotNull Editor editor,
            @NotNull Settings settings,
            @NotNull InlayHintsSink sink) {

        if (!settings.showRegisterWidth) return null;
        if (!(file instanceof NasmFile)) return null;

        return new FactoryInlayHintsCollector(editor) {
            @Override
            public boolean collect(@NotNull PsiElement element,
                                   @NotNull Editor editor,
                                   @NotNull InlayHintsSink sink) {

                if (element.getNode().getElementType() != NasmTokenTypes.REGISTER)
                    return true; // keep traversing

                String regName = element.getText().toLowerCase();
                String bits    = REG_BITS.get(regName);
                if (bits == null) return true;

                PresentationFactory factory = getFactory();
                InlayPresentation hint = factory.smallText("\u00AB" + bits + "\u00BB");
                InlayPresentation rounded = factory.roundWithBackground(hint);

                // Place hint right after the register token
                int offset = element.getTextRange().getEndOffset();
                sink.addInlineElement(offset, true, rounded, false);

                return true; // continue traversal
            }
        };
    }
}
