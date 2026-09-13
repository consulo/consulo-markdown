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
package org.intellij.markdown.parser.markerblocks.impl;

import consulo.language.ast.IElementType;
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.MarkdownTokenTypes;
import org.intellij.markdown.parser.LookaheadText;
import org.intellij.markdown.parser.ProductionHolder;
import org.intellij.markdown.parser.constraints.MarkdownConstraints;
import org.intellij.markdown.parser.markerblocks.MarkerBlock;
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AtxHeaderMarkerBlock extends MarkerBlockImpl {

    private final IElementType myNodeType;

    public AtxHeaderMarkerBlock(MarkdownConstraints constraints,
                                ProductionHolder productionHolder,
                                int headerRangeFirst,
                                int headerRangeLast,
                                int tailStartPos,
                                int endOfLinePos) {
        super(constraints, productionHolder.mark());

        int curPos = productionHolder.getCurrentPosition();
        List<SequentialParser.Node> nodes = new ArrayList<>();
        nodes.add(new SequentialParser.Node(curPos + headerRangeFirst, curPos + headerRangeLast + 1, MarkdownTokenTypes.ATX_HEADER));
        if (curPos + headerRangeLast + 1 != tailStartPos) {
            nodes.add(new SequentialParser.Node(curPos + headerRangeLast + 1, tailStartPos, MarkdownTokenTypes.ATX_CONTENT));
        }
        if (tailStartPos != endOfLinePos) {
            nodes.add(new SequentialParser.Node(tailStartPos, endOfLinePos, MarkdownTokenTypes.ATX_HEADER));
        }
        productionHolder.addProduction(nodes);

        myNodeType = calcNodeType(headerRangeLast - headerRangeFirst + 1);
    }

    @Override
    public boolean allowsSubBlocks() {
        return false;
    }

    @Override
    public boolean isInterestingOffset(LookaheadText.Position pos) {
        return true;
    }

    private static IElementType calcNodeType(int headerSize) {
        switch (headerSize) {
            case 1: return MarkdownElementTypes.ATX_1;
            case 2: return MarkdownElementTypes.ATX_2;
            case 3: return MarkdownElementTypes.ATX_3;
            case 4: return MarkdownElementTypes.ATX_4;
            case 5: return MarkdownElementTypes.ATX_5;
            case 6: return MarkdownElementTypes.ATX_6;
            default: return MarkdownElementTypes.ATX_6;
        }
    }

    @Override
    
    public IElementType getDefaultNodeType() {
        return myNodeType;
    }

    @Override
    
    protected MarkerBlock.ClosingAction getDefaultAction() {
        return MarkerBlock.ClosingAction.DONE;
    }

    @Override
    protected int calcNextInterestingOffset(LookaheadText.Position pos) {
        return pos.getNextLineOrEofOffset();
    }

    @Override
    
    protected MarkerBlock.ProcessingResult doProcessToken(LookaheadText.Position pos,
                                                          MarkdownConstraints currentConstraints) {
        if (pos.getOffsetInCurrentLine() == -1) {
            return new MarkerBlock.ProcessingResult(MarkerBlock.ClosingAction.DROP, MarkerBlock.ClosingAction.DONE, MarkerBlock.EventAction.PROPAGATE);
        }
        return MarkerBlock.ProcessingResult.CANCEL;
    }
}
