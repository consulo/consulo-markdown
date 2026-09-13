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

import consulo.language.ast.IElementType;

import java.util.List;

public abstract class DelimiterParser {

    public static final int MAX_ADVANCE = 50;

    /**
     * Finds all tokens that open or close element represented by this parser.
     * Invoked by {@link EmphasisLikeParser}.
     *
     * @return Number of steps to advance token iterator.
     */
    public abstract int scan(
            TokensCache tokens,
            TokensCache.Iterator iterator,
            List<Info> delimiters
    );

    /**
     * Receives a list of delimiters collected by {@link #scan}
     * (contains tokens from all parsers) and creates actual tree nodes from them.
     */
    public abstract void process(
            TokensCache tokens,
            TokensCache.Iterator iterator,
            List<Info> delimiters,
            SequentialParser.ParsingResultBuilder result
    );

    /**
     * Result of {@link #canOpenClose}: holds the canOpen and canClose flags.
     */
    public static final class OpenCloseResult {
        private final boolean myCanOpen;
        private final boolean myCanClose;

        public OpenCloseResult(boolean canOpen, boolean canClose) {
            myCanOpen = canOpen;
            myCanClose = canClose;
        }

        public boolean getCanOpen() {
            return myCanOpen;
        }

        public boolean getCanClose() {
            return myCanClose;
        }
    }

    /**
     * Checks if current token can open or close emphasis based on the current delimiter run.
     *
     * See <a href="https://spec.commonmark.org/0.30/#can-open-emphasis">Rules for opening and closing emphasises</a>.
     */
    
    public OpenCloseResult canOpenClose(
            TokensCache tokens,
            TokensCache.Iterator left,
            TokensCache.Iterator right,
            boolean canSplitText
    ) {
        boolean isLeftFlanking = isLeftFlankingRun(left, right);
        boolean isRightFlanking = isRightFlankingRun(tokens, left, right);
        boolean canOpen;
        boolean canClose;
        if (canSplitText) {
            canOpen = isLeftFlanking;
        } else {
            canOpen = isLeftFlanking && (!isRightFlanking || SequentialParserUtil.isPunctuation(left, -1));
        }
        if (canSplitText) {
            canClose = isRightFlanking;
        } else {
            canClose = isRightFlanking && (!isLeftFlanking || SequentialParserUtil.isPunctuation(right, 1));
        }
        return new OpenCloseResult(canOpen, canClose);
    }

    /**
     * See <a href="http://spec.commonmark.org/0.30/#left-flanking-delimiter-run">left flanking delimiter run</a>.
     */
    public boolean isLeftFlankingRun(TokensCache.Iterator leftIt, TokensCache.Iterator rightIt) {
        return !isWhitespace(rightIt, 1)
                && (!isPunctuation(rightIt, 1)
                    || isWhitespace(leftIt, -1)
                    || isPunctuation(leftIt, -1));
    }

    /**
     * See <a href="https://spec.commonmark.org/0.30/#right-flanking-delimiter-run">right flanking delimiter run</a>.
     */
    public boolean isRightFlankingRun(
            TokensCache tokens,
            TokensCache.Iterator leftIt,
            TokensCache.Iterator rightIt
    ) {
        return leftIt.charLookup(-1) != getType(leftIt)
                && !isWhitespace(leftIt, -1)
                && (!isPunctuation(leftIt, -1)
                    || isWhitespace(rightIt, 1)
                    || isPunctuation(rightIt, 1));
    }

    public boolean isWhitespace(TokensCache.Iterator info, int lookup) {
        return SequentialParserUtil.isWhitespace(info, lookup);
    }

    public boolean isPunctuation(TokensCache.Iterator info, int lookup) {
        return SequentialParserUtil.isPunctuation(info, lookup);
    }

    public static char getType(TokensCache.Iterator iterator) {
        return iterator.getFirstChar();
    }

    /**
     * Mutable delimiter info, used during delimiter collection and balancing.
     */
    public static final class Info {
        private final IElementType myTokenType;
        private final int myPosition;
        private final int myLength;
        private boolean myCanOpen;
        private boolean myCanClose;
        private char myMarker;
        private int myCloserIndex;

        public Info(
                IElementType tokenType,
                int position,
                int length,
                boolean canOpen,
                boolean canClose,
                char marker
        ) {
            myTokenType = tokenType;
            myPosition = position;
            myLength = length;
            myCanOpen = canOpen;
            myCanClose = canClose;
            myMarker = marker;
            myCloserIndex = -1;
        }

        public Info(
                IElementType tokenType,
                int position,
                boolean canOpen,
                boolean canClose,
                char marker
        ) {
            this(tokenType, position, 0, canOpen, canClose, marker);
        }

        
        public IElementType getTokenType() {
            return myTokenType;
        }

        public int getPosition() {
            return myPosition;
        }

        /**
         * Represents the length of the current delimiter run
         * and will be used to check rules 9 and 10 (rule of 3) for emphasises
         * on the delimiter balancing phase (will be skipped if equals to 0).
         */
        public int getLength() {
            return myLength;
        }

        public boolean getCanOpen() {
            return myCanOpen;
        }

        public void setCanOpen(boolean canOpen) {
            myCanOpen = canOpen;
        }

        public boolean getCanClose() {
            return myCanClose;
        }

        public void setCanClose(boolean canClose) {
            myCanClose = canClose;
        }

        public char getMarker() {
            return myMarker;
        }

        public void setMarker(char marker) {
            myMarker = marker;
        }

        /**
         * Index of the closing delimiter for the current one.
         * If it is less than 0, current delimiter does not have a matching closer.
         */
        public int getCloserIndex() {
            return myCloserIndex;
        }

        public void setCloserIndex(int closerIndex) {
            myCloserIndex = closerIndex;
        }
    }
}
