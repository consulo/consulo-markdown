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

public final class ReferenceLinkParser implements SequentialParser {

    @Override
    
    public ParsingResult parse(TokensCache tokens, List<IntRange> rangesToGlue) {
        ParsingResultBuilder result = new ParsingResultBuilder();
        RangesListBuilder delegateIndices = new RangesListBuilder();
        TokensCache.Iterator iterator = tokens.new RangesListIterator(rangesToGlue);

        while (iterator.getType() != null) {
            if (iterator.getType() == MarkdownTokenTypes.LBRACKET) {
                LocalParsingResult referenceLink = parseReferenceLink(iterator);
                if (referenceLink != null) {
                    iterator = referenceLink.getIteratorPosition().advance();
                    result.withOtherParsingResult(referenceLink);
                    continue;
                }
            }

            delegateIndices.put(iterator.getIndex());
            iterator = iterator.advance();
        }

        return result.withFurtherProcessing(delegateIndices.get());
    }

    public static @Nullable LocalParsingResult parseReferenceLink(TokensCache.Iterator iterator) {
        LocalParsingResult full = parseFullReferenceLink(iterator);
        if (full != null) {
            return full;
        }
        return parseShortReferenceLink(iterator);
    }

    private static @Nullable LocalParsingResult parseFullReferenceLink(TokensCache.Iterator iterator) {
        int startIndex = iterator.getIndex();

        LocalParsingResult linkText = LinkParserUtil.parseLinkText(iterator);
        if (linkText == null) {
            return null;
        }
        TokensCache.Iterator it = linkText.getIteratorPosition().advance();

        if (it.getType() == MarkdownTokenTypes.EOL) {
            it = it.advance();
        }

        LocalParsingResult linkLabel = LinkParserUtil.parseLinkLabel(it);
        if (linkLabel == null) {
            return null;
        }

        it = linkLabel.getIteratorPosition();

        ArrayList<Node> parsedNodes = new ArrayList<>();
        parsedNodes.addAll(linkText.getParsedNodes());
        parsedNodes.addAll(linkLabel.getParsedNodes());
        parsedNodes.add(new Node(startIndex, it.getIndex() + 1, MarkdownElementTypes.FULL_REFERENCE_LINK));

        ArrayList<List<IntRange>> rangesToProcess = new ArrayList<>();
        rangesToProcess.addAll(linkText.getRangesToProcessFurther());
        rangesToProcess.addAll(linkLabel.getRangesToProcessFurther());

        return new LocalParsingResult(it, parsedNodes, rangesToProcess);
    }

    private static @Nullable LocalParsingResult parseShortReferenceLink(TokensCache.Iterator iterator) {
        int startIndex = iterator.getIndex();

        LocalParsingResult linkLabel = LinkParserUtil.parseLinkLabel(iterator);
        if (linkLabel == null) {
            return null;
        }

        TokensCache.Iterator it = linkLabel.getIteratorPosition();
        TokensCache.Iterator shortcutLinkEnd = it;

        it = it.advance();
        if (it.getType() == MarkdownTokenTypes.EOL) {
            it = it.advance();
        }

        if (it.getType() == MarkdownTokenTypes.LBRACKET && it.rawLookup(1) == MarkdownTokenTypes.RBRACKET) {
            it = it.advance();
        } else {
            it = shortcutLinkEnd;
        }

        ArrayList<Node> parsedNodes = new ArrayList<>();
        parsedNodes.addAll(linkLabel.getParsedNodes());
        parsedNodes.add(new Node(startIndex, it.getIndex() + 1, MarkdownElementTypes.SHORT_REFERENCE_LINK));

        return new LocalParsingResult(it, parsedNodes, linkLabel.getRangesToProcessFurther());
    }
}
