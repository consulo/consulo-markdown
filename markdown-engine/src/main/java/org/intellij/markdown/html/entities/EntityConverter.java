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
package org.intellij.markdown.html.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EntityConverter {
    private EntityConverter() {
    }

    private static final String ESCAPE_ALLOWED_STRING = "!\"#\\$%&'\\(\\)\\*\\+,\\-.\\/:;<=>\\ ?@\\[\\\\\\]\\^_`\\{\\|\\}~";

    private static final Map<Character, String> REPLACEMENTS;

    static {
        REPLACEMENTS = new HashMap<>();
        REPLACEMENTS.put('"', "&quot;");
        REPLACEMENTS.put('&', "&amp;");
        REPLACEMENTS.put('<', "&lt;");
        REPLACEMENTS.put('>', "&gt;");
    }

    private static final Pattern REGEX = Pattern.compile(
            "&(?:([a-zA-Z0-9]+)|#([0-9]{1,8})|#[xX]([a-fA-F0-9]{1,8}));|([\"&<>])"
    );

    private static final Pattern REGEX_ESCAPES = Pattern.compile(
            REGEX.pattern() + "|\\\\([" + ESCAPE_ALLOWED_STRING + "])"
    );

    public static String replaceEntities(CharSequence text, boolean processEntities, boolean processEscapes) {
        Pattern pattern = processEscapes ? REGEX_ESCAPES : REGEX;
        Matcher matcher = pattern.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String replacement;
            // Group 5: escaped character (only present in REGEX_ESCAPES)
            if (matcher.groupCount() >= 5 && matcher.group(5) != null) {
                char ch = matcher.group(5).charAt(0);
                String rep = REPLACEMENTS.get(ch);
                replacement = rep != null ? rep : String.valueOf(ch);
            }
            // Group 4: special HTML character
            else if (matcher.group(4) != null) {
                char ch = matcher.group(4).charAt(0);
                String rep = REPLACEMENTS.get(ch);
                replacement = rep != null ? rep : matcher.group();
            }
            // Groups 1-3: entity references
            else {
                Integer code;
                if (!processEntities) {
                    code = null;
                } else if (matcher.group(1) != null) {
                    code = Entities.MAP.get(matcher.group());
                } else if (matcher.group(2) != null) {
                    code = Integer.parseInt(matcher.group(2));
                } else if (matcher.group(3) != null) {
                    code = Integer.parseInt(matcher.group(3), 16);
                } else {
                    code = null;
                }

                if (code != null) {
                    char ch = (char) code.intValue();
                    String rep = REPLACEMENTS.get(ch);
                    replacement = rep != null ? rep : String.valueOf(ch);
                } else {
                    replacement = "&amp;" + matcher.group().substring(1);
                }
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
