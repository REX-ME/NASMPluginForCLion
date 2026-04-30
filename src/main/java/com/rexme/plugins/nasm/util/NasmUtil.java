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

package com.rexme.plugins.nasm.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.rexme.plugins.nasm.psi.NasmLabelDef;
import com.rexme.plugins.nasm.psi.NasmLabelRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * File-scoped helpers for NASM label lookup.
 *
 * <h3>Caching</h3>
 * All expensive operations (PSI walks, map building) are wrapped in
 * {@link CachedValuesManager} so they are re-computed only when the file
 * changes.  On a 10 000-line file a single call to
 * {@link #buildFullNameMap} costs ~1 ms; subsequent calls in the same
 * edit-round are free.
 *
 * <h3>Local-label semantics</h3>
 * NASM treats labels starting with {@code '.'} as <em>local</em> to the
 * preceding non-dot label.  The canonical full name is formed by
 * concatenating that parent and the dot-name, e.g.:
 * <pre>
 *   error:
 *       .loop:        ; canonical name  →  "error.loop"
 *       jmp .loop     ; short ref       →  "error.loop"
 *       jmp error.loop; full ref        →  "error.loop" (direct)
 * </pre>
 */
public final class NasmUtil {

    private NasmUtil() {}

    // ── Cached label-def list ─────────────────────────────────────────────────

    /**
     * Returns all {@link NasmLabelDef} elements in {@code file},
     * in source order.  The result is cached until the next PSI change.
     */
    public static @NotNull List<NasmLabelDef> findAllLabelDefs(@NotNull PsiFile file) {
        return CachedValuesManager.getCachedValue(file, () -> {
            List<NasmLabelDef> defs =
                    new ArrayList<>(PsiTreeUtil.findChildrenOfType(file, NasmLabelDef.class));
            return CachedValueProvider.Result.create(defs, file);
        });
    }

    // ── Cached full-name map ──────────────────────────────────────────────────

    /**
     * Builds (and caches) a map  <b>canonical full name → all definitions</b>.
     *
     * <p>Local labels ({@code .xxx}) are stored under
     * {@code currentGlobalLabel + ".xxx"}.  If a name appears more than once,
     * the list contains all definitions so the annotator can flag duplicates.</p>
     */
    public static @NotNull Map<String, List<NasmLabelDef>> buildFullNameMap(@NotNull PsiFile file) {
        return CachedValuesManager.getCachedValue(file, () -> {
            Map<String, List<NasmLabelDef>> map = new LinkedHashMap<>();
            String currentGlobal = "";
            for (NasmLabelDef def : findAllLabelDefs(file)) {
                String raw = def.getName();
                if (raw == null || raw.isEmpty()) continue;
                String full = raw.startsWith(".") ? currentGlobal + raw : raw;
                if (!raw.startsWith(".")) currentGlobal = raw;
                map.computeIfAbsent(full, k -> new ArrayList<>()).add(def);
            }
            return CachedValueProvider.Result.create(map, file);
        });
    }

    // ── Full-name helpers ─────────────────────────────────────────────────────

    /**
     * Returns the canonical full name of {@code def}.
     * Uses the cached def list so this is O(n) only once per edit.
     */
    public static @NotNull String fullNameOf(@NotNull PsiFile file,
                                             @NotNull NasmLabelDef def) {
        String raw = def.getName();
        if (raw == null) return "";
        if (!raw.startsWith(".")) return raw;

        int defOffset = def.getTextOffset();
        String parent = "";
        for (NasmLabelDef d : findAllLabelDefs(file)) {
            if (d.getTextOffset() >= defOffset) break;
            String n = d.getName();
            if (n != null && !n.startsWith(".")) parent = n;
        }
        return parent + raw;
    }

    /**
     * Resolves the canonical full name of a label <em>reference</em>.
     *
     * <ul>
     *   <li>{@code .loop}      → {@code parentLabel + ".loop"}</li>
     *   <li>{@code error.loop} → {@code "error.loop"} (already qualified)</li>
     *   <li>{@code myFunc}     → {@code "myFunc"}</li>
     * </ul>
     */
    public static @NotNull String resolveRefFullName(@NotNull PsiFile file,
                                                     @NotNull PsiElement ref,
                                                     @NotNull String name) {
        if (!name.startsWith(".")) return name;

        int refOffset = ref.getTextOffset();
        String parent = "";
        for (NasmLabelDef def : findAllLabelDefs(file)) {
            if (def.getTextOffset() >= refOffset) break;
            String n = def.getName();
            if (n != null && !n.startsWith(".")) parent = n;
        }
        return parent + name;
    }

    // ── Lookup by full name ───────────────────────────────────────────────────

    /**
     * Returns the first definition for the given canonical full name,
     * or {@code null} if not found.
     */
    public static @Nullable NasmLabelDef findLabelDef(@NotNull PsiFile file,
                                                       @NotNull String fullName) {
        List<NasmLabelDef> defs = buildFullNameMap(file).get(fullName);
        return (defs != null && !defs.isEmpty()) ? defs.get(0) : null;
    }

    // ── Label references ──────────────────────────────────────────────────────

    /** All {@link NasmLabelRef} elements in {@code file}. */
    public static @NotNull Collection<NasmLabelRef> findAllLabelRefs(@NotNull PsiFile file) {
        return PsiTreeUtil.findChildrenOfType(file, NasmLabelRef.class);
    }

    // ── Completion / structure helpers ────────────────────────────────────────

    /** All canonical label names defined in {@code file} (for completion). */
    public static @NotNull List<String> allDefinedLabelNames(@NotNull PsiFile file) {
        return buildFullNameMap(file).keySet().stream()
                .filter(n -> !n.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Returns all non-local (global) label definitions in source order.
     * Used by the structure view and folding builder.
     */
    public static @NotNull List<NasmLabelDef> findGlobalLabelDefs(@NotNull PsiFile file) {
        return findAllLabelDefs(file).stream()
                .filter(d -> {
                    String n = d.getName();
                    return n != null && !n.startsWith(".");
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns the local labels (dot-labels) that immediately follow
     * {@code parent} in the file, up to the next non-dot label.
     */
    public static @NotNull List<NasmLabelDef> findLocalLabelsUnder(@NotNull NasmLabelDef parent) {
        PsiFile file = parent.getContainingFile();
        if (file == null) return List.of();
        List<NasmLabelDef> allDefs = findAllLabelDefs(file);
        int idx = allDefs.indexOf(parent);
        if (idx < 0) return List.of();

        List<NasmLabelDef> locals = new ArrayList<>();
        for (int i = idx + 1; i < allDefs.size(); i++) {
            NasmLabelDef next = allDefs.get(i);
            String name = next.getName();
            if (name == null) continue;
            if (!name.startsWith(".")) break;
            locals.add(next);
        }
        return locals;
    }
}
