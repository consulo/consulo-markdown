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

import java.util.ArrayList;
import java.util.List;

/**
 * Sink that keeps only the leaves, as the flat token stream a {@code Lexer} exposes.
 * <p>
 * Zero-length leaves are dropped: a lexer records token starts and derives each end from the next
 * start, so an empty token would be indistinguishable from a gap.
 */
public final class LexemeSink implements MarkdownProductionSink {
    private final List<IElementType> myTypes = new ArrayList<>();
    private final List<Integer> myStarts = new ArrayList<>();
    private final List<Integer> myEnds = new ArrayList<>();

    public List<IElementType> getTypes() {
        return myTypes;
    }

    public List<Integer> getStarts() {
        return myStarts;
    }

    public List<Integer> getEnds() {
        return myEnds;
    }

    @Override
    public void open(IElementType type, int startOffset) {
    }

    @Override
    public void close(IElementType type, int endOffset) {
    }

    @Override
    public void leaf(IElementType type, int startOffset, int endOffset) {
        if (startOffset == endOffset) {
            return;
        }
        myTypes.add(type);
        myStarts.add(startOffset);
        myEnds.add(endOffset);
    }
}
