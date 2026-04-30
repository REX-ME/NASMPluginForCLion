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

package com.rexme.plugins.nasm.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import com.rexme.plugins.nasm.psi.NasmTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Hand-written lexer for NASM assembly.
 * Stateless (state == 0 always) — safe for incremental re-lexing.
 */
public final class NasmLexer extends LexerBase {

    // ── x86/x86-64 register names ────────────────────────────────────────────
    private static final Set<String> REGISTERS = Set.of(
            // 64-bit GP
            "rax","rbx","rcx","rdx","rsi","rdi","rsp","rbp",
            "r8","r9","r10","r11","r12","r13","r14","r15",
            // 32-bit GP
            "eax","ebx","ecx","edx","esi","edi","esp","ebp",
            "r8d","r9d","r10d","r11d","r12d","r13d","r14d","r15d",
            // 16-bit GP
            "ax","bx","cx","dx","si","di","sp","bp",
            "r8w","r9w","r10w","r11w","r12w","r13w","r14w","r15w",
            // 8-bit
            "al","bl","cl","dl","ah","bh","ch","dh",
            "sil","dil","spl","bpl",
            "r8b","r9b","r10b","r11b","r12b","r13b","r14b","r15b",
            // Segment
            "cs","ds","es","fs","gs","ss",
            // Control / Debug
            "cr0","cr2","cr3","cr4","cr8",
            "dr0","dr1","dr2","dr3","dr6","dr7",
            // MMX
            "mm0","mm1","mm2","mm3","mm4","mm5","mm6","mm7",
            // XMM 0-15
            "xmm0","xmm1","xmm2","xmm3","xmm4","xmm5","xmm6","xmm7",
            "xmm8","xmm9","xmm10","xmm11","xmm12","xmm13","xmm14","xmm15",
            // YMM 0-15
            "ymm0","ymm1","ymm2","ymm3","ymm4","ymm5","ymm6","ymm7",
            "ymm8","ymm9","ymm10","ymm11","ymm12","ymm13","ymm14","ymm15",
            // ZMM 0-15 (AVX-512)
            "zmm0","zmm1","zmm2","zmm3","zmm4","zmm5","zmm6","zmm7",
            "zmm8","zmm9","zmm10","zmm11","zmm12","zmm13","zmm14","zmm15",
            // Special purpose
            "rip","eip","ip","rflags","eflags","flags",
            "st","st0","st1","st2","st3","st4","st5","st6","st7"
    );

