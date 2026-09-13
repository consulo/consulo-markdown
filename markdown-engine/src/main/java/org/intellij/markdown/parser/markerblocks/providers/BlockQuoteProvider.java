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
import org.intellij.markdown.parser.markerblocks.impl.BlockQuoteMarkerBlock;

import java.util.Collections;
import java.util.List;

public final class BlockQuoteProvider implements MarkerBlockProvider<MarkerProcessor.StateInfo> {

    @Override
    
    public List<MarkerBlock> createMarkerBlocks(LookaheadText.Position pos,
                                                ProductionHolder productionHolder,
                                                MarkerProcessor.StateInfo stateInfo) {
        MarkdownConstraints currentConstraints = stateInfo.getCurrentConstraints();
        MarkdownConstraints nextConstraints = stateInfo.getNextConstraints();
        if (pos.getOffsetInCurrentLine() != MarkdownConstraints.getCharsEaten(currentConstraints, pos.getCurrentLine())) {
            return Collections.emptyList();
        }
        char[] types = nextConstraints.getTypes();
        if (nextConstraints != currentConstraints && types.length > 0 && types[types.length - 1] == '>') {
            return Collections.singletonList(new BlockQuoteMarkerBlock(nextConstraints, productionHolder.mark()));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean interruptsParagraph(LookaheadText.Position pos,
                                       MarkdownConstraints constraints) {
        // Actually, blockquote may interrupt a paragraph, but we have MarkdownConstraints for these cases
        return false;
    }
}
