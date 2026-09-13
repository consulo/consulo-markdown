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
package org.intellij.markdown.parser.markerblocks.providers;

import org.intellij.markdown.parser.LookaheadText;
import org.intellij.markdown.parser.MarkerProcessor;
import org.intellij.markdown.parser.ProductionHolder;
import org.intellij.markdown.parser.constraints.MarkdownConstraints;
import org.intellij.markdown.parser.markerblocks.MarkerBlock;
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider;
import org.intellij.markdown.parser.markerblocks.impl.HorizontalRuleMarkerBlock;

import java.util.Collections;
import java.util.List;

public final class HorizontalRuleProvider implements MarkerBlockProvider<MarkerProcessor.StateInfo> {

    @Override
    
    public List<MarkerBlock> createMarkerBlocks(LookaheadText.Position pos,
                                                ProductionHolder productionHolder,
                                                MarkerProcessor.StateInfo stateInfo) {
        if (matches(pos, stateInfo.getCurrentConstraints())) {
            return Collections.singletonList(new HorizontalRuleMarkerBlock(stateInfo.getCurrentConstraints(), productionHolder.mark()));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean interruptsParagraph(LookaheadText.Position pos,
                                       MarkdownConstraints constraints) {
        return matches(pos, constraints);
    }

    public boolean matches(LookaheadText.Position pos,
                           MarkdownConstraints constraints) {
        if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, constraints)) {
            return false;
        }
        return isHorizontalRule(pos.getCurrentLine(), pos.getOffsetInCurrentLine());
    }

    public static boolean isHorizontalRule(CharSequence line, int offset) {
        Character hrChar = null;
        int startSpace = 0;
        int charCount = 1;
        for (int i = offset; i < line.length(); i++) {
            char c = line.charAt(i);
            if (hrChar == null) {
                if (c == '*' || c == '-' || c == '_') {
                    hrChar = c;
                } else if (startSpace < 3 && c == ' ') {
                    startSpace++;
                } else {
                    return false;
                }
            } else {
                if (c == hrChar) {
                    charCount++;
                } else if (c != ' ' && c != '\t') {
                    return false;
                }
            }
        }
        return charCount >= 3;
    }
}