    // ── All NASM instructions ────────────────────────────────────────────────
    private static final Set<String> INSTRUCTIONS = Set.of(
            "aaa","aad","aam","aas","adc","add","addpd","addps","addsd","addss",
            "and","andpd","andps","andnpd","andnps","arpl","bound","bsf","bsr",
            "bswap","bt","btc","btr","bts",
            "call","cbw","cdq","cdqe","clc","cld","cli","clts","cmc",
            "cmova","cmovae","cmovb","cmovbe","cmovc","cmove","cmovg","cmovge",
            "cmovl","cmovle","cmovna","cmovnae","cmovnb","cmovnbe","cmovnc",
            "cmovne","cmovng","cmovnge","cmovnl","cmovnle","cmovno","cmovnp",
            "cmovns","cmovnz","cmovo","cmovp","cmovpe","cmovpo","cmovs","cmovz",
            "cmp","cmppd","cmpps","cmpsb","cmpsd","cmpss","cmpsw",
            "cmpxchg","cmpxchg8b","cmpxchg16b","cpuid","cqo","cwd","cwde",
            "daa","das","dec","div","divpd","divps","divsd","divss",
            "enter","hlt","idiv","imul","in","inc","ins","insb","insd","insw",
            "int","int3","into","invd","invlpg",
            "iret","iretd","iretq",
            "ja","jae","jb","jbe","jc","jcxz","je","jecxz","jg","jge",
            "jl","jle","jmp","jna","jnae","jnb","jnbe","jnc","jne","jng",
            "jnge","jnl","jnle","jno","jnp","jns","jnz","jo","jp","jpe",
            "jpo","jrcxz","js","jz",
            "lahf","lar","ldmxcsr","lds","lea","leave","les","lfence",
            "lfs","lgdt","lgs","lidt","lldt","lmsw","lock",
            "lods","lodsb","lodsd","lodsq","lodsw",
            "loop","loope","loopne","loopnz","loopz","lsl","lss","ltr",
            "maskmovdqu","maskmovq","maxpd","maxps","maxsd","maxss",
            "mfence","minpd","minps","minsd","minss",
            "mov","movapd","movaps","movbe","movd","movddup","movdq2q",
            "movdqa","movdqu","movhlps","movhpd","movhps","movlhps","movlpd",
            "movlps","movmskpd","movmskps","movntdq","movntdqa","movnti",
            "movntpd","movntps","movntq","movq","movq2dq",
            "movs","movsb","movsd","movshdup","movsldup","movsq","movss","movsw",
            "movsx","movsxd","movupd","movups","movzx",
            "mul","mulpd","mulps","mulsd","mulss","neg","nop","not",
            "or","orpd","orps","out","outs","outsb","outsd","outsw",
            "pop","popa","popad","popcnt","popf","popfd","popfq",
            "push","pusha","pushad","pushf","pushfd","pushfq",
            "rcl","rcpps","rcpss","rcr","rdmsr","rdpmc","rdtsc","rdtscp",
            "rep","repe","repne","repnz","repz","ret","retf","retn",
            "rol","ror","roundpd","roundps","roundsd","roundss",
            "rsm","rsqrtps","rsqrtss",
            "sahf","sal","sar","sbb","scas","scasb","scasd","scasq","scasw",
            "seta","setae","setb","setbe","setc","sete","setg","setge",
            "setl","setle","setna","setnae","setnb","setnbe","setnc","setne",
            "setng","setnge","setnl","setnle","setno","setnp","setns","setnz",
            "seto","setp","setpe","setpo","sets","setz",
            "sfence","sgdt","shl","shld","shr","shrd","shufpd","shufps",
            "sidt","sldt","smsw","sqrtpd","sqrtps","sqrtsd","sqrtss",
            "stc","std","sti","stmxcsr","stos","stosb","stosd","stosq","stosw",
            "str","sub","subpd","subps","subsd","subss",
            "swapgs","syscall","sysenter","sysexit","sysret",
            "test","tzcnt","ucomisd","ucomiss","ud2",
            "unpckhpd","unpckhps","unpcklpd","unpcklps",
            "verr","verw","vmovdqa","vmovdqu","vmovaps","vmovapd",
            "vmovups","vmovupd","vzeroupper","vzeroall",
            "wait","wbinvd","wrmsr",
            "xadd","xchg","xgetbv","xlat","xlatb","xor","xorpd","xorps",
            // SSE/AVX misc
            "palignr","pabsb","pabsd","pabsw","packssdw","packsswb",
            "packusdw","packuswb","paddb","paddd","paddq","paddsb","paddsw",
            "paddusb","paddusw","paddw","pand","pandn","pause",
            "pavgb","pavgw","pcmpeqb","pcmpeqd","pcmpeqq","pcmpeqw",
            "pcmpestri","pcmpestrm","pcmpgtb","pcmpgtd","pcmpgtq","pcmpgtw",
            "pcmpistri","pcmpistrm","pextrb","pextrd","pextrq","pextrw",
            "phaddd","phaddsw","phaddw","phminposuw","phsubd","phsubsw","phsubw",
            "pinsrb","pinsrd","pinsrq","pinsrw","pmaddubsw","pmaddwd",
            "pmaxsb","pmaxsd","pmaxsw","pmaxub","pmaxud","pmaxuw",
            "pminsb","pminsd","pminsw","pminub","pminud","pminuw",
            "pmovmskb","pmovsxbd","pmovsxbq","pmovsxbw","pmovsxdq",
            "pmovsxwd","pmovsxwq","pmovzxbd","pmovzxbq","pmovzxbw",
            "pmovzxdq","pmovzxwd","pmovzxwq","pmuldq","pmulhrsw","pmulhuw",
            "pmulhw","pmulld","pmullw","pmuludq","por",
            "prefetchnta","prefetcht0","prefetcht1","prefetcht2",
            "psadbw","pshufb","pshufd","pshufhw","pshuflw","pshufw",
            "psignb","psignd","psignw","pslld","pslldq","psllq","psllw",
            "psrad","psraw","psrld","psrldq","psrlq","psrlw",
            "psubb","psubd","psubq","psubsb","psubsw","psubusb","psubusw","psubw",
            "ptest","punpckhbw","punpckhdq","punpckhqdq","punpckhwd",
            "punpcklbw","punpckldq","punpcklqdq","punpcklwd","pxor"
    );

