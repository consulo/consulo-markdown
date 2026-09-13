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
package org.intellij.markdown.flavours.gfm.table;

import org.intellij.markdown.parser.LookaheadText;
import org.intellij.markdown.parser.MarkerProcessor;
import org.intellij.markdown.parser.ProductionHolder;
import org.intellij.markdown.parser.constraints.MarkdownConstraints;
import org.intellij.markdown.parser.markerblocks.MarkerBlock;
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider;

import java.util.Collections;
import java.util.List;

public final class GitHubTableMarkerProvider implements MarkerBlockProvider<MarkerProcessor.StateInfo> {

    @Override
    public List<MarkerBlock> createMarkerBlocks(LookaheadText.Position pos,
                                                 ProductionHolder productionHolder,
                                                 MarkerProcessor.StateInfo stateInfo) {
        MarkdownConstraints currentConstraints = stateInfo.myCurrentConstraints;
        if (!stateInfo.myNextConstraints.equals(currentConstraints)) {
            return Collections.emptyList();
        }

        CharSequence currentLineFromPosition = pos.getCurrentLineFromPosition();
        if (!containsPipe(currentLineFromPosition)) {
            return Collections.emptyList();
        }

        List<String> split = GitHubTableMarkerBlock.splitByPipes(currentLineFromPosition);
        int numberOfHeaderCells = 0;
        for (int i = 0; i < split.size(); i++) {
            if ((i > 0 && i < split.size() - 1) || !split.get(i).trim().isEmpty()) {
                numberOfHeaderCells++;
            }
        }
        if (numberOfHeaderCells == 0) {
            return Collections.emptyList();
        }
        CharSequence nextLine = getNextLineFromConstraints(pos, currentConstraints);
        if (nextLine == null) {
            return Collections.emptyList();
        }
        if (countSecondLineCells(nextLine) == numberOfHeaderCells) {
            return Collections.singletonList(
                    new GitHubTableMarkerBlock(pos, currentConstraints, productionHolder, numberOfHeaderCells));
        }
        return Collections.emptyList();
    }

    @Override
    public boolean interruptsParagraph(LookaheadText.Position pos, MarkdownConstraints constraints) {
        return false;
    }

    private CharSequence getNextLineFromConstraints(LookaheadText.Position pos, MarkdownConstraints constraints) {
        String line = pos.getNextLine();
        if (line == null) {
            return null;
        }
        LookaheadText.Position nextLinePosition = pos.nextLinePosition();
        MarkdownConstraints nextLineConstraints = constraints.applyToNextLine(nextLinePosition);
        if (MarkdownConstraints.extendsPrev(nextLineConstraints, constraints)) {
            return MarkdownConstraints.eatItselfFromString(nextLineConstraints, line);
        } else {
            return null;
        }
    }

    private static boolean containsPipe(CharSequence text) {
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '|') {
                return true;
            }
        }
        return false;
    }

    /**
     * @return number of cells in the separator line
     */
    public static int countSecondLineCells(CharSequence line) {
        int offset = passWhiteSpaces(line, 0);
        if (offset < line.length() && line.charAt(offset) == '|') {
            offset++;
        }

        int result = 0;
        while (offset < line.length()) {
            offset = passWhiteSpaces(line, offset);
            if (offset < line.length() && line.charAt(offset) == ':') {
                offset++;
                offset = passWhiteSpaces(line, offset);
            }

            int dashes = 0;
            while (offset < line.length() && line.charAt(offset) == '-') {
                offset++;
                dashes++;
            }

            if (dashes < 1) {
                return 0;
            }
            result++;

            offset = passWhiteSpaces(line, offset);
            if (offset < line.length() && line.charAt(offset) == ':') {
                offset++;
                offset = passWhiteSpaces(line, offset);
            }

            if (offset < line.length() && line.charAt(offset) == '|') {
                offset++;
                offset = passWhiteSpaces(line, offset);
            } else {
                break;
            }
        }

        if (offset == line.length()) {
            return result;
        } else {
            return 0;
        }
    }

    public static int passWhiteSpaces(CharSequence line, int offset) {
        int curOffset = offset;
        while (curOffset < line.length()) {
            if (line.charAt(curOffset) != ' ' && line.charAt(curOffset) != '\t') {
                break;
            }
            curOffset++;
        }
        return curOffset;
    }
}
