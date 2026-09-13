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
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.MarkdownTokenTypes;
import org.intellij.markdown.flavours.gfm.GFMTokenTypes;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;

/**
 * Walker for block-level productions, whose offsets are already absolute character offsets.
 * <p>
 * Anything no production covers is whitespace, and is emitted as leaves so the stream tiles the
 * document without gaps. That is what the old per-composite gap fill did while
 * assembling composites, except that it happens in document order.
 */
public final class TopLevelProductionWalker extends ProductionWalker {
    /**
     * Expands a production whose content is inline structure rather than block structure.
     * Used by callers that want one fully expanded tree; the PSI path leaves these to be
     * reparsed on demand as chameleons instead.
     */
    public interface InlineExpander {
        void expand(IElementType type, int startOffset, int endOffset, MarkdownProductionSink sink);
    }

    private final CharSequence myText;
    private final InlineExpander myInlineExpander;
    private int myPosition;

    public TopLevelProductionWalker(CharSequence text,
                                    InlineExpander inlineExpander,
                                    CancellationToken cancellationToken) {
        super(cancellationToken);
        myText = text;
        myInlineExpander = inlineExpander;
    }

    public TopLevelProductionWalker(CharSequence text, CancellationToken cancellationToken) {
        this(text, null, cancellationToken);
    }

    @Override
    protected int startOffsetOf(SequentialParser.Node node) {
        return node.getStartOffset();
    }

    @Override
    protected int endOffsetOf(SequentialParser.Node node) {
        return node.getEndOffset();
    }

    @Override
    protected void flushBefore(int offset, MarkdownProductionSink sink) {
        if (myPosition < offset) {
            emitWhitespace(myPosition, offset, sink);
            myPosition = offset;
        }
    }

    @Override
    protected void emitLeaf(SequentialParser.Node node, MarkdownProductionSink sink) {
        IElementType type = node.getType();
        if (myInlineExpander != null && isInlineHolder(type)) {
            myInlineExpander.expand(type, node.getStartOffset(), node.getEndOffset(), sink);
        }
        else {
            sink.leaf(type, node.getStartOffset(), node.getEndOffset());
        }
        myPosition = node.getEndOffset();
    }

    private static boolean isInlineHolder(IElementType type) {
        return type == MarkdownElementTypes.PARAGRAPH
                || type == MarkdownTokenTypes.ATX_CONTENT
                || type == MarkdownTokenTypes.SETEXT_CONTENT
                || type == GFMTokenTypes.CELL;
    }

    /**
     * Splits a whitespace run on newlines, so an EOL is its own token. Highlighting and the
     * block parser both distinguish the two.
     */
    private void emitWhitespace(int startOffset, int endOffset, MarkdownProductionSink sink) {
        int last = startOffset;
        while (last < endOffset) {
            myCancellationToken.checkCancelled();
            int nextEol = indexOf(myText, last, endOffset, '\n');
            if (nextEol == -1) {
                break;
            }
            if (nextEol > last) {
                sink.leaf(MarkdownTokenTypes.WHITE_SPACE, last, nextEol);
            }
            sink.leaf(MarkdownTokenTypes.EOL, nextEol, nextEol + 1);
            last = nextEol + 1;
        }
        if (endOffset > last) {
            sink.leaf(MarkdownTokenTypes.WHITE_SPACE, last, endOffset);
        }
    }

    private static int indexOf(CharSequence s, int from, int to, char c) {
        for (int i = from; i < to; i++) {
            if (s.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }
}