    // ── Size specifiers & memory qualifiers ──────────────────────────────────
    private static final Set<String> SIZE_SPECIFIERS = Set.of(
            "byte","word","dword","qword","tword","oword","yword","zword",
            "tbyte","far","near","short","strict","nosplit","ptr"
    );

    // ── State ────────────────────────────────────────────────────────────────
    private CharSequence myBuffer;
    private int myEnd;
    private int myTokenStart;
    private int myTokenEnd;
    private IElementType myTokenType;

    // ── LexerBase contract ───────────────────────────────────────────────────

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        myBuffer     = buffer;
        myEnd        = endOffset;
        myTokenStart = startOffset;
        myTokenEnd   = startOffset;
        myTokenType  = null;
        advance();
    }

    @Override public int getState()                        { return 0; }
    @Override public @Nullable IElementType getTokenType() { return myTokenType; }
    @Override public int getTokenStart()                   { return myTokenStart; }
    @Override public int getTokenEnd()                     { return myTokenEnd; }
    @Override public @NotNull CharSequence getBufferSequence() { return myBuffer; }
    @Override public int getBufferEnd()                    { return myEnd; }

    @Override
    public void advance() {
        if (myTokenEnd >= myEnd) {
            myTokenType  = null;
            myTokenStart = myEnd;
            return;
        }
        myTokenStart = myTokenEnd;
        int pos = myTokenStart;
        char c  = myBuffer.charAt(pos);

        // ── Whitespace (spaces / tabs / CR) ──────────────────────────────────
        if (c == ' ' || c == '\t' || c == '\r') {
            do { pos++; } while (pos < myEnd && isHorizontalSpace(myBuffer.charAt(pos)));
            finish(pos, NasmTokenTypes.WHITE_SPACE);
            return;
        }

        // ── Newline ───────────────────────────────────────────────────────────
        if (c == '\n') {
            finish(pos + 1, NasmTokenTypes.NEWLINE);
            return;
        }

        // ── Line comment ──────────────────────────────────────────────────────
        if (c == ';') {
            do { pos++; } while (pos < myEnd && myBuffer.charAt(pos) != '\n');
            finish(pos, NasmTokenTypes.COMMENT);
            return;
        }

        // ── String literals  " ' ` ────────────────────────────────────────────
        if (c == '"' || c == '\'' || c == '`') {
            char q = c;
            pos++;
            while (pos < myEnd && myBuffer.charAt(pos) != q && myBuffer.charAt(pos) != '\n') {
                if (myBuffer.charAt(pos) == '\\' && pos + 1 < myEnd) pos++; // escape
                pos++;
            }
            if (pos < myEnd && myBuffer.charAt(pos) == q) pos++;
            finish(pos, NasmTokenTypes.STRING);
            return;
        }

        // ── Numbers starting with digit ───────────────────────────────────────
        if (Character.isDigit(c)) {
            finish(scanNumber(pos), NasmTokenTypes.NUMBER);
            return;
        }

        // ── $ — current position, $$ — section base, $0F — hex literal ───────
        if (c == '$') {
            if (pos + 1 < myEnd && myBuffer.charAt(pos + 1) == '$') {
                finish(pos + 2, NasmTokenTypes.DOLLAR_DOLLAR);
                return;
            }
            if (pos + 1 < myEnd && isHexDigit(myBuffer.charAt(pos + 1))) {
                pos += 2;
                while (pos < myEnd && isHexDigit(myBuffer.charAt(pos))) pos++;
                finish(pos, NasmTokenTypes.NUMBER);
                return;
            }
            finish(pos + 1, NasmTokenTypes.DOLLAR);
            return;
        }

        // ── Preprocessor directives  %define %macro … ─────────────────────────
        if (c == '%') {
            if (pos + 1 < myEnd && (Character.isLetter(myBuffer.charAt(pos + 1))
                    || myBuffer.charAt(pos + 1) == '!' || myBuffer.charAt(pos + 1) == '%')) {
                pos++;
                while (pos < myEnd && isIdentChar(myBuffer.charAt(pos))) pos++;
                finish(pos, NasmTokenTypes.PREPROC_DIRECTIVE);
                return;
            }
            finish(pos + 1, NasmTokenTypes.PERCENT);
            return;
        }

        // ── Identifiers / keywords / mnemonics ────────────────────────────────
        if (Character.isLetter(c) || c == '_' || c == '.' || c == '@' || c == '?') {
            int start = pos++;
            while (pos < myEnd && isIdentChar(myBuffer.charAt(pos))) pos++;
            String text = myBuffer.subSequence(start, pos).toString().toLowerCase();
            finish(pos, classifyIdentifier(text));
            return;
        }

        // ── Two-char operators ────────────────────────────────────────────────
        if (c == '<' && pos + 1 < myEnd && myBuffer.charAt(pos + 1) == '<') {
            finish(pos + 2, NasmTokenTypes.SHL); return;
        }
        if (c == '>' && pos + 1 < myEnd && myBuffer.charAt(pos + 1) == '>') {
            finish(pos + 2, NasmTokenTypes.SHR); return;
        }

        // ── Single-char tokens ────────────────────────────────────────────────
        IElementType tt;
        switch (c) {
            case ',': tt = NasmTokenTypes.COMMA;      break;
            case ':': tt = NasmTokenTypes.COLON;      break;
            case '[': tt = NasmTokenTypes.LBRACKET;   break;
            case ']': tt = NasmTokenTypes.RBRACKET;   break;
            case '(': tt = NasmTokenTypes.LPAREN;     break;
            case ')': tt = NasmTokenTypes.RPAREN;     break;
            case '{': tt = NasmTokenTypes.LBRACE;     break;
            case '}': tt = NasmTokenTypes.RBRACE;     break;
            case '+': tt = NasmTokenTypes.PLUS;       break;
            case '-': tt = NasmTokenTypes.MINUS;      break;
            case '*': tt = NasmTokenTypes.STAR;       break;
            case '/': tt = NasmTokenTypes.SLASH;      break;
            case '~': tt = NasmTokenTypes.TILDE;      break;
            case '^': tt = NasmTokenTypes.CARET;      break;
            case '&': tt = NasmTokenTypes.AMP;        break;
            case '|': tt = NasmTokenTypes.PIPE;       break;
            case '!': tt = NasmTokenTypes.BANG;       break;
            case '<': tt = NasmTokenTypes.LT;         break;
            case '>': tt = NasmTokenTypes.GT;         break;
            default:  tt = NasmTokenTypes.BAD_CHARACTER;
        }
        finish(pos + 1, tt);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void finish(int end, IElementType type) {
        myTokenEnd  = end;
        myTokenType = type;
    }

    private boolean isHorizontalSpace(char c) {
        return c == ' ' || c == '\t' || c == '\r';
    }

    private boolean isIdentChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '@' || c == '?' || c == '#';
    }

    private boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    /** Scan a numeric literal starting at {@code pos}. Returns the end offset. */
    private int scanNumber(int pos) {
        char c = myBuffer.charAt(pos);
        // 0x hex, 0b binary, 0o octal prefixes
        if (c == '0' && pos + 1 < myEnd) {
            char next = myBuffer.charAt(pos + 1);
            if (next == 'x' || next == 'X') {
                pos += 2;
                while (pos < myEnd && isHexDigit(myBuffer.charAt(pos))) pos++;
                return pos;
            }
            if (next == 'b' || next == 'B') {
                int tmp = pos + 2;
                while (tmp < myEnd && (myBuffer.charAt(tmp) == '0' || myBuffer.charAt(tmp) == '1')) tmp++;
                // Only treat as binary if followed by non-ident char
                if (tmp > pos + 2 && (tmp >= myEnd || !isIdentChar(myBuffer.charAt(tmp)))) return tmp;
            }
            if (next == 'o' || next == 'O') {
                pos += 2;
                while (pos < myEnd && myBuffer.charAt(pos) >= '0' && myBuffer.charAt(pos) <= '7') pos++;
                return pos;
            }
        }
        // Decimal or hex body (may have hex digits for h-suffix style)
        while (pos < myEnd && (Character.isDigit(myBuffer.charAt(pos)) || isHexDigit(myBuffer.charAt(pos)))) pos++;
        // h / H  — NASM hex suffix (only if next char is non-ident)
        if (pos < myEnd && (myBuffer.charAt(pos) == 'h' || myBuffer.charAt(pos) == 'H')
                && (pos + 1 >= myEnd || !isIdentChar(myBuffer.charAt(pos + 1)))) {
            return pos + 1;
        }
        // b / o / q / d — base suffixes
        if (pos < myEnd && "boqdBOQD".indexOf(myBuffer.charAt(pos)) >= 0
                && (pos + 1 >= myEnd || !isIdentChar(myBuffer.charAt(pos + 1)))) {
            return pos + 1;
        }
        return pos;
    }

    /** Map a lower-case identifier string to its token type. */
    private IElementType classifyIdentifier(String text) {
        if (REGISTERS.contains(text))       return NasmTokenTypes.REGISTER;
        if (SIZE_SPECIFIERS.contains(text)) return NasmTokenTypes.SIZE_SPECIFIER;

        return switch (text) {
            case "section", "segment"  -> NasmTokenTypes.SECTION_KW;
            case "global"              -> NasmTokenTypes.GLOBAL_KW;
            case "extern"              -> NasmTokenTypes.EXTERN_KW;
            case "common"              -> NasmTokenTypes.COMMON_KW;
            case "db"                  -> NasmTokenTypes.DB_KW;
            case "dw"                  -> NasmTokenTypes.DW_KW;
            case "dd"                  -> NasmTokenTypes.DD_KW;
            case "dq"                  -> NasmTokenTypes.DQ_KW;
            case "dt","do","ddq","dy","dz" -> NasmTokenTypes.DX_KW;
            case "resb"                -> NasmTokenTypes.RESB_KW;
            case "resw"                -> NasmTokenTypes.RESW_KW;
            case "resd"                -> NasmTokenTypes.RESD_KW;
            case "resq"                -> NasmTokenTypes.RESQ_KW;
            case "rest","reso","resy","resz","resdq" -> NasmTokenTypes.RESX_KW;
            case "equ"                 -> NasmTokenTypes.EQU_KW;
            case "times"               -> NasmTokenTypes.TIMES_KW;
            case "bits","use16","use32","use64" -> NasmTokenTypes.BITS_KW;
            case "align","alignb"      -> NasmTokenTypes.ALIGN_KW;
            case "incbin"              -> NasmTokenTypes.INCBIN_KW;
            case "struc","endstruc","istruc","at","iend" -> NasmTokenTypes.STRUC_KW;
            case "absolute"            -> NasmTokenTypes.ABSOLUTE_KW;
            case "default"             -> NasmTokenTypes.DEFAULT_KW;
            case "abs"                 -> NasmTokenTypes.ABS_KW;
            case "rel"                 -> NasmTokenTypes.REL_KW;
            default -> INSTRUCTIONS.contains(text) ? NasmTokenTypes.INSTRUCTION : NasmTokenTypes.IDENTIFIER;
        };
    }
}
