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
package org.intellij.markdown.lexer;

import consulo.language.ast.IElementType;
import consulo.language.lexer.LexerBase;
import org.intellij.markdown.parser.MarkdownParserManager;
import org.jspecify.annotations.Nullable;

public class MarkdownLexerAdapter extends LexerBase {
    private final MarkdownLexer myDelegate = MarkdownParserManager.FLAVOUR.createInlinesLexer();

    private int myEndOffset;

    @Override
    public void start(CharSequence buffer, int startOffset, int endOffset, int initialState) {
        myEndOffset = endOffset;
        myDelegate.start(buffer, startOffset, endOffset, 0);
    }

    @Override
    public int getState() {
        return 1;
    }

    @Override
    public @Nullable IElementType getTokenType() {
        return myDelegate.getType();
    }

    @Override
    public int getTokenStart() {
        return myDelegate.getTokenStart();
    }

    @Override
    public int getTokenEnd() {
        return myDelegate.getTokenEnd();
    }

    @Override
    public void advance() {
        myDelegate.advance();
    }

    @Override
    public CharSequence getBufferSequence() {
        return myDelegate.getOriginalText();
    }

    @Override
    public int getBufferEnd() {
        return myEndOffset;
    }
}
