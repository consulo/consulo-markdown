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
import org.intellij.markdown.parser.sequentialparsers.DelimiterParser;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;
import org.intellij.markdown.parser.sequentialparsers.TokensCache;

import java.util.List;

public final class EmphStrongDelimiterParser extends DelimiterParser {

    @Override
    public int scan(
            TokensCache tokens,
            TokensCache.Iterator iterator,
            List<Info> delimiters
    ) {
        if (iterator.getType() != MarkdownTokenTypes.EMPH) {
            return 0;
        }
        int stepsToAdvance = 1;
        TokensCache.Iterator rightIterator = iterator;
        char marker = getType(rightIterator);
        for (int index = 0; index < MAX_ADVANCE; index++) {
            if (rightIterator.rawLookup(1) != MarkdownTokenTypes.EMPH
                    || getType(rightIterator.advance()) != marker) {
                break;
            }
            rightIterator = rightIterator.advance();
            stepsToAdvance += 1;
        }
        OpenCloseResult openClose = canOpenClose(tokens, iterator, rightIterator, marker == '*');
        boolean canOpen = openClose.getCanOpen();
        boolean canClose = openClose.getCanClose();
        for (int index = 0; index < stepsToAdvance; index++) {
            Info info = new Info(
                    MarkdownTokenTypes.EMPH,
                    iterator.getIndex() + index,
                    stepsToAdvance,
                    canOpen,
                    canClose,
                    marker
            );
            delimiters.add(info);
        }
        return stepsToAdvance;
    }

    @Override
    public void process(
            TokensCache tokens,
            TokensCache.Iterator iterator,
            List<Info> delimiters,
            SequentialParser.ParsingResultBuilder result
    ) {
        boolean isStrong = false;
        for (int index = delimiters.size() - 1; index >= 0; index--) {
            if (isStrong) {
                isStrong = false;
                continue;
            }
            Info opener = delimiters.get(index);
            if (opener.getTokenType() != MarkdownTokenTypes.EMPH || opener.getCloserIndex() == -1) {
                continue;
            }
            isStrong = areAdjacentSameMarkers(delimiters, index, opener.getCloserIndex());
            Info closer = delimiters.get(opener.getCloserIndex());
            SequentialParser.Node node;
            if (isStrong) {
                node = new SequentialParser.Node(
                        opener.getPosition() - 1,
                        closer.getPosition() + 2,
                        MarkdownElementTypes.STRONG
                );
            } else {
                node = new SequentialParser.Node(
                        opener.getPosition(),
                        closer.getPosition() + 1,
                        MarkdownElementTypes.EMPH
                );
            }
            result.withNode(node);
        }
    }

    public static boolean areAdjacentSameMarkers(
            List<Info> delimiters,
            int openerIndex,
            int closerIndex
    ) {
        Info opener = delimiters.get(openerIndex);
        Info closer = delimiters.get(closerIndex);
        return openerIndex > 0
                && delimiters.get(openerIndex - 1).getCloserIndex() == opener.getCloserIndex() + 1
                && delimiters.get(openerIndex - 1).getMarker() == opener.getMarker()
                && delimiters.get(openerIndex - 1).getPosition() == opener.getPosition() - 1
                && delimiters.get(opener.getCloserIndex() + 1).getPosition() == closer.getPosition() + 1;
    }
}
