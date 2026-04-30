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

package com.rexme.plugins.nasm.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.rexme.plugins.nasm.highlighting.NasmSyntaxHighlighter;
import com.rexme.plugins.nasm.psi.*;
import com.rexme.plugins.nasm.util.NasmUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Semantic annotator for NASM assembly.
 *
 * <h3>Responsibilities</h3>
 * <ol>
 *   <li><b>Label colouring</b> — {@link NasmLabelDef} → LABEL_DEFINITION,
 *       {@link NasmLabelRef} → LABEL_REFERENCE.</li>
 *   <li><b>Duplicate-label error</b> — uses <em>fully-qualified</em> names so
 *       {@code funcA.done} and {@code funcB.done} are NOT considered duplicates.</li>
 *   <li><b>Undefined-label error</b> — unresolved refs are underlined red;
 *       {@code extern}/NASM builtins/macro-defined names are excluded.</li>
 *   <li><b>Instruction operand-count error</b> — e.g. {@code mov rax} (missing
 *       second operand) is flagged.</li>
 *   <li><b>Invalid label-name warning</b>.</li>
 * </ol>
 */
public final class NasmAnnotator implements Annotator {

    // ── NASM built-in special symbols ─────────────────────────────────────────
    private static final Set<String> NASM_BUILTINS = Set.of(
            // UTF string helpers
            "__utf16__", "__utf16le__", "__utf16be__",
            "__utf32__", "__utf32le__", "__utf32be__",
            // Float constants
            "__float8__", "__float16__", "__bfloat16__",
            "__float32__", "__float64__", "__float80m__", "__float80e__",
            "__float128l__", "__float128h__",
            "__float_inf__", "__float_ninf__", "__float_nan__",
            "__float_qnan__", "__float_snan__", "__float_nsnan__",
            // Compile-time macros
            "__BITS__", "__DATE__", "__DATE_NUM__", "__TIME__", "__TIME_NUM__",
            "__POSIX_TIME__", "__FILE__", "__LINE__", "__PASS__",
            "__OUTPUT_FORMAT__", "__DEBUG_FORMAT__", "__SECT__",
            "__NASM_MAJOR__", "__NASM_MINOR__", "__NASM_SUBMINOR__",
            "__NASM_VERSION_ID__", "__NASM_VER__",
            // Standard section names used as labels in some styles
            "__bss_start", "_end", "__end__", "__data_start"
    );

    // ── Instruction operand-count table ──────────────────────────────────────
    // int[]{min, max}
    private static final Map<String, int[]> OPERAND_COUNTS = new HashMap<>();
    static {
        // 2 operands
        for (String s : new String[]{
                "mov","add","sub","and","or","xor","cmp","test","lea",
                "adc","sbb","xchg","xadd","bsf","bsr","bt","btc","btr","bts",
                "movsx","movsxd","movzx","movbe",
                "cmova","cmovae","cmovb","cmovbe","cmove","cmovg","cmovge",
                "cmovl","cmovle","cmovne","cmovno","cmovnp","cmovns","cmovnz",
                "cmovo","cmovp","cmovs","cmovz","cmovna","cmovnae","cmovnb",
                "cmovnbe","cmovnc","cmovng","cmovnge","cmovnl","cmovnle",
                "cmovpe","cmovpo","out"}) {
            OPERAND_COUNTS.put(s, new int[]{2, 2});
        }
        // 1 operand
        for (String s : new String[]{
                "push","pop","inc","dec","not","neg","mul","div","idiv","bswap",
                "jmp","call",
                "ja","jae","jb","jbe","jc","je","jg","jge","jl","jle",
                "jna","jnae","jnb","jnbe","jnc","jne","jng","jnge","jnl","jnle",
                "jno","jnp","jns","jnz","jo","jp","jpe","jpo","js","jz",
                "jcxz","jecxz","jrcxz",
                "loop","loope","loopne","loopnz","loopz",
                "seta","setae","setb","setbe","setc","sete","setg","setge",
                "setl","setle","setna","setnae","setnb","setnbe","setnc","setne",
                "setng","setnge","setnl","setnle","setno","setnp","setns","setnz",
                "seto","setp","setpe","setpo","sets","setz",
                "invlpg","verr","verw","lldt","ltr","sldt","str","lgdt","lidt",
                "sgdt","sidt","lmsw","smsw"}) {
            OPERAND_COUNTS.put(s, new int[]{1, 1});
        }
        // 0 operands
        for (String s : new String[]{
                "nop","hlt","syscall","sysenter","sysexit","sysret",
                "clc","stc","cmc","cld","std","cli","sti","wait","pause",
                "cdq","cdqe","cbw","cwde","cwd","cqo",
                "pushf","pushfd","pushfq","popf","popfd","popfq",
                "pusha","pushad","popa","popad","leave",
                "cpuid","rdtsc","rdtscp","ud2","mfence","sfence","lfence",
                "swapgs","int3","iret","iretd","iretq","xlat","xlatb",
                "rdmsr","wrmsr","rdpmc","invd","wbinvd","clts","rsm",
                "lahf","sahf","daa","das","aaa","aas","stmxcsr","vzeroall","vzeroupper", "ret"}) {
            OPERAND_COUNTS.put(s, new int[]{0, 0});
        }
        // Variable / special
        OPERAND_COUNTS.put("imul", new int[]{1, 3});
        OPERAND_COUNTS.put("in",   new int[]{2, 2});
        OPERAND_COUNTS.put("int",  new int[]{1, 1});
        OPERAND_COUNTS.put("retf", new int[]{0, 1});
        OPERAND_COUNTS.put("retn", new int[]{0, 1});
        for (String s : new String[]{"shl","shr","sal","sar","rol","ror","rcl","rcr","shld","shrd"}) {
            OPERAND_COUNTS.put(s, new int[]{1, 2});
        }
        for (String s : new String[]{"enter","cmpxchg","cmpxchg8b","cmpxchg16b"}) {
            OPERAND_COUNTS.put(s, new int[]{2, 2});
        }
    }

