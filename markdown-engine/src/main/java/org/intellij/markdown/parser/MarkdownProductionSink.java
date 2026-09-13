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
package org.intellij.markdown.parser;

import consulo.language.ast.IElementType;

/**
 * Receives the parse result as a linear stream of open/close/leaf calls.
 * <p>
 * Replaces building an intermediate tree: the walker emits productions in document order and each
 * consumer decides what to do with them. Every offset is an absolute character offset in the
 * document - no token indices ever cross this interface.
 */
public interface MarkdownProductionSink {
    void open(IElementType type, int startOffset);

    void close(IElementType type, int endOffset);

    /**
     * A terminal node. Sinks fed by a lexer that already produced the tokens - such as one writing
     * into a {@code PsiBuilder} - ignore this.
     */
    void leaf(IElementType type, int startOffset, int endOffset);
}
