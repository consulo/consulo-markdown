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

import java.util.List;

public final class ImageParser implements SequentialParser {

    @Override
    
    public ParsingResult parse(TokensCache tokens, List<IntRange> rangesToGlue) {
        ParsingResultBuilder result = new ParsingResultBuilder();
        RangesListBuilder delegateIndices = new RangesListBuilder();
        TokensCache.Iterator iterator = tokens.new RangesListIterator(rangesToGlue);

        while (iterator.getType() != null) {
            if (iterator.getType() == MarkdownTokenTypes.EXCLAMATION_MARK
                    && iterator.rawLookup(1) == MarkdownTokenTypes.LBRACKET) {
                LocalParsingResult link = InlineLinkParser.parseInlineLink(iterator.advance());
                if (link == null) {
                    link = ReferenceLinkParser.parseReferenceLink(iterator.advance());
                }

                if (link != null) {
                    result.withNode(new Node(iterator.getIndex(), link.getIteratorPosition().getIndex() + 1, MarkdownElementTypes.IMAGE))
                          .withOtherParsingResult(link);
                    iterator = link.getIteratorPosition().advance();
                    continue;
                }
            }

            delegateIndices.put(iterator.getIndex());
            iterator = iterator.advance();
        }

        return result.withFurtherProcessing(delegateIndices.get());
    }
}
