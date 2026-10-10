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
import org.intellij.markdown.lexer.TokenInfo;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;
import org.intellij.markdown.parser.sequentialparsers.TokensCache;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Walker for inline productions.
 * <p>
 * Unlike block productions, these are expressed as indices into the cache's <em>filtered</em>
 * token list, which has whitespace removed. Every index is converted to an absolute character
 * offset here, so the sink only ever sees character offsets. Leaves come from the <em>cached</em>
 * token list, whitespace included, so the emitted stream tiles the range without gaps.
 */
public final class InlineProductionWalker extends ProductionWalker {
    private final TokensCache myTokensCache;
    private final SequentialParser.Node myRoot;
    private int myRawIndex;

    public InlineProductionWalker(TokensCache tokensCache,
                                  SequentialParser.Node root,
                                  CancellationToken cancellationToken) {
        super(cancellationToken);
        myTokensCache = tokensCache;
        myRoot = root;
    }

    /**
     * Inline productions are always composites: a token-typed span here still contains the raw
     * tokens it covers, unlike at block level where such a span collapses into a single leaf.
     */
    @Override
    protected boolean isLeafType(IElementType type) {
        return false;
    }

    @Override
    protected int startOffsetOf(SequentialParser.Node node) {
        if (node == myRoot) {
            // The root deliberately spans the whole lexed range, including any raw tokens outside
            // the filtered span, so that nothing is left over.
            return myTokensCache.getOriginalTextStart();
        }
        return myTokensCache.new Iterator(node.getStartOffset()).getStart();
    }

    @Override
    protected int endOffsetOf(SequentialParser.Node node) {
        if (node == myRoot) {
            // getOriginalTextEnd() is an INCLUSIVE last index (a leftover of Kotlin's `until`),
            // so the exclusive end is one past it. Using it directly drops the final token.
            return myTokensCache.getOriginalTextEnd() + 1;
        }
        int start = node.getStartOffset();
        int end = node.getEndOffset();
        if (end == start) {
            return myTokensCache.new Iterator(start).getStart();
        }
        // The end of the last token inside the span - NOT the start of the token after it, which
        // would swallow the separator belonging to the enclosing node.
        return myTokensCache.new Iterator(end - 1).getEnd();
    }

    @Override
    protected void flushBefore(int offset, MarkdownProductionSink sink) {
        List<TokenInfo> cached = myTokensCache.getCachedTokens();
        while (myRawIndex < cached.size() && cached.get(myRawIndex).getTokenStart() < offset) {
            myCancellationToken.checkCancelled();
            TokenInfo token = cached.get(myRawIndex);
            sink.leaf(token.getType(), token.getTokenStart(), token.getTokenEnd());
            myRawIndex++;
        }
    }

    @Override
    protected void emitLeaf(SequentialParser.Node node, MarkdownProductionSink sink) {
        throw new IllegalStateException("inline productions are never leaves");
    }

    @Override
    protected void emitFallback(SequentialParser.@Nullable Node root, MarkdownProductionSink sink) {
        if (root == null) {
            return;
        }
        IElementType type = root.getType();
        int start = myTokensCache.getOriginalTextStart();
        int end = myTokensCache.getOriginalTextEnd() + 1;
        sink.open(type, start);
        for (TokenInfo token : myTokensCache.getCachedTokens()) {
            sink.leaf(token.getType(), token.getTokenStart(), token.getTokenEnd());
        }
        sink.close(type, end);
    }
}
