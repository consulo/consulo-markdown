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
package org.intellij.markdown.html;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class CommonDefs {
    private CommonDefs() {
    }

    private static final int PUNCTUATION_MASK = (1 << Character.DASH_PUNCTUATION)
            | (1 << Character.START_PUNCTUATION)
            | (1 << Character.END_PUNCTUATION)
            | (1 << Character.CONNECTOR_PUNCTUATION)
            | (1 << Character.OTHER_PUNCTUATION)
            | (1 << Character.INITIAL_QUOTE_PUNCTUATION)
            | (1 << Character.FINAL_QUOTE_PUNCTUATION)
            | (1 << Character.MATH_SYMBOL);

    public static String resolveToStringSafe(URI uri, String str) {
        try {
            return uri.resolve(str).toString();
        } catch (Throwable e) {
            return str;
        }
    }

    public static boolean isWhitespace(char ch) {
        return ch == 0 || Character.isSpaceChar(ch) || Character.isWhitespace(ch);
    }

    public static boolean isPunctuation(char ch) {
        return isAsciiPunctuationFix(ch) || ((PUNCTUATION_MASK >>> Character.getType(ch)) & 1) != 0;
    }

    private static boolean isAsciiPunctuationFix(char ch) {
        // the ones which are not covered by a more general check
        return ch == '$' || ch == '^' || ch == '`';
    }

    public static String urlEncode(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }
}
