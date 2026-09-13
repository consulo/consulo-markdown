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
import org.intellij.markdown.parser.markerblocks.impl.SetextHeaderMarkerBlock;

import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class SetextHeaderProvider implements MarkerBlockProvider<MarkerProcessor.StateInfo> {

    private static final Pattern REGEX = Pattern.compile("^ {0,3}(-+|=+) *$");

    @Override
    
    public List<MarkerBlock> createMarkerBlocks(LookaheadText.Position pos,
                                                ProductionHolder productionHolder,
                                                MarkerProcessor.StateInfo stateInfo) {
        if (stateInfo.getParagraphBlock() != null) {
            return Collections.emptyList();
        }
        MarkdownConstraints currentConstraints = stateInfo.getCurrentConstraints();
        if (stateInfo.getNextConstraints() != currentConstraints) {
            return Collections.emptyList();
        }
        CharSequence nextLine = getNextLineFromConstraints(pos, currentConstraints);
        if (MarkerBlockProvider.isStartOfLineWithConstraints(pos, currentConstraints)
                && nextLine != null && REGEX.matcher(nextLine).matches()) {
            return Collections.singletonList(new SetextHeaderMarkerBlock(currentConstraints, productionHolder));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean interruptsParagraph(LookaheadText.Position pos,
                                       MarkdownConstraints constraints) {
        return false;
    }

    private @Nullable CharSequence getNextLineFromConstraints(LookaheadText.Position pos,
                                                    MarkdownConstraints constraints) {
        String line = pos.getNextLine();
        if (line == null) {
            return null;
        }
        LookaheadText.Position nextLinePos = pos.nextLinePosition();
        MarkdownConstraints nextLineConstraints = constraints.applyToNextLine(nextLinePos);
        if (MarkdownConstraints.extendsPrev(nextLineConstraints, constraints)) {
            return MarkdownConstraints.eatItselfFromString(nextLineConstraints, line);
        } else {
            return null;
        }
    }
}
