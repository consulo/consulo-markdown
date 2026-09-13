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
package org.intellij.markdown.flavours.gfm;

import consulo.language.ast.IElementType;
import org.intellij.markdown.MarkdownElementType;

public final class GFMElementTypes {
    private GFMElementTypes() {
    }

    public static final IElementType STRIKETHROUGH = new MarkdownElementType("STRIKETHROUGH");

    public static final IElementType TABLE = new MarkdownElementType("TABLE");

    public static final IElementType HEADER = new MarkdownElementType("HEADER");

    public static final IElementType ROW = new MarkdownElementType("ROW");

    public static final IElementType INLINE_MATH = new MarkdownElementType("INLINE_MATH");

    public static final IElementType BLOCK_MATH = new MarkdownElementType("BLOCK_MATH");
}
