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
package org.intellij.markdown.parser.constraints;

import org.intellij.markdown.lexer.Compat;
import org.intellij.markdown.parser.LookaheadText;
import org.intellij.markdown.parser.markerblocks.providers.HorizontalRuleProvider;

import java.util.Arrays;

public class CommonMarkdownConstraints implements MarkdownConstraints {
    public static final CommonMarkdownConstraints BASE = new CommonMarkdownConstraints(new int[0], new char[0], new boolean[0], 0);
    public static final char BQ_CHAR = '>';

    private final int[] myIndents;
    private final char[] myTypes;
    private final boolean[] myIsExplicit;
    private final int myCharsEaten;

    protected CommonMarkdownConstraints(int[] indents, char[] types, boolean[] isExplicit, int charsEaten) {
        myIndents = indents;
        myTypes = types;
        myIsExplicit = isExplicit;
        myCharsEaten = charsEaten;
    }

    protected CommonMarkdownConstraints getBase() {
        return BASE;
    }

    protected CommonMarkdownConstraints createNewConstraints(int[] indents, char[] types, boolean[] isExplicit, int charsEaten) {
        return new CommonMarkdownConstraints(indents, types, isExplicit, charsEaten);
    }

    @Override
    public int getIndent() {
        return myIndents.length > 0 ? myIndents[myIndents.length - 1] : 0;
    }

    @Override
    public char[] getTypes() {
        return myTypes;
    }

    @Override
    public boolean[] getIsExplicit() {
        return myIsExplicit;
    }

    @Override
    public int getCharsEaten() {
        return myCharsEaten;
    }