    // ── Valid NASM identifier pattern ─────────────────────────────────────────
    private static final Pattern VALID_LABEL = Pattern.compile("[a-zA-Z_.@?][a-zA-Z0-9_.@?#$]*");

    // ── Preproc name extractors  (%define NAME / %macro NAME) ─────────────────
    private static final Pattern PREPROC_DEFINE =
            Pattern.compile("%(?:define|xdefine|assign|idefine|ixdefine)\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PREPROC_MACRO =
            Pattern.compile("%(?:i?macro)\\s+(\\S+)", Pattern.CASE_INSENSITIVE);

    // ── Annotator entry point ─────────────────────────────────────────────────

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {

        // ── 1. Label definition ────────────────────────────────────────────────
        if (element.getNode().getElementType() == NasmElementTypes.LABEL_DEF) {
            applyColor(element, NasmSyntaxHighlighter.LABEL_DEFINITION, holder);
            if (element instanceof NasmLabelDef labelDef) checkLabelDef(labelDef, holder);
            return;
        }

        // ── 2. Label reference ─────────────────────────────────────────────────
        if (element.getNode().getElementType() == NasmElementTypes.LABEL_REF) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(element)
                    .enforcedTextAttributes(NasmSyntaxHighlighter.LABEL_REFERENCE.getDefaultAttributes())
                    .create();

            checkLabelRef(element, holder);
            return;
        }

        // ── 3. Instruction statement ───────────────────────────────────────────
        if (element.getNode().getElementType() == NasmElementTypes.INSTRUCTION_STMT) {
            checkInstructionOperands(element, holder);
        }
    }

    // ── Label definition checks ───────────────────────────────────────────────

    private void checkLabelDef(@NotNull NasmLabelDef def, @NotNull AnnotationHolder holder) {
        PsiFile file = def.getContainingFile();
        if (file == null) return;

        String raw = def.getName();
        if (raw == null || raw.isEmpty()) return;

        // 1a. Invalid label name
        if (!VALID_LABEL.matcher(raw).matches()) {
            holder.newAnnotation(HighlightSeverity.WARNING,
                    "Invalid label name: '" + raw + "'")
                    .range(def).create();
        }

        // 1b. Duplicate definition — use FULLY QUALIFIED name so that
        //     funcA.done and funcB.done are treated as different labels.
        String fullName = NasmUtil.fullNameOf(file, def);
        List<NasmLabelDef> all = NasmUtil.buildFullNameMap(file).getOrDefault(fullName, List.of());
        if (all.size() > 1) {
            holder.newAnnotation(HighlightSeverity.ERROR,
                    "Duplicate label definition: '" + fullName + "'")
                    .range(def).create();
        }
    }

