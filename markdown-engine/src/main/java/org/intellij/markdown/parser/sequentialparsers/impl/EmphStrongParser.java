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
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil;
import org.intellij.markdown.parser.sequentialparsers.TokensCache;

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated Use {@link org.intellij.markdown.parser.sequentialparsers.EmphasisLikeParser}
 * with {@link EmphStrongDelimiterParser} instead.
 */
@Deprecated
public final class EmphStrongParser implements SequentialParser {

    public static final char ITALIC = '_';
    public static final char BOLD = '*';

    @Override
    
    public ParsingResult parse(TokensCache tokens, List<IntRange> rangesToGlue) {
        ParsingResultBuilder result = new ParsingResultBuilder();
        TokensCache.Iterator iterator = tokens.new RangesListIterator(rangesToGlue);

        ArrayList<OpeningEmphInfo> openingOnes = new ArrayList<>();

        while (iterator.getType() != null) {
            if (iterator.getType() != MarkdownTokenTypes.EMPH) {
                iterator = iterator.advance();
                continue;
            }

            boolean thisEmphWasEaten = false;

            int numCanEnd = canEndNumber(iterator);
            while (numCanEnd > 0) {
                int stackIndex = -1;
                for (int i = openingOnes.size() - 1; i >= 0; i--) {
                    if (openingOnes.get(i).myType == getType(iterator)) {
                        stackIndex = i;
                        break;
                    }
                }
                if (stackIndex == -1) {
                    break;
                }
                OpeningEmphInfo opening = openingOnes.get(stackIndex);

                int toMakeMax = Math.min(opening.myNumChars, numCanEnd);
                int toMake = (toMakeMax % 2 == 0) ? 2 : 1;
                int from = opening.myPos + (opening.myNumChars - toMake);
                int to = iterator.getIndex() + toMake - 1;

                SequentialParser.Node node;
                if (toMake == 2) {
                    node = new Node(from, to + 1, MarkdownElementTypes.STRONG);
                } else {
                    node = new Node(from, to + 1, MarkdownElementTypes.EMPH);
                }
                result.withNode(node);
                openingOnes.subList(stackIndex, openingOnes.size()).clear();

                thisEmphWasEaten = true;
                for (int r = 0; r < toMake; r++) {
                    iterator = iterator.advance();
                }
                numCanEnd -= toMake;
                if (opening.myNumChars > toMake) {
                    openingOnes.add(new OpeningEmphInfo(opening.myPos, opening.myNumChars - toMake, opening.myType));
                }
            }

            if (thisEmphWasEaten) {
                continue;
            }

            int numCanStart = canStartNumber(iterator);
            if (numCanStart != 0) {
                openingOnes.add(new OpeningEmphInfo(iterator.getIndex(), numCanStart, getType(iterator)));
                for (int r = 0; r < numCanStart; r++) {
                    iterator = iterator.advance();
                }
                continue;
            }
            iterator = iterator.advance();
        }

        return result;
    }

    private int canStartNumber(TokensCache.Iterator iterator) {
        TokensCache.Iterator leftIt = iterator;
        while (leftIt.rawLookup(-1) == MarkdownTokenTypes.EMPH && getType(leftIt) == leftIt.charLookup(-1)) {
            leftIt = leftIt.rollback();
        }

        TokensCache.Iterator it = iterator;
        for (int i = 0; i <= 50 - 1; i++) {
            if (it.rawLookup(1) != MarkdownTokenTypes.EMPH || getType(it) != getType(it.advance())) {
                if (!isLeftFlankingRun(leftIt, it)) {
                    return 0;
                }
                if (getType(it) == ITALIC && isRightFlankingRun(leftIt, it) && !SequentialParserUtil.isPunctuation(leftIt, -1)) {
                    return 0;
                }
                return i + 1;
            }
            it = it.advance();
        }

        return 50;
    }

    private int canEndNumber(TokensCache.Iterator iterator) {
        TokensCache.Iterator it = iterator;
        if (SequentialParserUtil.isWhitespace(it, -1)) {
            return 0;
        }

        for (int i = 0; i <= 50 - 1; i++) {
            if (it.rawLookup(1) != MarkdownTokenTypes.EMPH || getType(it) != getType(it.advance())) {
                if (!isRightFlankingRun(iterator, it)) {
                    return 0;
                }
                if (getType(it) == ITALIC && isLeftFlankingRun(iterator, it) && !SequentialParserUtil.isPunctuation(it, 1)) {
                    return 0;
                }
                return i + 1;
            }
            it = it.advance();
        }

        return 50;
    }

    /**
     * See <a href="http://spec.commonmark.org/0.22/#left-flanking-delimiter-run">left flanking delimiter run</a>.
     */
    private boolean isLeftFlankingRun(TokensCache.Iterator leftIt, TokensCache.Iterator rightIt) {
        return !SequentialParserUtil.isWhitespace(rightIt, 1)
                && (!SequentialParserUtil.isPunctuation(rightIt, 1)
                    || SequentialParserUtil.isWhitespace(leftIt, -1)
                    || SequentialParserUtil.isPunctuation(leftIt, -1));
    }

    private boolean isRightFlankingRun(TokensCache.Iterator leftIt, TokensCache.Iterator rightIt) {
        return leftIt.charLookup(-1) != getType(leftIt)
                && !SequentialParserUtil.isWhitespace(leftIt, -1)
                && (!SequentialParserUtil.isPunctuation(leftIt, -1)
                    || SequentialParserUtil.isWhitespace(rightIt, 1)
                    || SequentialParserUtil.isPunctuation(rightIt, 1));
    }

    private char getType(TokensCache.Iterator info) {
        return info.getFirstChar();
    }

    private static final class OpeningEmphInfo {
        final int myPos;
        final int myNumChars;
        final char myType;

        OpeningEmphInfo(int pos, int numChars, char type) {
            myPos = pos;
            myNumChars = numChars;
            myType = type;
        }
    }
}
