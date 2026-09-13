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
package org.intellij.markdown.parser.sequentialparsers.impl;

import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.MarkdownTokenTypes;
import org.intellij.markdown.parser.sequentialparsers.IntRange;
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;
import org.intellij.markdown.parser.sequentialparsers.TokensCache;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class InlineLinkParser implements SequentialParser {

    @Override
    
    public ParsingResult parse(TokensCache tokens, List<IntRange> rangesToGlue) {
        ParsingResultBuilder result = new ParsingResultBuilder();
        RangesListBuilder delegateIndices = new RangesListBuilder();
        TokensCache.Iterator iterator = tokens.new RangesListIterator(rangesToGlue);

        while (iterator.getType() != null) {
            if (iterator.getType() == MarkdownTokenTypes.LBRACKET) {
                LocalParsingResult inlineLink = parseInlineLink(iterator);
                if (inlineLink != null) {
                    iterator = inlineLink.getIteratorPosition().advance();
                    result.withOtherParsingResult(inlineLink);
                    continue;
                }
            }

            delegateIndices.put(iterator.getIndex());
            iterator = iterator.advance();
        }

        return result.withFurtherProcessing(delegateIndices.get());
    }

    public static @Nullable LocalParsingResult parseInlineLink(TokensCache.Iterator iterator) {
        int startIndex = iterator.getIndex();
        TokensCache.Iterator it = iterator;

        LocalParsingResult linkText = LinkParserUtil.parseLinkText(it);
        if (linkText == null) {
            return null;
        }
        it = linkText.getIteratorPosition();
        if (it.rawLookup(1) != MarkdownTokenTypes.LPAREN) {
            return null;
        }

        it = it.advance().advance();
        if (it.getType() == MarkdownTokenTypes.EOL) {
            it = it.advance();
        }
        LocalParsingResult linkDestination = LinkParserUtil.parseLinkDestination(it);
        if (linkDestination != null) {
            it = linkDestination.getIteratorPosition().advance();
            if (it.getType() == MarkdownTokenTypes.EOL) {
                it = it.advance();
            }
        }
        LocalParsingResult linkTitle = LinkParserUtil.parseLinkTitle(it);
        if (linkTitle != null) {
            it = linkTitle.getIteratorPosition().advance();
            if (it.getType() == MarkdownTokenTypes.EOL) {
                it = it.advance();
            }
        }
        if (it.getType() != MarkdownTokenTypes.RPAREN) {
            return null;
        }

        ArrayList<Node> parsedNodes = new ArrayList<>();
        parsedNodes.addAll(linkText.getParsedNodes());
        if (linkDestination != null) {
            parsedNodes.addAll(linkDestination.getParsedNodes());
        }
        if (linkTitle != null) {
            parsedNodes.addAll(linkTitle.getParsedNodes());
        }
        parsedNodes.add(new Node(startIndex, it.getIndex() + 1, MarkdownElementTypes.INLINE_LINK));

        return new LocalParsingResult(it, parsedNodes, linkText.getRangesToProcessFurther());
    }
}
