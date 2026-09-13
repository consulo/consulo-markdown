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

public final class MarkdownElementTypes {
    private MarkdownElementTypes() {
    }

    public static final IElementType MARKDOWN_FILE = new MarkdownElementType("MARKDOWN_FILE");
    public static final IElementType UNORDERED_LIST = new MarkdownElementType("UNORDERED_LIST");
    public static final IElementType ORDERED_LIST = new MarkdownElementType("ORDERED_LIST");
    public static final IElementType LIST_ITEM = new MarkdownElementType("LIST_ITEM");
    public static final IElementType BLOCK_QUOTE = new MarkdownElementType("BLOCK_QUOTE");
    public static final IElementType CODE_FENCE = new MarkdownElementType("CODE_FENCE");
    public static final IElementType CODE_BLOCK = new MarkdownElementType("CODE_BLOCK");
    public static final IElementType CODE_SPAN = new MarkdownElementType("CODE_SPAN");
    public static final IElementType HTML_BLOCK = new MarkdownElementType("HTML_BLOCK");
    public static final IElementType PARAGRAPH = new MarkdownLazyElementType("PARAGRAPH", true);
    public static final IElementType EMPH = new MarkdownElementType("EMPH");
    public static final IElementType STRONG = new MarkdownElementType("STRONG");

    public static final IElementType LINK_DEFINITION = new MarkdownElementType("LINK_DEFINITION");
    public static final IElementType LINK_LABEL = new MarkdownElementType("LINK_LABEL", true);
    public static final IElementType LINK_DESTINATION = new MarkdownElementType("LINK_DESTINATION", true);
    public static final IElementType LINK_TITLE = new MarkdownElementType("LINK_TITLE", true);
    public static final IElementType LINK_TEXT = new MarkdownElementType("LINK_TEXT", true);
    public static final IElementType INLINE_LINK = new MarkdownElementType("INLINE_LINK");
    public static final IElementType FULL_REFERENCE_LINK = new MarkdownElementType("FULL_REFERENCE_LINK");
    public static final IElementType SHORT_REFERENCE_LINK = new MarkdownElementType("SHORT_REFERENCE_LINK");
    public static final IElementType IMAGE = new MarkdownElementType("IMAGE");

    public static final IElementType AUTOLINK = new MarkdownElementType("AUTOLINK");

    public static final IElementType SETEXT_1 = new MarkdownElementType("SETEXT_1");
    public static final IElementType SETEXT_2 = new MarkdownElementType("SETEXT_2");

    public static final IElementType ATX_1 = new MarkdownElementType("ATX_1");
    public static final IElementType ATX_2 = new MarkdownElementType("ATX_2");
    public static final IElementType ATX_3 = new MarkdownElementType("ATX_3");
    public static final IElementType ATX_4 = new MarkdownElementType("ATX_4");
    public static final IElementType ATX_5 = new MarkdownElementType("ATX_5");
    public static final IElementType ATX_6 = new MarkdownElementType("ATX_6");
}
