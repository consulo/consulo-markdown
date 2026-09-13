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

import consulo.language.ast.IElementType;
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.MarkdownTokenTypes;
import org.intellij.markdown.parser.sequentialparsers.IntRange;
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;
import org.intellij.markdown.parser.sequentialparsers.TokensCache;

import java.util.List;

public final class AutolinkParser implements SequentialParser {
    private final List<IElementType> myTypesAfterLT;

    public AutolinkParser(List<IElementType> typesAfterLT) {
        myTypesAfterLT = typesAfterLT;
    }

    @Override
    
    public ParsingResult parse(TokensCache tokens, List<IntRange> rangesToGlue) {
        ParsingResultBuilder result = new ParsingResultBuilder();
        RangesListBuilder delegateIndices = new RangesListBuilder();
        TokensCache.Iterator iterator = tokens.new RangesListIterator(rangesToGlue);

        while (iterator.getType() != null) {
            if (iterator.getType() == MarkdownTokenTypes.LT) {
                IElementType nextType = iterator.rawLookup(1);
                if (nextType != null && myTypesAfterLT.contains(nextType)) {
                    int start = iterator.getIndex();
                    while (iterator.getType() != MarkdownTokenTypes.GT && iterator.getType() != null) {
                        iterator = iterator.advance();
                    }
                    if (iterator.getType() == MarkdownTokenTypes.GT) {
                        result.withNode(new Node(start, iterator.getIndex() + 1, MarkdownElementTypes.AUTOLINK));
                    }
                } else {
                    delegateIndices.put(iterator.getIndex());
                }
            } else {
                delegateIndices.put(iterator.getIndex());
            }
            iterator = iterator.advance();
        }

        return result.withFurtherProcessing(delegateIndices.get());
    }
}
