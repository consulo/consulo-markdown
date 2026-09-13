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
import org.intellij.markdown.parser.markerblocks.impl.ListItemMarkerBlock;
import org.intellij.markdown.parser.markerblocks.impl.ListMarkerBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ListMarkerProvider implements MarkerBlockProvider<MarkerProcessor.StateInfo> {

    @Override
    
    public List<MarkerBlock> createMarkerBlocks(LookaheadText.Position pos,
                                                ProductionHolder productionHolder,
                                                MarkerProcessor.StateInfo stateInfo) {
        MarkdownConstraints currentConstraints = stateInfo.getCurrentConstraints();
        MarkdownConstraints nextConstraints = stateInfo.getNextConstraints();

        if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, currentConstraints)) {
            return Collections.emptyList();
        }
        char[] types = nextConstraints.getTypes();
        if (nextConstraints != currentConstraints
                && types.length > 0 && types[types.length - 1] != '>'
                && getLastExplicit(nextConstraints) == Boolean.TRUE) {
            List<MarkerBlock> result = new ArrayList<>();
            if (!(stateInfo.getLastBlock() instanceof ListMarkerBlock)) {
                result.add(new ListMarkerBlock(nextConstraints, productionHolder.mark(), types[types.length - 1]));
            }
            result.add(new ListItemMarkerBlock(nextConstraints, productionHolder.mark()));
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean interruptsParagraph(LookaheadText.Position pos,
                                       MarkdownConstraints constraints) {
        // Actually, list item interrupts a paragraph, but we have MarkdownConstraints for these cases
        return false;
    }

    private static Boolean getLastExplicit(MarkdownConstraints constraints) {
        boolean[] isExplicit = constraints.getIsExplicit();
        if (isExplicit.length == 0) {
            return null;
        }
        return isExplicit[isExplicit.length - 1];
    }
}
