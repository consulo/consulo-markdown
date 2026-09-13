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
package org.intellij.markdown;

import consulo.language.ast.IElementType;

public class MarkdownTokenTypes {
    public static final IElementType TEXT = new MarkdownElementType("TEXT", true);
    public static final IElementType CODE_LINE = new MarkdownElementType("CODE_LINE", true);
    public static final IElementType BLOCK_QUOTE = new MarkdownElementType("BLOCK_QUOTE", true);
    public static final IElementType HTML_BLOCK_CONTENT = new MarkdownElementType("HTML_BLOCK_CONTENT", true);

    public static final IElementType SINGLE_QUOTE = new MarkdownElementType("'", true);
    public static final IElementType DOUBLE_QUOTE = new MarkdownElementType("\"", true);
    public static final IElementType LPAREN = new MarkdownElementType("(", true);
    public static final IElementType RPAREN = new MarkdownElementType(")", true);
    public static final IElementType LBRACKET = new MarkdownElementType("[", true);
    public static final IElementType RBRACKET = new MarkdownElementType("]", true);
    public static final IElementType LT = new MarkdownElementType("<", true);
    public static final IElementType GT = new MarkdownElementType(">", true);
    public static final IElementType COLON = new MarkdownElementType(":", true);
    public static final IElementType EXCLAMATION_MARK = new MarkdownElementType("!", true);

    public static final IElementType HARD_LINE_BREAK = new MarkdownElementType("BR", true);
    public static final IElementType EOL = new MarkdownElementType("EOL", true);
    public static final IElementType LINK_ID = new MarkdownElementType("LINK_ID", true);
    public static final IElementType ATX_HEADER = new MarkdownElementType("ATX_HEADER", true);
    public static final IElementType ATX_CONTENT = new MarkdownLazyElementType("ATX_CONTENT", true);
    public static final IElementType SETEXT_1 = new MarkdownElementType("SETEXT_1", true);
    public static final IElementType SETEXT_2 = new MarkdownElementType("SETEXT_2", true);
    public static final IElementType SETEXT_CONTENT = new MarkdownLazyElementType("SETEXT_CONTENT", true);
    public static final IElementType EMPH = new MarkdownElementType("EMPH", true);
    public static final IElementType BACKTICK = new MarkdownElementType("BACKTICK", true);
    public static final IElementType ESCAPED_BACKTICKS = new MarkdownElementType("ESCAPED_BACKTICKS", true);
    public static final IElementType LIST_BULLET = new MarkdownElementType("LIST_BULLET", true);
    public static final IElementType URL = new MarkdownElementType("URL", true);
    public static final IElementType HORIZONTAL_RULE = new MarkdownElementType("HORIZONTAL_RULE", true);
    public static final IElementType LIST_NUMBER = new MarkdownElementType("LIST_NUMBER", true);
    public static final IElementType FENCE_LANG = new MarkdownElementType("FENCE_LANG", true);
    public static final IElementType CODE_FENCE_START = new MarkdownElementType("CODE_FENCE_START", true);
    public static final IElementType CODE_FENCE_CONTENT = new MarkdownElementType("CODE_FENCE_CONTENT", true);
    public static final IElementType CODE_FENCE_END = new MarkdownElementType("CODE_FENCE_END", true);
    public static final IElementType LINK_TITLE = new MarkdownElementType("LINK_TITLE", true);
    public static final IElementType AUTOLINK = new MarkdownElementType("AUTOLINK", true);
    public static final IElementType EMAIL_AUTOLINK = new MarkdownElementType("EMAIL_AUTOLINK", true);
    public static final IElementType HTML_TAG = new MarkdownElementType("HTML_TAG", true);
    public static final IElementType BAD_CHARACTER = new MarkdownElementType("BAD_CHARACTER", true);

    public static final IElementType WHITE_SPACE = new MarkdownElementType("WHITE_SPACE", true) {
        @Override
        public String toString() {
            return "WHITE_SPACE";
        }
    };
}
