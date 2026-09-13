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
import consulo.language.lexer.Lexer;
import org.intellij.markdown.MarkdownTokenTypes;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MarkdownLexer {
    private final Lexer myBaseLexer;

    /**
     * Bounds of the token most recently returned by {@link #advanceBase()}.
     * <p>
     * Consulo's {@link Lexer} exposes the token at the <em>current</em> position and moves past it
     * on {@code advance()}, whereas the upstream generated-lexer contract returned the token it had
     * just scanned and left its bounds queryable. These fields bridge that difference — without
     * them every offset would be shifted by one token.
     */
    private int myBaseTokenStart;
    private int myBaseTokenEnd;

    private @Nullable IElementType myType;
    private @Nullable IElementType myNextType;

    private CharSequence myOriginalText = "";
    private int myBufferStart;
    private int myBufferEnd;

    private int myTokenStart;
    private int myTokenEnd;

    private static final Set<IElementType> TOKENS_TO_MERGE = new HashSet<>(Arrays.asList(
            MarkdownTokenTypes.TEXT,
            MarkdownTokenTypes.WHITE_SPACE,
            MarkdownTokenTypes.CODE_LINE,
            MarkdownTokenTypes.LINK_ID,
            MarkdownTokenTypes.LINK_TITLE,
            MarkdownTokenTypes.URL,
            MarkdownTokenTypes.AUTOLINK,
            MarkdownTokenTypes.EMAIL_AUTOLINK,
            MarkdownTokenTypes.BAD_CHARACTER));

    public MarkdownLexer(Lexer baseLexer) {
        myBaseLexer = baseLexer;
    }

    public @Nullable IElementType getType() {
        return myType;
    }

    public CharSequence getOriginalText() {
        return myOriginalText;
    }

    public int getBufferStart() {
        return myBufferStart;
    }

    public int getBufferEnd() {
        return myBufferEnd;
    }

    public int getTokenStart() {
        return myTokenStart;
    }

    public int getTokenEnd() {
        return myTokenEnd;
    }

    public int getState() {
        return myBaseLexer.getState();
    }

    public void start(CharSequence originalText, int bufferStart, int bufferEnd, int state) {
        reset(originalText, bufferStart, bufferEnd, state);
        calcNextType();
    }

    public void start(CharSequence originalText, int bufferStart, int bufferEnd) {
        start(originalText, bufferStart, bufferEnd, 0);
    }

    public void start(CharSequence originalText, int bufferStart) {
        start(originalText, bufferStart, originalText.length(), 0);
    }

    public void start(CharSequence originalText) {
        start(originalText, 0, originalText.length(), 0);
    }

    public boolean advance() {
        return locateToken();
    }

    private boolean locateToken() {
        myType = myNextType;
        myTokenStart = myTokenEnd;
        if (myType == null) {
            return false;
        }

        calcNextType();
        return true;
    }

    private void calcNextType() {
        do {
            myTokenEnd = myBaseTokenEnd;
            myNextType = advanceBase();
        } while (myNextType == myType && myType != null && TOKENS_TO_MERGE.contains(myType));
    }

    private @Nullable IElementType advanceBase() {
        IElementType type = myBaseLexer.getTokenType();
        myBaseTokenStart = myBaseLexer.getTokenStart();
        myBaseTokenEnd = myBaseLexer.getTokenEnd();
        if (type != null) {
            myBaseLexer.advance();
        }
        return type;
    }

    public void reset(CharSequence buffer, int start, int end, int initialState) {
        myOriginalText = buffer;
        myBufferStart = start;
        myBufferEnd = end;
        myBaseLexer.start(buffer, start, end, initialState);
        myType = advanceBase();
        myTokenStart = myBaseTokenStart;
    }
}
