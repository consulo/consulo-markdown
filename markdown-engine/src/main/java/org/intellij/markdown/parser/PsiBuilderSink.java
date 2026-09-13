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
import consulo.language.parser.PsiBuilder;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Sink that drives a {@link PsiBuilder}, so the platform builds the tree.
 * <p>
 * Leaves are ignored: the builder is fed by the lexer, which has already produced every token.
 * Markdown registers no whitespace or comment token types with the platform, so
 * {@code advanceLexer()} consumes exactly one lexeme and offsets line up exactly.
 */
public final class PsiBuilderSink implements MarkdownProductionSink {
    private final PsiBuilder myBuilder;
    private final Deque<PsiBuilder.Marker> myMarkers = new ArrayDeque<>();

    public PsiBuilderSink(PsiBuilder builder) {
        myBuilder = builder;
    }

    @Override
    public void open(IElementType type, int startOffset) {
        advanceTo(startOffset);
        myMarkers.push(myBuilder.mark());
    }

    @Override
    public void close(IElementType type, int endOffset) {
        advanceTo(endOffset);
        myMarkers.pop().done(type);
    }

    @Override
    public void leaf(IElementType type, int startOffset, int endOffset) {
        // Supplied by the lexer.
    }

    private void advanceTo(int offset) {
        while (myBuilder.getCurrentOffset() < offset) {
            myBuilder.advanceLexer();
        }
        if (myBuilder.getCurrentOffset() != offset) {
            throw new AssertionError("parsed tree and lexer are unsynchronized");
        }
    }
}