    // ── Label reference checks ────────────────────────────────────────────────

    private void checkLabelRef(@NotNull PsiElement element,
                               @NotNull AnnotationHolder holder) {
        ASTNode idNode = element.getNode().findChildByType(NasmTokenTypes.IDENTIFIER);
        if (idNode == null) return;
        String name = idNode.getText();
        PsiFile file = element.getContainingFile();
        if (file == null) return;

        String fullName = NasmUtil.resolveRefFullName(file, element, name);
        Set<String> known = collectKnownExternals(file);

        if (!known.contains(name) && !known.contains(fullName)
                && NasmUtil.findLabelDef(file, fullName) == null) {
            holder.newAnnotation(HighlightSeverity.ERROR,
                            "Undefined label: '" + fullName + "'")
                    .range(element).create();
        }
    }

    // ── Instruction operand count check ───────────────────────────────────────

    private void checkInstructionOperands(@NotNull PsiElement stmt,
                                          @NotNull AnnotationHolder holder) {
        ASTNode stmtNode = stmt.getNode();

        // Find the last INSTRUCTION child (skip rep/lock prefixes)
        ASTNode lastInstr = null;
        ASTNode c = stmtNode.getFirstChildNode();
        while (c != null) {
            if (c.getElementType() == NasmTokenTypes.INSTRUCTION) lastInstr = c;
            c = c.getTreeNext();
        }
        if (lastInstr == null) return;

        String mnemonic = lastInstr.getText().toLowerCase();
        int[] range = OPERAND_COUNTS.get(mnemonic);
        if (range == null) return;

        // Count only DIRECT OPERAND children that appear after the mnemonic node
        int operandCount = 0;
        boolean past = false;
        c = stmtNode.getFirstChildNode();
        while (c != null) {
            if (c == lastInstr)                                  past = true;
            else if (past && c.getElementType() == NasmElementTypes.OPERAND) operandCount++;
            c = c.getTreeNext();
        }

        if (operandCount < range[0]) {
            holder.newAnnotation(HighlightSeverity.ERROR,
                            "'" + mnemonic + "' expects " + range[0]
                                    + " operand(s) but got " + operandCount)
                    .range(stmt).create();
        } else if (operandCount > range[1]) {
            holder.newAnnotation(HighlightSeverity.ERROR,
                            "'" + mnemonic + "' expects at most " + range[1]
                                    + " operand(s) but got " + operandCount)
                    .range(stmt).create();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void applyColor(@NotNull PsiElement element,
                                   @NotNull TextAttributesKey key,
                                   @NotNull AnnotationHolder holder) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element).textAttributes(key).create();
    }

    /**
     * Collects all symbol names that are valid without a file-local definition:
     * <ul>
     *   <li>Targets of {@code extern} / {@code common} directives</li>
     *   <li>NASM built-in symbols (e.g. {@code __utf16__})</li>
     *   <li>Names defined by {@code %define} / {@code %macro} / {@code %assign}</li>
     * </ul>
     */
    private static @NotNull Set<String> collectKnownExternals(@NotNull PsiFile file) {
        Set<String> names = new HashSet<>(NASM_BUILTINS);

        for (PsiElement stmt : PsiTreeUtil.findChildrenOfType(file, NasmCompositeElementImpl.class)) {
            com.intellij.psi.tree.IElementType type = stmt.getNode().getElementType();

            if (type == NasmElementTypes.EXPORT_DECL) {
                // extern / common / global  → refs inside are all valid cross-file symbols
                PsiTreeUtil.findChildrenOfType(stmt, NasmLabelRef.class)
                        .forEach(r -> { if (r.getName() != null) names.add(r.getName()); });

            } else if (type == NasmElementTypes.PREPROC_STMT) {
                // Extract names from %define / %macro / %assign
                String text = stmt.getText();
                Matcher m = PREPROC_DEFINE.matcher(text);
                if (m.find()) names.add(extractSimpleName(m.group(1)));
                Matcher m2 = PREPROC_MACRO.matcher(text);
                if (m2.find()) names.add(extractSimpleName(m2.group(1)));
            }
        }
        return names;
    }

    /** Strips a trailing {@code (…)} from a macro name, e.g. {@code myMacro(x)} → {@code myMacro}. */
    private static String extractSimpleName(String s) {
        int p = s.indexOf('(');
        return p > 0 ? s.substring(0, p) : s;
    }
}
