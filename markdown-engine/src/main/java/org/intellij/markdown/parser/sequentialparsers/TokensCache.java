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
import org.intellij.markdown.lexer.TokenInfo;

import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class TokensCache {

    
    public abstract List<TokenInfo> getCachedTokens();

    
    public abstract List<TokenInfo> getFilteredTokens();

    
    public abstract CharSequence getOriginalText();

    public abstract int getOriginalTextStart();

    public abstract int getOriginalTextEnd();

    public char getRawCharAt(int index) {
        if (index < getOriginalTextStart()) return 0;
        if (index > getOriginalTextEnd()) return 0;
        return getOriginalText().charAt(index);
    }

    protected void verify() {
        List<TokenInfo> cached = getCachedTokens();
        for (int i = 0; i < cached.size(); i++) {
            assert cached.get(i).getRawIndex() == i;
        }
        List<TokenInfo> filtered = getFilteredTokens();
        for (int i = 0; i < filtered.size(); i++) {
            assert filtered.get(i).getNormIndex() == i;
        }
    }

    public class RangesListIterator extends Iterator {
        private final List<IntRange> myRanges;
        private final int myListIndex;

        private RangesListIterator(List<IntRange> ranges, int listIndex, int value) {
            super(value);
            myRanges = ranges;
            myListIndex = listIndex;
        }

        public RangesListIterator(List<IntRange> ranges) {
            this(ranges, 0, !ranges.isEmpty() ? ranges.get(0).getStart() : -1);
        }

        @Override
        
        public RangesListIterator advance() {
            if (myListIndex >= myRanges.size()) {
                return this;
            }
            if (getIndex() == myRanges.get(myListIndex).getLast()) {
                int nextIndex = myListIndex + 1;
                int nextValue = nextIndex < myRanges.size()
                        ? myRanges.get(nextIndex).getStart()
                        : getFilteredTokens().size();
                return new RangesListIterator(myRanges, nextIndex, nextValue);
            }
            return new RangesListIterator(myRanges, myListIndex, getIndex() + 1);
        }

        @Override
        
        public RangesListIterator rollback() {
            if (myListIndex < 0) {
                return this;
            }
            if (getIndex() == myRanges.get(myListIndex).getFirst()) {
                int prevIndex = myListIndex - 1;
                int prevValue = prevIndex >= 0 && prevIndex < myRanges.size()
                        ? myRanges.get(prevIndex).getEndInclusive()
                        : -1;
                return new RangesListIterator(myRanges, prevIndex, prevValue);
            }
            return new RangesListIterator(myRanges, myListIndex, getIndex() - 1);
        }

        @Override
        public @Nullable IElementType rawLookup(int steps) {
            if (myListIndex < 0 || myListIndex >= myRanges.size()) {
                return null;
            }
            IntRange currentRange = myRanges.get(myListIndex);
            if (!currentRange.contains(getIndex() + steps)) {
                return null;
            }
            return super.rawLookup(steps);
        }
    }

    public class Iterator {
        private final int myIndex;

        public Iterator(int index) {
            myIndex = index;
        }

        public int getIndex() {
            return myIndex;
        }

        public @Nullable IElementType getType() {
            return info(0).getType();
        }

        public char getFirstChar() {
            return getRawCharAt(info(0).getTokenStart());
        }

        public int getLength() {
            return info(0).getTokenEnd() - info(0).getTokenStart();
        }

        public int getStart() {
            return info(0).getTokenStart();
        }

        public int getEnd() {
            return info(0).getTokenEnd();
        }

        
        public Iterator advance() {
            return new Iterator(myIndex + 1);
        }

        
        public Iterator rollback() {
            return new Iterator(myIndex - 1);
        }

        
        private TokenInfo info(int rawSteps) {
            List<TokenInfo> filtered = getFilteredTokens();
            List<TokenInfo> cached = getCachedTokens();

            if (myIndex < 0) {
                return new TokenInfo(null, getOriginalTextStart(), getOriginalTextStart(), 0, 0);
            } else if (myIndex > filtered.size()) {
                return new TokenInfo(null, getOriginalTextEnd() + 1, getOriginalTextEnd() + 1, 0, 0);
            }

            int rawIndex;
            if (myIndex < filtered.size()) {
                rawIndex = filtered.get(myIndex).getRawIndex() + rawSteps;
            } else {
                rawIndex = cached.size() + rawSteps;
            }

            if (rawIndex < 0) {
                return new TokenInfo(null, getOriginalTextStart(), getOriginalTextStart(), 0, 0);
            } else if (rawIndex >= cached.size()) {
                return new TokenInfo(null, getOriginalTextEnd() + 1, getOriginalTextEnd() + 1, 0, 0);
            }

            return cached.get(rawIndex);
        }

        public @Nullable IElementType rawLookup(int steps) {
            return info(steps).getType();
        }

        public int rawStart(int steps) {
            return info(steps).getTokenStart();
        }

        public char charLookup(int steps) {
            if (steps == 0) {
                return getRawCharAt(getStart());
            }
            if (steps == 1) {
                return getRawCharAt(getEnd());
            } else if (steps == -1) {
                return getRawCharAt(getStart() - 1);
            } else {
                int pos = steps > 0 ? rawStart(steps) : rawStart(steps + 1) - 1;
                return getRawCharAt(pos);
            }
        }

        @Override
        public String toString() {
            return "Iterator: " + myIndex + ": " + getType();
        }
    }
}
