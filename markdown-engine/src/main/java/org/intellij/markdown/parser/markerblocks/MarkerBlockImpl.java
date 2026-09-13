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

import consulo.language.ast.IElementType;
import org.intellij.markdown.parser.LookaheadText;
import org.intellij.markdown.parser.ProductionHolder;
import org.intellij.markdown.parser.constraints.MarkdownConstraints;

import org.jspecify.annotations.Nullable;

public abstract class MarkerBlockImpl implements MarkerBlock {

    protected final MarkdownConstraints myConstraints;
    protected final ProductionHolder.Marker myMarker;

    private int myLastInterestingOffset = -2;

    private MarkerBlock.@Nullable ProcessingResult myScheduledResult = null;

    protected MarkerBlockImpl(MarkdownConstraints constraints,
                              ProductionHolder.Marker marker) {
        myConstraints = constraints;
        myMarker = marker;
    }

    @Override
    public final int getNextInterestingOffset(LookaheadText.Position pos) {
        if (myScheduledResult != null) {
            return pos.getOffset() + 1;
        }

        if (myLastInterestingOffset != -1 && myLastInterestingOffset <= pos.getOffset()) {
            myLastInterestingOffset = calcNextInterestingOffset(pos);
        }
        return myLastInterestingOffset;
    }

    @Override
    
    public final MarkerBlock.ProcessingResult processToken(LookaheadText.Position pos,
                                                           MarkdownConstraints currentConstraints) {
        if (myLastInterestingOffset != pos.getOffset() && myScheduledResult != null) {
            return MarkerBlock.ProcessingResult.CANCEL;
        }

        if (myLastInterestingOffset == -1 || myLastInterestingOffset > pos.getOffset()) {
            return MarkerBlock.ProcessingResult.PASS;
        }
        if (myLastInterestingOffset < pos.getOffset() && !isInterestingOffset(pos)) {
            return MarkerBlock.ProcessingResult.PASS;
        }
        if (myScheduledResult != null) {
            return myScheduledResult;
        }
        return doProcessToken(pos, currentConstraints);
    }

    @Override
    
    public final MarkdownConstraints getBlockConstraints() {
        return myConstraints;
    }

    @Override
    public boolean acceptAction(MarkerBlock.ClosingAction action) {
        MarkerBlock.ClosingAction actionToRun = action;
        if (actionToRun == MarkerBlock.ClosingAction.DEFAULT) {
            actionToRun = getDefaultAction();
        }

        actionToRun.doAction(myMarker, getDefaultNodeType());

        return actionToRun != MarkerBlock.ClosingAction.NOTHING;
    }

    protected void scheduleProcessingResult(int offset, MarkerBlock.ProcessingResult result) {
        myLastInterestingOffset = offset;
        myScheduledResult = result;
    }

    
    protected abstract MarkerBlock.ClosingAction getDefaultAction();

    
    protected abstract MarkerBlock.ProcessingResult doProcessToken(LookaheadText.Position pos,
                                                                   MarkdownConstraints currentConstraints);

    protected abstract int calcNextInterestingOffset(LookaheadText.Position pos);

    
    public abstract IElementType getDefaultNodeType();
}
