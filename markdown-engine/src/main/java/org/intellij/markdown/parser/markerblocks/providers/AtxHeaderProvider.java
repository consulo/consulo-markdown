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
import org.intellij.markdown.parser.markerblocks.impl.AtxHeaderMarkerBlock;

import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public final class AtxHeaderProvider implements MarkerBlockProvider<MarkerProcessor.StateInfo> {

    @Override
    
    public List<MarkerBlock> createMarkerBlocks(LookaheadText.Position pos,
                                                ProductionHolder productionHolder,
                                                MarkerProcessor.StateInfo stateInfo) {
        int[] headerRange = matches(pos);
        if (headerRange != null) {
            return Collections.singletonList(new AtxHeaderMarkerBlock(
                    stateInfo.getCurrentConstraints(),
                    productionHolder,
                    headerRange[0],
                    headerRange[1],
                    calcTailStartPos(pos, headerRange[1]),
                    pos.getNextLineOrEofOffset()));
        } else {
            return Collections.emptyList();
        }
    }

    private int calcTailStartPos(LookaheadText.Position pos, int headerSize) {
        CharSequence line = pos.getCurrentLineFromPosition();
        int offset = line.length() - 1;
        while (offset > headerSize && Character.isWhitespace(line.charAt(offset))) {
            offset--;
        }
        while (offset > headerSize && line.charAt(offset) == '#' && line.charAt(offset - 1) != '\\') {
            offset--;
        }
        if (offset + 1 < line.length() && Character.isWhitespace(line.charAt(offset)) && line.charAt(offset + 1) == '#') {
            return pos.getOffset() + offset + 1;
        } else {
            return pos.getOffset() + line.length();
        }
    }

    @Override
    public boolean interruptsParagraph(LookaheadText.Position pos,
                                       MarkdownConstraints constraints) {
        return matches(pos) != null;
    }

    /**
     * Returns an int[2] array with {start, end} (inclusive) or null if no match.
     */
    @Nullable
    private int[] matches(LookaheadText.Position pos) {
        if (pos.getOffsetInCurrentLine() != -1) {
            CharSequence text = pos.getCurrentLineFromPosition();
            int offset = MarkerBlockProvider.passSmallIndent(text);
            if (offset >= text.length() || text.charAt(offset) != '#') {
                return null;
            }

            int start = offset;
            for (int i = 0; i < 6; i++) {
                if (offset < text.length() && text.charAt(offset) == '#') {
                    offset++;
                }
            }

            if (offset < text.length() && text.charAt(offset) != ' ' && text.charAt(offset) != '\t') {
                return null;
            }
            return new int[]{start, offset - 1};
        }
        return null;
    }
}
