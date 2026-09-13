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
package org.intellij.markdown.parser.sequentialparsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Should be used to parse emphasis-like elements with the same priority that should be aware of each other.
 * Accepts a list of {@link DelimiterParser} that finds target delimiters runs
 * and stores containing delimiters in the shared delimiters list.
 * For each of those delimiters, this parser tries to find a matching closing delimiter.
 * Matching information then is passed back to the {@link DelimiterParser}
 * that should use it to create actual tree nodes.
 */
public final class EmphasisLikeParser implements SequentialParser {
    private final DelimiterParser[] myParsers;

    public EmphasisLikeParser(DelimiterParser... parsers) {
        myParsers = parsers;
    }

    @Override
    
    public ParsingResult parse(TokensCache tokens, List<IntRange> rangesToGlue) {
        ParsingResultBuilder result = new ParsingResultBuilder();
        TokensCache.Iterator iterator = tokens.new RangesListIterator(rangesToGlue);
        ArrayList<DelimiterParser.Info> delimiters = collectDelimiters(tokens, iterator);
        balanceDelimiters(delimiters);
        for (DelimiterParser parser : myParsers) {
            parser.process(tokens, iterator, delimiters, result);
        }
        return result;
    }

    
    private ArrayList<DelimiterParser.Info> collectDelimiters(
            TokensCache tokens,
            TokensCache.Iterator iterator
    ) {
        ArrayList<DelimiterParser.Info> delimiters = new ArrayList<>();
        TokensCache.Iterator position = iterator;
        outer:
        while (position.getType() != null) {
            int iteratorMoved = 0;
            for (DelimiterParser parser : myParsers) {
                int steps = parser.scan(tokens, position, delimiters);
                iteratorMoved += steps;
                for (int step = 0; step < steps; step++) {
                    if (position.getType() == null) {
                        break outer;
                    }
                    position = position.advance();
                }
            }
            if (iteratorMoved == 0) {
                position = position.advance();
            }
        }
        return delimiters;
    }

    /**
     * For each delimiter from delimiters tries to find matching closing delimiter.
     * If such a delimiter is found, sets {@link DelimiterParser.Info#setCloserIndex(int)} for the opening delimiter
     * to the index of the closing one.
     *
     * Heavily based on cmark and markdown-it implementations.
     */
    private void balanceDelimiters(ArrayList<DelimiterParser.Info> delimiters) {
        int runStartIndex = 0;
        int previousDelimiterIndex = -2;
        int[] openersIndices = new int[delimiters.size()];
        // Used to set lower bounds for openers searches in case some previous match failed.
        HashMap<Character, int[]> openersBottom = new HashMap<>();
        for (int closerIndex = 0; closerIndex < delimiters.size(); closerIndex++) {
            DelimiterParser.Info closer = delimiters.get(closerIndex);
            // Initiate a new delimiter run if the current delimiter can not belong to the current run
            if (delimiters.get(runStartIndex).getMarker() != closer.getMarker()
                    || previousDelimiterIndex != closer.getPosition() - 1) {
                runStartIndex = closerIndex;
            }
            previousDelimiterIndex = closer.getPosition();
            if (!closer.getCanClose()) {
                continue;
            }
            if (!openersBottom.containsKey(closer.getMarker())) {
                openersBottom.put(closer.getMarker(), new int[]{-1, -1, -1, -1, -1, -1});
            }
            // Now, look back in the stack for the first matching potential opener
            int minOpenerIndex = openersBottom.get(closer.getMarker())[(closer.getCanOpen() ? 3 : 0) + (closer.getLength() % 3)];
            int openerIndex = runStartIndex - openersIndices[runStartIndex] - 1;
            int newMinOpenerIndex = openerIndex;
            while (openerIndex > minOpenerIndex) {
                DelimiterParser.Info opener = delimiters.get(openerIndex);
                if (opener.getMarker() != closer.getMarker()) {
                    openerIndex -= openersIndices[openerIndex] + 1;
                    continue;
                }
                if (opener.getCanOpen() && opener.getCloserIndex() < 0 && !violatesRuleOfThree(opener, closer)) {
                    // If the previous delimiter cannot be an opener, skip the entire sequence in future checks.
                    int lastIndex;
                    if (openerIndex > 0 && !delimiters.get(openerIndex - 1).getCanOpen()) {
                        lastIndex = openersIndices[openerIndex - 1] + 1;
                    } else {
                        lastIndex = 0;
                    }
                    openersIndices[openerIndex] = lastIndex;
                    openersIndices[closerIndex] = closerIndex - openerIndex + lastIndex;
                    closer.setCanOpen(false);
                    opener.setCloserIndex(closerIndex);
                    opener.setCanClose(false);
                    newMinOpenerIndex = -1;
                    // Start new delimiter run
                    previousDelimiterIndex = -2;
                    break;
                }
                // Search for other opener before
                openerIndex -= openersIndices[openerIndex] + 1;
            }
            // Search has failed, so update future search lower bound
            if (newMinOpenerIndex != -1) {
                openersBottom.get(closer.getMarker())[(closer.getCanOpen() ? 3 : 0) + (closer.getLength() % 3)] = newMinOpenerIndex;
            }
        }
    }

    /**
     * If one of the delimiters can both open and close emphasis, then the
     * sum of the lengths of the delimiter runs containing the opening and
     * closing delimiters must not be a multiple of 3 unless both lengths
     * are multiples of 3.
     *
     * See rules 9 and 10.
     */
    private boolean violatesRuleOfThree(DelimiterParser.Info opener, DelimiterParser.Info closer) {
        return (opener.getCanClose() || closer.getCanOpen())
                && ((opener.getLength() + closer.getLength()) % 3 == 0)
                && (opener.getLength() % 3 != 0 || closer.getLength() % 3 != 0);
    }
}
