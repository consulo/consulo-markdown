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
package org.intellij.markdown.parser;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class LookaheadText {
    private final CharSequence myText;
    private final List<String> myLines;
    private final Position myStartPosition;

    public LookaheadText(CharSequence text) {
        myText = text;
        myLines = splitByNewline(text);
        if (text.length() > 0) {
            myStartPosition = new Position(0, -1, -1).nextPosition(1);
        } else {
            myStartPosition = null;
        }
    }

    public @Nullable Position getStartPosition() {
        return myStartPosition;
    }

    
    private static List<String> splitByNewline(CharSequence text) {
        List<String> result = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                result.add(text.subSequence(start, i).toString());
                start = i + 1;
            }
        }
        result.add(text.subSequence(start, text.length()).toString());
        return result;
    }

    public final class Position {
        private final int myLineN;
        private final int myLocalPos; // -1 if on newline before
        private final int myGlobalPos;
        private final String myCurrentLine;

        Position(int lineN, int localPos, int globalPos) {
            myLineN = lineN;
            myLocalPos = localPos;
            myGlobalPos = globalPos;
            myCurrentLine = myLines.get(lineN);
            assert myLocalPos >= -1 && myLocalPos < myCurrentLine.length();
        }

        
        public CharSequence getOriginalText() {
            return myText;
        }

        
        public String getCurrentLine() {
            return myCurrentLine;
        }

        public int getOffset() {
            return myGlobalPos;
        }

        public int getOffsetInCurrentLine() {
            return myLocalPos;
        }

        public @Nullable Integer getNextLineOffset() {
            if (myLineN + 1 < myLines.size()) {
                return myGlobalPos + (myCurrentLine.length() - myLocalPos);
            }
            return null;
        }

        public int getNextLineOrEofOffset() {
            return myGlobalPos + (myCurrentLine.length() - myLocalPos);
        }

        
        public CharSequence getTextFromPosition() {
            return myText.subSequence(myGlobalPos, myText.length());
        }

        
        public CharSequence getCurrentLineFromPosition() {
            return myCurrentLine.substring(getOffsetInCurrentLine());
        }

        public @Nullable String getNextLine() {
            if (myLineN + 1 < myLines.size()) {
                return myLines.get(myLineN + 1);
            }
            return null;
        }

        public @Nullable String getPrevLine() {
            if (myLineN > 0) {
                return myLines.get(myLineN - 1);
            }
            return null;
        }

        public char getChar() {
            return myText.charAt(myGlobalPos);
        }

        public @Nullable Position nextPosition() {
            return nextPosition(1);
        }

        public @Nullable Position nextPosition(int delta) {
            int remaining = delta;
            Position currentPosition = this;

            while (true) {
                if (remaining == 0) {
                    return currentPosition;
                }
                if (currentPosition.myLocalPos + remaining < currentPosition.myCurrentLine.length()) {
                    return new Position(
                            currentPosition.myLineN,
                            currentPosition.myLocalPos + remaining,
                            currentPosition.myGlobalPos + remaining);
                } else {
                    Integer nextLine = currentPosition.getNextLineOffset();
                    if (nextLine == null) {
                        return null;
                    } else {
                        int payload = currentPosition.myCurrentLine.length() - currentPosition.myLocalPos;
                        currentPosition = new Position(
                                currentPosition.myLineN + 1,
                                -1,
                                currentPosition.myGlobalPos + payload);
                        remaining -= payload;
                    }
                }
            }
        }

        public @Nullable Position nextLinePosition() {
            Integer nextLine = getNextLineOffset();
            if (nextLine == null) {
                return null;
            }
            return nextPosition(nextLine - getOffset());
        }

        public @Nullable Integer charsToNonWhitespace() {
            String line = myCurrentLine;
            int offset = Math.max(myLocalPos, 0);
            while (offset < line.length()) {
                char c = line.charAt(offset);
                if (c != ' ' && c != '\t') {
                    return offset - myLocalPos;
                }
                offset++;
            }
            return null;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other == null) return false;
            if (getClass() != other.getClass()) return false;
            Position that = (Position) other;
            return myGlobalPos == that.myGlobalPos;
        }

        @Override
        public int hashCode() {
            return myGlobalPos;
        }

        @Override
        public String toString() {
            String content;
            if (myLocalPos == -1) {
                content = "\\n" + myCurrentLine;
            } else {
                content = myCurrentLine.substring(myLocalPos);
            }
            return "Position: '" + content + "'";
        }
    }
}