    @Override
    public boolean startsWith(MarkdownConstraints other) {
        if (!(other instanceof CommonMarkdownConstraints)) return false;
        CommonMarkdownConstraints otherCmc = (CommonMarkdownConstraints) other;
        int n = myIndents.length;
        int m = otherCmc.myIndents.length;

        if (n < m) {
            return false;
        }
        // Kotlin: (0 until m).none { types[it] != other.types[it] }
        // Means: there is NO index where types differ, i.e., all types match
        for (int i = 0; i < m; i++) {
            if (myTypes[i] != otherCmc.myTypes[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean containsListMarkers(int upToIndex) {
        for (int i = 0; i < upToIndex; i++) {
            if (myTypes[i] != BQ_CHAR && myIsExplicit[i]) {
                return true;
            }
        }
        return false;
    }

    @Override
    public CommonMarkdownConstraints addModifierIfNeeded(LookaheadText.Position pos) {
        if (pos == null || pos.getOffsetInCurrentLine() == -1) {
            return null;
        }
        if (HorizontalRuleProvider.isHorizontalRule(pos.getCurrentLine(), pos.getOffsetInCurrentLine())) {
            return null;
        }
        CommonMarkdownConstraints result = tryAddListItem(pos);
        if (result != null) {
            return result;
        }
        return tryAddBlockQuote(pos);
    }

    @Override
    public CommonMarkdownConstraints applyToNextLine(LookaheadText.Position pos) {
        if (pos == null) {
            return getBase();
        }
        Compat.assertCondition(pos.getOffsetInCurrentLine() == -1, "given " + pos);

        String line = pos.getCurrentLine();
        int prevN = myIndents.length;
        int[] indexPrevHolder = {0};

        CommonMarkdownConstraints result = getBase();
        while (true) {
            CommonMarkdownConstraints nextConstraints = fillMaybeBlockquoteAndListIndents(result, line, prevN, indexPrevHolder);
            if (nextConstraints == result) {
                return result;
            }
            result = nextConstraints;
        }
    }

    private CommonMarkdownConstraints fillMaybeBlockquoteAndListIndents(
            CommonMarkdownConstraints constraints, String line, int prevN, int[] indexPrevHolder) {
        int indexPrev = indexPrevHolder[0];
        if (indexPrev >= prevN) {
            return constraints;
        }

        int offset = MarkdownConstraints.getCharsEaten(constraints, line);
        int totalSpaces = 0;
        int[] spacesSeen = {0};
        int[] offsetHolder = {offset};
        int[] totalSpacesHolder = {totalSpaces};

        // hasKMoreSpaces lambda equivalent
        class SpacesChecker {
            boolean hasKMoreSpaces(int k) {
                int oldSpacesSeen = spacesSeen[0];
                int oldOffset = offsetHolder[0];
                while (spacesSeen[0] < k && offsetHolder[0] < line.length()) {
                    char c = line.charAt(offsetHolder[0]);
                    int deltaSpaces;
                    if (c == ' ') {
                        deltaSpaces = 1;
                    } else if (c == '\t') {
                        deltaSpaces = 4 - totalSpacesHolder[0] % 4;
                    } else {
                        break;
                    }
                    spacesSeen[0] += deltaSpaces;
                    totalSpacesHolder[0] += deltaSpaces;
                    offsetHolder[0]++;
                }
                if (offsetHolder[0] == line.length()) {
                    spacesSeen[0] = Integer.MAX_VALUE;
                }

                if (k <= spacesSeen[0]) {
                    spacesSeen[0] -= k;
                    return true;
                } else {
                    offsetHolder[0] = oldOffset;
                    spacesSeen[0] = oldSpacesSeen;
                    return false;
                }
            }
        }
        SpacesChecker spacesChecker = new SpacesChecker();

        Integer bqIndent = null;
        if (myTypes[indexPrev] == BQ_CHAR) {
            bqIndent = getBlockQuoteIndent(line, offsetHolder[0]);
            if (bqIndent == null) {
                return constraints;
            }
            offsetHolder[0] += bqIndent;
            indexPrev++;
        }

        int oldIndexPrev = indexPrev;
        while (indexPrev < prevN && myTypes[indexPrev] != BQ_CHAR) {
            int deltaIndent = myIndents[indexPrev] - (indexPrev == 0 ? 0 : myIndents[indexPrev - 1]);

            if (!spacesChecker.hasKMoreSpaces(deltaIndent)) {
                break;
            }

            indexPrev++;
        }

        CommonMarkdownConstraints result = constraints;
        if (bqIndent != null) {
            int bonusForTheBlockquote = spacesChecker.hasKMoreSpaces(1) ? 1 : 0;
            result = create(result, bqIndent + bonusForTheBlockquote, BQ_CHAR, true, offsetHolder[0]);
        }
        for (int index = oldIndexPrev; index < indexPrev; index++) {
            int deltaIndent = myIndents[index] - (index == 0 ? 0 : myIndents[index - 1]);
            result = create(result, deltaIndent, myTypes[index], false, offsetHolder[0]);
        }

        indexPrevHolder[0] = indexPrev;
        return result;
    }

    private static Integer getBlockQuoteIndent(String line, int startOffset) {
        int offset = startOffset;
        int blockQuoteIndent = 0;

        while (blockQuoteIndent < 3 && offset < line.length() && line.charAt(offset) == ' ') {
            blockQuoteIndent++;
            offset++;
        }

        if (offset < line.length() && line.charAt(offset) == BQ_CHAR) {
            return blockQuoteIndent + 1;
        } else {
            return null;
        }
    }

    protected ListMarkerInfo fetchListMarker(LookaheadText.Position pos) {
        char c = pos.getChar();
        if (c == '*' || c == '-' || c == '+') {
            return new ListMarkerInfo(1, c, 1);
        }

        String line = pos.getCurrentLine();
        int offset = pos.getOffsetInCurrentLine();
        int startOffset = offset;
        while (offset < line.length() && line.charAt(offset) >= '0' && line.charAt(offset) <= '9') {
            offset++;
        }
        if (offset > startOffset
                && offset - startOffset <= 9
                && offset < line.length()
                && (line.charAt(offset) == '.' || line.charAt(offset) == ')')) {
            return new ListMarkerInfo(offset + 1 - startOffset, line.charAt(offset), offset + 1 - startOffset);
        }
        return null;
    }

    protected static class ListMarkerInfo {
        private final int myMarkerLength;
        private final char myMarkerType;
        private final int myMarkerIndent;

        public ListMarkerInfo(int markerLength, char markerType, int markerIndent) {
            myMarkerLength = markerLength;
            myMarkerType = markerType;
            myMarkerIndent = markerIndent;
        }

        public int getMarkerLength() {
            return myMarkerLength;
        }

        public char getMarkerType() {
            return myMarkerType;
        }

        public int getMarkerIndent() {
            return myMarkerIndent;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ListMarkerInfo)) return false;
            ListMarkerInfo that = (ListMarkerInfo) o;
            return myMarkerLength == that.myMarkerLength && myMarkerType == that.myMarkerType && myMarkerIndent == that.myMarkerIndent;
        }

        @Override
        public int hashCode() {
            int result = myMarkerLength;
            result = 31 * result + myMarkerType;
            result = 31 * result + myMarkerIndent;
            return result;
        }

        @Override
        public String toString() {
            return "ListMarkerInfo(markerLength=" + myMarkerLength + ", markerType=" + myMarkerType + ", markerIndent=" + myMarkerIndent + ")";
        }
    }

    private CommonMarkdownConstraints tryAddListItem(LookaheadText.Position pos) {
        String line = pos.getCurrentLine();

        int offset = pos.getOffsetInCurrentLine();
        int spacesBefore = (offset > 0 && line.charAt(offset - 1) == '\t')
                ? (4 - getIndent() % 4) % 4
                : 0;
        while (offset < line.length() && line.charAt(offset) == ' ' && spacesBefore < 3) {
            spacesBefore++;
            offset++;
        }
        if (offset == line.length()) {
            return null;
        }

        LookaheadText.Position nextPos = pos.nextPosition(offset - pos.getOffsetInCurrentLine());
        if (nextPos == null) {
            return null;
        }
        ListMarkerInfo markerInfo = fetchListMarker(nextPos);
        if (markerInfo == null) {
            return null;
        }

        offset += markerInfo.getMarkerLength();
        int spacesAfter = 0;

        int markerEndOffset = offset;
        afterSpaces:
        while (offset < line.length()) {
            switch (line.charAt(offset)) {
                case ' ':
                    spacesAfter++;
                    break;
                case '\t':
                    spacesAfter += 4 - spacesAfter % 4;
                    break;
                default:
                    break afterSpaces;
            }
            offset++;
        }

        // By the classification http://spec.commonmark.org/0.20/#list-items
        // 1. Basic case
        if (spacesAfter >= 1 && spacesAfter <= 4 && offset < line.length()) {
            return create(this, spacesBefore + markerInfo.getMarkerIndent() + spacesAfter, markerInfo.getMarkerType(), true, offset);
        }
        if ((spacesAfter >= 5 && offset < line.length()) // 2. Starts with an indented code
                || offset == line.length()) {
            // 3. Starts with an empty string
            return create(this, spacesBefore + markerInfo.getMarkerIndent() + 1, markerInfo.getMarkerType(), true,
                    Math.min(offset, markerEndOffset + 1));
        }

        return null;
    }

    private CommonMarkdownConstraints tryAddBlockQuote(LookaheadText.Position pos) {
        String line = pos.getCurrentLine();

        int offset = pos.getOffsetInCurrentLine();
        int spacesBefore = 0;
        while (offset < line.length() && line.charAt(offset) == ' ' && spacesBefore < 3) {
            spacesBefore++;
            offset++;
        }
        if (offset == line.length() || line.charAt(offset) != BQ_CHAR) {
            return null;
        }
        offset++;

        int spacesAfter = 0;
        if (offset >= line.length() || line.charAt(offset) == ' ' || line.charAt(offset) == '\t') {
            spacesAfter = 1;

            if (offset < line.length()) {
                offset++;
            }
        }

        return create(this, spacesBefore + 1 + spacesAfter, BQ_CHAR, true, offset);
    }

    @Override
    public String toString() {
        return "MdConstraints: " + new String(myTypes) + "(" + getIndent() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommonMarkdownConstraints)) return false;
        CommonMarkdownConstraints that = (CommonMarkdownConstraints) o;
        return myCharsEaten == that.myCharsEaten
                && Arrays.equals(myIndents, that.myIndents)
                && Arrays.equals(myTypes, that.myTypes)
                && Arrays.equals(myIsExplicit, that.myIsExplicit);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(myIndents);
        result = 31 * result + Arrays.hashCode(myTypes);
        result = 31 * result + Arrays.hashCode(myIsExplicit);
        result = 31 * result + myCharsEaten;
        return result;
    }

    private static CommonMarkdownConstraints create(CommonMarkdownConstraints parent,
                                                     int newIndentDelta,
                                                     char newType,
                                                     boolean newExplicit,
                                                     int newOffset) {
        int n = parent.myIndents.length;
        int[] indents = Arrays.copyOf(parent.myIndents, n + 1);
        char[] types = Arrays.copyOf(parent.myTypes, n + 1);
        boolean[] isExplicit = Arrays.copyOf(parent.myIsExplicit, n + 1);

        indents[n] = parent.getIndent() + newIndentDelta;
        types[n] = newType;
        isExplicit[n] = newExplicit;
        return parent.createNewConstraints(indents, types, isExplicit, newOffset);
    }
}
