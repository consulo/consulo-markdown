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

/**
 * Element type of the vendored markdown engine.
 * <p>
 * Rebased onto {@link IElementType} so the engine and the Consulo platform share a single
 * token-type universe. {@code toString()} keeps the upstream {@code Markdown:NAME} rendering
 * so parse-tree dumps stay comparable against the upstream corpus.
 */
public class MarkdownElementType extends IElementType implements MarkdownTokenMarker {
    private final boolean myIsToken;

    public MarkdownElementType(String name, boolean isToken) {
        super(name, MarkdownLanguage.INSTANCE);
        myIsToken = isToken;
    }

    public MarkdownElementType(String name) {
        this(name, false);
    }

    @Override
    public boolean isToken() {
        return myIsToken;
    }

    @Override
    public String toString() {
        return "Markdown:" + super.toString();
    }
}
