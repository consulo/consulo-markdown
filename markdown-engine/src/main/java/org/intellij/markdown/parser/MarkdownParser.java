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

import org.intellij.markdown.parser.sequentialparsers.IntRange;
import consulo.language.ast.IElementType;
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.MarkdownParsingException;
import org.intellij.markdown.MarkdownTokenTypes;
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor;
import org.intellij.markdown.flavours.gfm.GFMTokenTypes;
import org.intellij.markdown.parser.sequentialparsers.LexerBasedTokensCache;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;
import org.intellij.markdown.parser.sequentialparsers.TokensCache;
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MarkdownParser {
    private final MarkdownFlavourDescriptor myFlavour;
    private final boolean myAssertionsEnabled;
    private final CancellationToken myCancellationToken;

    public MarkdownParser(MarkdownFlavourDescriptor flavour, boolean assertionsEnabled, CancellationToken cancellationToken) {
        myFlavour = flavour;
        myAssertionsEnabled = assertionsEnabled;
        myCancellationToken = cancellationToken;
    }

    public MarkdownParser(MarkdownFlavourDescriptor flavour, boolean assertionsEnabled) {
        this(flavour, assertionsEnabled, CancellationToken.NON_CANCELLABLE);
    }

    public MarkdownParser(MarkdownFlavourDescriptor flavour) {
        this(flavour, true);
    }





    /**
     * Runs the block parser and returns its productions, without turning them into anything.
     * Callers pick a sink; see {@link #walkBlocks}.
     */
    public java.util.List<SequentialParser.Node> parseBlocks(IElementType root, CharSequence text) {
        ProductionHolder productionHolder = new ProductionHolder();
        MarkerProcessor<?> markerProcessor = myFlavour.getMarkerProcessorFactory().createMarkerProcessor(productionHolder);
        ProductionHolder.Marker rootMarker = productionHolder.mark();

        LookaheadText textHolder = new LookaheadText(text.toString());
        LookaheadText.Position pos = textHolder.getStartPosition();
        while (pos != null) {
            myCancellationToken.checkCancelled();
            productionHolder.updatePosition(pos.getOffset());
            pos = markerProcessor.processPosition(pos);
        }

        productionHolder.updatePosition(text.length());
        markerProcessor.flushMarkers();
        rootMarker.done(root);

        return productionHolder.getProduction();
    }


    /**
     * Drives the block-level productions into {@code sink}. The one place block parse results
     * are turned into anything - every consumer supplies its own sink.
     */
    public void walkBlocks(java.util.List<SequentialParser.Node> production,
                           CharSequence text,
                           boolean parseInlines,
                           MarkdownProductionSink sink) {
        TopLevelProductionWalker.InlineExpander expander = parseInlines
                ? (type, start, end, target) -> parseInlineInto(type, text, start, end, target)
                : null;
        new TopLevelProductionWalker(text, expander, myCancellationToken).walk(production, sink);
    }

    /** Drives inline productions into {@code sink}; see {@link #walkBlocks}. */
    public void walkInline(TokensCache tokensCache,
                           java.util.List<SequentialParser.Node> production,
                           SequentialParser.Node root,
                           MarkdownProductionSink sink) {
        new InlineProductionWalker(tokensCache, root, myCancellationToken).walk(production, sink);
    }


    /**
     * Parses inline content straight into {@code sink}, building no tree. This is what the
     * chameleon reparse uses: it can drive a {@code PsiBuilder} directly.
     */
    public void parseInlineInto(IElementType root, CharSequence text, int textStart, int textEnd,
                                MarkdownProductionSink sink) {
        org.intellij.markdown.lexer.MarkdownLexer lexer = myFlavour.createInlinesLexer();
        lexer.start(text, textStart, textEnd, 0);
        TokensCache tokensCache = new LexerBasedTokensCache(lexer);

        int wholeRangeStart = 0;
        int wholeRangeEnd = tokensCache.getFilteredTokens().size();
        java.util.Collection<SequentialParser.Node> nodes =
            myFlavour.getSequentialParserManager().runParsingSequence(
                tokensCache,
                SequentialParserUtil.filterBlockquotes(tokensCache, new IntRange(wholeRangeStart, wholeRangeEnd)),
                myCancellationToken);

        java.util.List<SequentialParser.Node> allNodes = new java.util.ArrayList<>(nodes);
        SequentialParser.Node rootNode = new SequentialParser.Node(wholeRangeStart, wholeRangeEnd, root);
        allNodes.add(rootNode);

        walkInline(tokensCache, allNodes, rootNode, sink);
    }



}
