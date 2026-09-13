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

import org.intellij.markdown.flavours.gfm.GFMElementTypes;
import org.intellij.markdown.flavours.gfm.GFMTokenTypes;
import org.intellij.markdown.parser.sequentialparsers.IntRange;
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;
import org.intellij.markdown.parser.sequentialparsers.TokensCache;

import org.jspecify.annotations.Nullable;

import java.util.List;

public final class MathParser implements SequentialParser {

    @Override
    
    public ParsingResult parse(TokensCache tokens, List<IntRange> rangesToGlue) {
        ParsingResultBuilder result = new ParsingResultBuilder();
        RangesListBuilder delegateIndices = new RangesListBuilder();
        TokensCache.Iterator iterator = tokens.new RangesListIterator(rangesToGlue);

        while (iterator.getType() != null) {
            if (iterator.getType() == GFMTokenTypes.DOLLAR) {

                TokensCache.Iterator endIterator = findOfSize(iterator.advance(), iterator.getLength());

                if (endIterator != null) {
                    if (iterator.getLength() == 1) {
                        result.withNode(new Node(iterator.getIndex(), endIterator.getIndex() + 1, GFMElementTypes.INLINE_MATH));
                    } else {
                        result.withNode(new Node(iterator.getIndex(), endIterator.getIndex() + 1, GFMElementTypes.BLOCK_MATH));
                    }
                    iterator = endIterator.advance();
                    continue;
                }
            }
            delegateIndices.put(iterator.getIndex());
            iterator = iterator.advance();
        }

        return result.withFurtherProcessing(delegateIndices.get());
    }

    private TokensCache.@Nullable Iterator findOfSize(TokensCache.Iterator it, int length) {
        TokensCache.Iterator iterator = it;
        while (iterator.getType() != null) {
            if (iterator.getType() == GFMTokenTypes.DOLLAR) {
                if (iterator.getLength() == length) {
                    return iterator;
                }
            }
            iterator = iterator.advance();
        }
        return null;
    }
}
