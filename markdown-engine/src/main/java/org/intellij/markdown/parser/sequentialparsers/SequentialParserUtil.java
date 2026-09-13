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
package org.intellij.markdown.parser.sequentialparsers;

import org.intellij.markdown.MarkdownTokenTypes;

import java.util.ArrayList;
import java.util.List;

public final class SequentialParserUtil {
    private SequentialParserUtil() {
    }

    private static final int PUNCTUATION_MASK =
            (1 << Character.DASH_PUNCTUATION)
            | (1 << Character.START_PUNCTUATION)
            | (1 << Character.END_PUNCTUATION)
            | (1 << Character.CONNECTOR_PUNCTUATION)
            | (1 << Character.OTHER_PUNCTUATION)
            | (1 << Character.INITIAL_QUOTE_PUNCTUATION)
            | (1 << Character.FINAL_QUOTE_PUNCTUATION)
            | (1 << Character.MATH_SYMBOL);

    public static boolean isWhitespace(TokensCache.Iterator info, int lookup) {
        return isWhitespace(info.charLookup(lookup));
    }

    public static boolean isPunctuation(TokensCache.Iterator info, int lookup) {
        return isPunctuation(info.charLookup(lookup));
    }

    public static boolean isWhitespace(char c) {
        return c == 0 || Character.isSpaceChar(c) || Character.isWhitespace(c);
    }

    public static boolean isPunctuation(char c) {
        return isAsciiPunctuationFix(c) || ((PUNCTUATION_MASK >>> Character.getType(c)) & 1) != 0;
    }

    private static boolean isAsciiPunctuationFix(char c) {
        return c == '$' || c == '^' || c == '`';
    }

    
    public static List<IntRange> filterBlockquotes(TokensCache tokensCache, IntRange textRange) {
        List<IntRange> result = new ArrayList<>();
        int lastStart = textRange.getFirst();

        int R = textRange.getLast();
        for (int i = lastStart; i <= R - 1; i++) {
            if (tokensCache.new Iterator(i).getType() == MarkdownTokenTypes.BLOCK_QUOTE) {
                if (lastStart < i) {
                    result.add(new IntRange(lastStart, i - 1));
                }
                lastStart = i + 1;
            }
        }
        if (lastStart < R) {
            result.add(new IntRange(lastStart, R));
        }
        return result;
    }
}
