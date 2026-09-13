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
package org.intellij.markdown.parser.markerblocks;

import org.intellij.markdown.lexer.Compat;
import org.intellij.markdown.parser.LookaheadText;
import org.intellij.markdown.parser.constraints.MarkdownConstraints;

import org.jspecify.annotations.Nullable;

public final class MarkdownParserUtil {

    private MarkdownParserUtil() {
    }

    public static int calcNumberOfConsequentEols(LookaheadText.Position pos,
                                                 MarkdownConstraints constraints) {
        Compat.assertCondition(pos.getOffsetInCurrentLine() == -1, "");

        LookaheadText.Position currentPos = pos;
        int result = 1;

        while (isClearLine(currentPos, constraints)) {
            currentPos = currentPos.nextLinePosition();
            if (currentPos == null) {
                break;
            }

            result++;
            if (result > 4) {
                break;
            }
        }
        return result;
    }

    private static boolean isClearLine(LookaheadText.Position pos,
                                       MarkdownConstraints constraints) {
        MarkdownConstraints currentConstraints = constraints.applyToNextLine(pos);
        int constraintsLength = MarkdownConstraints.getCharsEaten(currentConstraints, pos.getCurrentLine());

        if (!MarkdownConstraints.upstreamWith(currentConstraints, constraints)) {
            return false;
        }

        if (constraintsLength >= pos.getCurrentLine().length()) {
            return true;
        }

        LookaheadText.Position nextPos = pos.nextPosition(1 + constraintsLength);
        return nextPos != null && nextPos.charsToNonWhitespace() == null;
    }

    public static LookaheadText.@Nullable Position getFirstNonWhitespaceLinePos(LookaheadText.Position pos,
                                                                       int eolsToSkip) {
        LookaheadText.Position currentPos = pos;
        for (int i = 0; i < eolsToSkip - 1; i++) {
            currentPos = pos.nextLinePosition();
            if (currentPos == null) {
                return null;
            }
        }
        while (currentPos.charsToNonWhitespace() == null) {
            currentPos = currentPos.nextLinePosition();
            if (currentPos == null) {
                return null;
            }
        }
        return currentPos;
    }

    public static boolean hasCodeBlockIndent(LookaheadText.Position pos,
                                             MarkdownConstraints constraints) {
        int constraintsLength = MarkdownConstraints.getCharsEaten(constraints, pos.getCurrentLine());

        if (pos.getOffsetInCurrentLine() >= constraintsLength + 4) {
            return true;
        }
        for (int i = constraintsLength; i <= pos.getOffsetInCurrentLine(); i++) {
            if (pos.getCurrentLine().charAt(i) == '\t') {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmptyOrSpaces(CharSequence s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != ' ' && c != '\t') {
                return false;
            }
        }
        return true;
    }

    public static LookaheadText.@Nullable Position findNonEmptyLineWithSameConstraints(MarkdownConstraints constraints,
                                                                              LookaheadText.Position pos) {
        LookaheadText.Position currentPos = pos;

        while (true) {
            MarkdownConstraints nextLineConstraints = MarkdownConstraints.applyToNextLineAndAddModifiers(constraints, currentPos);
            // kinda equals
            if (!(MarkdownConstraints.upstreamWith(nextLineConstraints, constraints)
                    && MarkdownConstraints.extendsPrev(nextLineConstraints, constraints))) {
                return null;
            }

            CharSequence stringAfterConstraints = MarkdownConstraints.eatItselfFromString(nextLineConstraints, currentPos.getCurrentLine());

            if (!MarkdownParserUtil.isEmptyOrSpaces(stringAfterConstraints)) {
                return currentPos;
            } else {
                currentPos = currentPos.nextLinePosition();
                if (currentPos == null) {
                    return null;
                }
            }
        }
    }
}
