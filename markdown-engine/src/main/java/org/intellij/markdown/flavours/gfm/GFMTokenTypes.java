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
import org.intellij.markdown.MarkdownLazyElementType;

public final class GFMTokenTypes {
    private GFMTokenTypes() {
    }

    public static final IElementType TILDE = new MarkdownElementType("~", true);

    public static final IElementType TABLE_SEPARATOR = new MarkdownElementType("TABLE_SEPARATOR", true);

    public static final IElementType GFM_AUTOLINK = new MarkdownElementType("GFM_AUTOLINK", true);

    public static final IElementType CHECK_BOX = new MarkdownElementType("CHECK_BOX", true);

    public static final IElementType CELL = new MarkdownLazyElementType("CELL", true);

    public static final IElementType DOLLAR = new MarkdownElementType("DOLLAR", true);
}
