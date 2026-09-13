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

public interface MarkdownConstraints {
    int getIndent();

    char[] getTypes();

    boolean[] getIsExplicit();

    int getCharsEaten();

    boolean startsWith(MarkdownConstraints other);

    boolean containsListMarkers(int upToIndex);

    MarkdownConstraints addModifierIfNeeded(LookaheadText.Position pos);

    MarkdownConstraints applyToNextLine(LookaheadText.Position pos);

    static boolean extendsList(MarkdownConstraints self, MarkdownConstraints other) {
        if (other.getTypes().length == 0) {
            throw new IllegalArgumentException("List constraints should contain at least one item");
        }
        return self.startsWith(other) && !self.containsListMarkers(other.getTypes().length - 1);
    }

    static boolean extendsPrev(MarkdownConstraints self, MarkdownConstraints other) {
        return self.startsWith(other) && !self.containsListMarkers(other.getTypes().length);
    }

    static boolean upstreamWith(MarkdownConstraints self, MarkdownConstraints other) {
        return other.startsWith(self) && !self.containsListMarkers(self.getTypes().length);
    }

    static CharSequence eatItselfFromString(MarkdownConstraints self, CharSequence s) {
        if (s.length() < self.getCharsEaten()) {
            return "";
        } else {
            return s.subSequence(self.getCharsEaten(), s.length());
        }
    }

    static MarkdownConstraints applyToNextLineAndAddModifiers(MarkdownConstraints self, LookaheadText.Position pos) {
        Compat.assertCondition(pos.getOffsetInCurrentLine() == -1, "");

        MarkdownConstraints result = self.applyToNextLine(pos);
        String line = pos.getCurrentLine();

        while (true) {
            int offset = getCharsEaten(result, line);
            LookaheadText.Position nextPos = pos.nextPosition(1 + offset);
            if (nextPos == null) {
                break;
            }
            MarkdownConstraints added = result.addModifierIfNeeded(nextPos);
            if (added == null) {
                break;
            }
            result = added;
        }

        return result;
    }

    static int getCharsEaten(MarkdownConstraints self, CharSequence s) {
        return Math.min(self.getCharsEaten(), s.length());
    }
}
