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

import org.intellij.markdown.parser.LookaheadText;
import org.intellij.markdown.parser.MarkerProcessor;
import org.intellij.markdown.parser.ProductionHolder;
import org.intellij.markdown.parser.constraints.MarkdownConstraints;

import java.util.List;

public interface MarkerBlockProvider<T extends MarkerProcessor.StateInfo> {
    
    List<MarkerBlock> createMarkerBlocks(LookaheadText.Position pos,
                                         ProductionHolder productionHolder,
                                         T stateInfo);

    boolean interruptsParagraph(LookaheadText.Position pos, MarkdownConstraints constraints);

    static boolean isStartOfLineWithConstraints(LookaheadText.Position pos,
                                                MarkdownConstraints constraints) {
        return pos.getOffsetInCurrentLine() == MarkdownConstraints.getCharsEaten(constraints, pos.getCurrentLine());
    }

    static int passSmallIndent(CharSequence text) {
        return passSmallIndent(text, 0);
    }

    static int passSmallIndent(CharSequence text, int startOffset) {
        int offset = startOffset;
        for (int i = 0; i < 3; i++) {
            if (offset < text.length() && text.charAt(offset) == ' ') {
                offset++;
            }
        }
        return offset;
    }
}
