/*
 * Copyright 2016 JetBrains s.r.o.
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.markdown.lexer;

import org.intellij.markdown.MarkdownParsingException;

import java.util.function.IntConsumer;

public final class Compat {
    private static final int MIN_SUPPLEMENTARY_CODE_POINT = 0x010000;
    private static final char MIN_HIGH_SURROGATE = '\uD800';
    private static final char MIN_LOW_SURROGATE = '\uDC00';

    private Compat() {
    }

    public static void forEachCodePoint(CharSequence seq, IntConsumer f) {
        int offset = 0;
        while (offset < seq.length()) {
            int codePoint = codePointAt(seq, offset);
            f.accept(codePoint);
            offset += charCount(codePoint);
        }
    }

    public static String codePointToString(int c) {
        if (charCount(c) == 1) {
            return Character.toString((char) c);
        } else {
            char high = (char) ((c >>> 10) + ((int) MIN_HIGH_SURROGATE - (MIN_SUPPLEMENTARY_CODE_POINT >>> 10)));
            char low = (char) ((c & 0x3ff) + (int) MIN_LOW_SURROGATE);
            return new String(new char[]{high, low});
        }
    }

    public static int offsetByCodePoints(CharSequence seq, int index, int codePointOffset) {
        int length = seq.length();
        if (index < 0 || index > length) {
            throw new IndexOutOfBoundsException();
        }

        int x = index;
        if (codePointOffset >= 0) {
            int i = 0;
            while (x < length && i < codePointOffset) {
                if (Character.isHighSurrogate(seq.charAt(x++)) && x < length &&
                        Character.isLowSurrogate(seq.charAt(x))) {
                    x++;
                }
                i++;
            }
            if (i < codePointOffset) {
                throw new IndexOutOfBoundsException();
            }
        } else {
            int i = codePointOffset;
            while (x > 0 && i < 0) {
                if (Character.isLowSurrogate(seq.charAt(--x)) && x > 0 &&
                        Character.isHighSurrogate(seq.charAt(x - 1))) {
                    x--;
                }
                i++;
            }
            if (i < 0) {
                throw new IndexOutOfBoundsException();
            }
        }
        return x;
    }

    public static int codePointBefore(CharSequence seq, int index) {
        int idx = index;
        char c2 = seq.charAt(--idx);
        if (Character.isLowSurrogate(c2) && idx > 0) {
            char c1 = seq.charAt(--idx);
            if (Character.isHighSurrogate(c1)) {
                return toCodePoint(c1, c2);
            }
        }
        return (int) c2;
    }

    public static int charCount(int ch) {
        return ch >= MIN_SUPPLEMENTARY_CODE_POINT ? 2 : 1;
    }

    public static int toCodePoint(char high, char low) {
        // Optimized form of:
        // return ((high - MIN_HIGH_SURROGATE) << 10)
        //         + (low - MIN_LOW_SURROGATE)
        //         + MIN_SUPPLEMENTARY_CODE_POINT;
        return ((int) high << 10) + (int) low + (MIN_SUPPLEMENTARY_CODE_POINT
                - ((int) Character.MIN_HIGH_SURROGATE << 10)
                - (int) Character.MIN_LOW_SURROGATE);
    }

    public static int codePointAt(CharSequence seq, int index) {
        int idx = index;
        char c1 = seq.charAt(idx);
        if (Character.isHighSurrogate(c1) && ++idx < seq.length()) {
            char c2 = seq.charAt(idx);
            if (Character.isLowSurrogate(c2)) {
                return toCodePoint(c1, c2);
            }
        }
        return (int) c1;
    }

    public static void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new MarkdownParsingException(message);
        }
    }

    public static void assertCondition(boolean condition) {
        assertCondition(condition, "");
    }
}
