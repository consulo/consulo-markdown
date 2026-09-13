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

import org.intellij.markdown.parser.sequentialparsers.DelimiterParser;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;
import org.intellij.markdown.parser.sequentialparsers.TokensCache;
import org.intellij.markdown.parser.sequentialparsers.impl.EmphStrongDelimiterParser;

import java.util.List;

public final class StrikeThroughDelimiterParser extends DelimiterParser {
    @Override
    public int scan(TokensCache tokens, TokensCache.Iterator iterator, List<Info> delimiters) {
        if (iterator.getType() != GFMTokenTypes.TILDE) {
            return 0;
        }
        int stepsToAdvance = 1;
        TokensCache.Iterator rightIterator = iterator;
        for (int index = 0; index < MAX_ADVANCE; index++) {
            if (rightIterator.rawLookup(1) != GFMTokenTypes.TILDE) {
                break;
            }
            rightIterator = rightIterator.advance();
            stepsToAdvance += 1;
        }
        OpenCloseResult openClose = canOpenClose(tokens, iterator, rightIterator, true);
        boolean canOpen = openClose.getCanOpen();
        boolean canClose = openClose.getCanClose();
        for (int index = 0; index < stepsToAdvance; index++) {
            Info info = new Info(
                    GFMTokenTypes.TILDE,
                    iterator.getIndex() + index,
                    0,
                    canOpen,
                    canClose,
                    '~'
            );
            delimiters.add(info);
        }
        return stepsToAdvance;
    }

    @Override
    public void process(TokensCache tokens, TokensCache.Iterator iterator, List<Info> delimiters,
                        SequentialParser.ParsingResultBuilder result) {
        // Start at the end and move backward, matching tokens
        int index = delimiters.size() - 1;

        while (index > 0) {
            // Find opening tilde
            if (!isOpeningTilde(delimiters.get(index))) {
                index -= 1;
                continue;
            }
            int openerIndex = index;
            int closerIndex = delimiters.get(index).getCloserIndex();

            // Attempt to widen the matched delimiters
            int delimitersMatched = 1;
            while (EmphStrongDelimiterParser.areAdjacentSameMarkers(delimiters, openerIndex, closerIndex)) {
                openerIndex -= 1;
                closerIndex += 1;
                delimitersMatched += 1;
            }

            // If 3 or more delimiters are matched, ignore
            if (delimitersMatched < 3) {
                Info opener = delimiters.get(openerIndex);
                Info closer = delimiters.get(closerIndex);

                result.withNode(new SequentialParser.Node(
                        opener.getPosition(), closer.getPosition() + 1, GFMElementTypes.STRIKETHROUGH));
            }

            // Update index
            index = openerIndex - 1;
        }
    }

    private static boolean isOpeningTilde(Info info) {
        return info.getTokenType() == GFMTokenTypes.TILDE && info.getCloserIndex() != -1;
    }
}
