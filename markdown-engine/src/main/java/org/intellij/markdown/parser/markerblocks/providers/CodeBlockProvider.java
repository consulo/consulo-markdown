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
import org.intellij.markdown.parser.markerblocks.MarkdownParserUtil;
import org.intellij.markdown.parser.markerblocks.MarkerBlock;
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider;
import org.intellij.markdown.parser.markerblocks.impl.CodeBlockMarkerBlock;

import java.util.Collections;
import java.util.List;

public final class CodeBlockProvider implements MarkerBlockProvider<MarkerProcessor.StateInfo> {

    @Override
    
    public List<MarkerBlock> createMarkerBlocks(LookaheadText.Position pos,
                                                ProductionHolder productionHolder,
                                                MarkerProcessor.StateInfo stateInfo) {
        if (MarkdownConstraints.getCharsEaten(stateInfo.getNextConstraints(), pos.getCurrentLine()) > pos.getOffsetInCurrentLine()) {
            return Collections.emptyList();
        }

        Integer charsToNonWhitespace = pos.charsToNonWhitespace();
        if (charsToNonWhitespace == null) {
            return Collections.emptyList();
        }
        LookaheadText.Position blockStart = pos.nextPosition(charsToNonWhitespace);
        if (blockStart == null) {
            return Collections.emptyList();
        }

        if (MarkdownParserUtil.hasCodeBlockIndent(blockStart, stateInfo.getCurrentConstraints())) {
            return Collections.singletonList(new CodeBlockMarkerBlock(stateInfo.getCurrentConstraints(), productionHolder, pos));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean interruptsParagraph(LookaheadText.Position pos,
                                       MarkdownConstraints constraints) {
        return false;
    }
}
