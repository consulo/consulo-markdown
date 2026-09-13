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
package org.intellij.markdown.flavours.gfm;

import consulo.language.ast.IElementType;
import org.intellij.markdown.MarkdownTokenTypes;
import org.intellij.markdown.parser.sequentialparsers.IntRange;
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;
import org.intellij.markdown.parser.sequentialparsers.TokensCache;

import java.util.List;

/**
 * @deprecated Use {@link org.intellij.markdown.parser.sequentialparsers.EmphasisLikeParser} with
 * {@link StrikeThroughDelimiterParser} instead.
 */
@Deprecated
public final class StrikeThroughParser implements SequentialParser {
    @Override
    public ParsingResult parse(TokensCache tokens, List<IntRange> rangesToGlue) {
        ParsingResultBuilder result = new ParsingResultBuilder();
        RangesListBuilder delegateIndices = new RangesListBuilder();
        Integer lastOpenedPos = null;
        TokensCache.Iterator iterator = tokens.new RangesListIterator(rangesToGlue);

        while (iterator.getType() != null) {
            if (iterator.getType() != GFMTokenTypes.TILDE) {
                delegateIndices.put(iterator.getIndex());
                iterator = iterator.advance();
                continue;
            }

            if (lastOpenedPos != null
                    && isGoodType(iterator.rawLookup(-1))
                    && iterator.rawLookup(1) == GFMTokenTypes.TILDE) {
                iterator = iterator.advance();
                result.withNode(new Node(lastOpenedPos, iterator.getIndex() + 1, GFMElementTypes.STRIKETHROUGH));
                iterator = iterator.advance();
                lastOpenedPos = null;
                continue;
            }
            if (lastOpenedPos == null
                    && iterator.rawLookup(1) == GFMTokenTypes.TILDE
                    && isGoodType(iterator.rawLookup(2))) {
                lastOpenedPos = iterator.getIndex();
                iterator = iterator.advance().advance();
                continue;
            }

            delegateIndices.put(iterator.getIndex());
            iterator = iterator.advance();
        }

        if (lastOpenedPos != null) {
            for (int delta = 0; delta <= 1; delta++) {
                delegateIndices.put(lastOpenedPos + delta);
            }
        }

        return result.withFurtherProcessing(delegateIndices.get());
    }

    private boolean isGoodType(IElementType type) {
        return type == null
                || (type != MarkdownTokenTypes.WHITE_SPACE
                && type != MarkdownTokenTypes.EOL
                && type != GFMTokenTypes.TILDE
                && type != MarkdownTokenTypes.HTML_TAG
                && type != MarkdownTokenTypes.HTML_BLOCK_CONTENT);
    }
}
