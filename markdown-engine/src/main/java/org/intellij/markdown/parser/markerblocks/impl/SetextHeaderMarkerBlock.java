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

import java.util.Collections;

public final class SetextHeaderMarkerBlock extends MarkerBlockImpl {

    private final ProductionHolder myProductionHolder;
    private final ProductionHolder.Marker myContentMarker;
    private IElementType myNodeType = MarkdownElementTypes.SETEXT_1;

    public SetextHeaderMarkerBlock(MarkdownConstraints constraints,
                                   ProductionHolder productionHolder) {
        super(constraints, productionHolder.mark());
        myProductionHolder = productionHolder;
        myContentMarker = productionHolder.mark();
    }

    @Override
    public boolean allowsSubBlocks() {
        return false;
    }

    @Override
    public boolean isInterestingOffset(LookaheadText.Position pos) {
        return pos.getOffsetInCurrentLine() == -1;
    }

    @Override
    protected int calcNextInterestingOffset(LookaheadText.Position pos) {
        return pos.getNextLineOrEofOffset();
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
    
    protected MarkerBlock.ProcessingResult doProcessToken(LookaheadText.Position pos,
                                                          MarkdownConstraints currentConstraints) {
        if (pos.getOffsetInCurrentLine() != -1) {
            return MarkerBlock.ProcessingResult.CANCEL;
        }

        Integer startSpaces = pos.charsToNonWhitespace();
        if (startSpaces == null) {
            return new MarkerBlock.ProcessingResult(MarkerBlock.ClosingAction.DROP, MarkerBlock.ClosingAction.DROP, MarkerBlock.EventAction.PROPAGATE);
        }

        LookaheadText.Position setextMarkerStart = pos.nextPosition(startSpaces);
        if (setextMarkerStart != null && setextMarkerStart.getChar() == '-') {
            myNodeType = MarkdownElementTypes.SETEXT_2;
        }

        int setextMarkerStartOffset = setextMarkerStart != null ? setextMarkerStart.getOffset() : pos.getOffset();
        IElementType markerNodeType = (myNodeType == MarkdownElementTypes.SETEXT_2)
                ? MarkdownTokenTypes.SETEXT_2
                : MarkdownTokenTypes.SETEXT_1;

        myContentMarker.done(MarkdownTokenTypes.SETEXT_CONTENT);
        myProductionHolder.addProduction(Collections.singletonList(new SequentialParser.Node(
                setextMarkerStartOffset, pos.getNextLineOrEofOffset(), markerNodeType)));
        scheduleProcessingResult(pos.getNextLineOrEofOffset(), MarkerBlock.ProcessingResult.DEFAULT);
        return MarkerBlock.ProcessingResult.CANCEL;
    }
}
