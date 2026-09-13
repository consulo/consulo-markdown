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
package org.intellij.markdown.parser;

import org.intellij.markdown.lexer.Compat;
import org.intellij.markdown.parser.constraints.MarkdownConstraints;
import org.intellij.markdown.parser.markerblocks.MarkerBlock;
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider;
import org.intellij.markdown.parser.markerblocks.impl.ParagraphMarkerBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public abstract class MarkerProcessor<T extends MarkerProcessor.StateInfo> {
    private final ProductionHolder myProductionHolder;
    protected final MarkdownConstraints myStartConstraints;

    protected final List<MarkerBlock> NO_BLOCKS = Collections.emptyList();

    protected final List<MarkerBlock> myMarkersStack = new ArrayList<>();

    protected MarkdownConstraints myTopBlockConstraints;

    private int myNextInterestingPosForExistingMarkers = -1;

    private final BiFunction<LookaheadText.Position, MarkdownConstraints, Boolean> myInterruptsParagraph;

    public MarkerProcessor(ProductionHolder productionHolder, MarkdownConstraints startConstraints) {
        myProductionHolder = productionHolder;
        myStartConstraints = startConstraints;
        myTopBlockConstraints = startConstraints;
        myInterruptsParagraph = (position, constraints) -> {
            for (MarkerBlockProvider<T> provider : getMarkerBlockProviders()) {
                if (provider.interruptsParagraph(position, constraints)) {
                    return true;
                }
            }
            return false;
        };
    }

    protected abstract T getStateInfo();

    protected abstract List<MarkerBlockProvider<T>> getMarkerBlockProviders();

    protected abstract void updateStateInfo(LookaheadText.Position pos);

    protected abstract void populateConstraintsTokens(LookaheadText.Position pos,
                                                       MarkdownConstraints constraints,
                                                       ProductionHolder productionHolder);

    public List<MarkerBlock> createNewMarkerBlocks(LookaheadText.Position pos,
                                                    ProductionHolder productionHolder) {
        Compat.assertCondition(MarkerBlockProvider.isStartOfLineWithConstraints(pos, getStateInfo().myCurrentConstraints), "");

        for (MarkerBlockProvider<T> provider : getMarkerBlockProviders()) {
            List<MarkerBlock> list = provider.createMarkerBlocks(pos, productionHolder, getStateInfo());
            if (!list.isEmpty()) {
                return list;
            }
        }

        if (pos.getOffsetInCurrentLine() >= MarkdownConstraints.getCharsEaten(getStateInfo().myNextConstraints, pos.getCurrentLine())
                && pos.charsToNonWhitespace() != null) {
            return Collections.singletonList(
                    new ParagraphMarkerBlock(getStateInfo().myCurrentConstraints, productionHolder.mark(), myInterruptsParagraph));
        }

        return Collections.emptyList();
    }

    public LookaheadText.Position processPosition(LookaheadText.Position pos) {
        updateStateInfo(pos);

        boolean shouldRecalcNextPos = false;

        if (pos.getOffset() >= myNextInterestingPosForExistingMarkers) {
            processMarkers(pos);
            shouldRecalcNextPos = true;
        }

        MarkerBlock lastBlock = myMarkersStack.isEmpty() ? null : myMarkersStack.get(myMarkersStack.size() - 1);
        if (MarkerBlockProvider.isStartOfLineWithConstraints(pos, getStateInfo().myCurrentConstraints)
                && (lastBlock == null || lastBlock.allowsSubBlocks())) {
            List<MarkerBlock> newMarkerBlocks = createNewMarkerBlocks(pos, myProductionHolder);
            for (MarkerBlock newMarkerBlock : newMarkerBlocks) {
                addNewMarkerBlock(newMarkerBlock);
                shouldRecalcNextPos = true;
            }
        }

        if (shouldRecalcNextPos) {
            myNextInterestingPosForExistingMarkers = calculateNextPosForExistingMarkers(pos);
        }

        if (pos.getOffsetInCurrentLine() == -1
                || MarkerBlockProvider.isStartOfLineWithConstraints(pos, getStateInfo().myCurrentConstraints)) {
            int delta = MarkdownConstraints.getCharsEaten(getStateInfo().myNextConstraints, pos.getCurrentLine()) - pos.getOffsetInCurrentLine();
            if (delta > 0) {
                if (pos.getOffsetInCurrentLine() != -1 && getStateInfo().myNextConstraints.getIndent() <= myTopBlockConstraints.getIndent()) {
                    populateConstraintsTokens(pos, getStateInfo().myNextConstraints, myProductionHolder);
                }
                return pos.nextPosition(delta);
            }
        }

        return pos.nextPosition(myNextInterestingPosForExistingMarkers - pos.getOffset());
    }

    private int calculateNextPosForExistingMarkers(LookaheadText.Position pos) {
        int result;
        if (myMarkersStack.isEmpty()) {
            result = pos.getNextLineOrEofOffset();
        } else {
            result = myMarkersStack.get(myMarkersStack.size() - 1).getNextInterestingOffset(pos);
        }
        return result == -1 ? Integer.MAX_VALUE : result;
    }

    public void addNewMarkerBlock(MarkerBlock newMarkerBlock) {
        myMarkersStack.add(newMarkerBlock);
        relaxTopConstraints();
    }

    public void flushMarkers() {
        closeChildren(-1, MarkerBlock.ClosingAction.DEFAULT);
    }

    private boolean processMarkers(LookaheadText.Position pos) {
        int index = myMarkersStack.size();
        while (index > 0) {
            index--;
            if (index >= myMarkersStack.size()) {
                continue;
            }

            MarkerBlock markerBlock = myMarkersStack.get(index);
            MarkerBlock.ProcessingResult processingResult = markerBlock.processToken(pos, getStateInfo().myCurrentConstraints);
            if (processingResult == MarkerBlock.ProcessingResult.PASS) {
                continue;
            }

            applyProcessingResult(index, markerBlock, processingResult);

            if (processingResult.getEventAction() == MarkerBlock.EventAction.CANCEL) {
                return true;
            }
        }

        return false;
    }

    private void applyProcessingResult(int index, MarkerBlock markerBlock, MarkerBlock.ProcessingResult processingResult) {
        closeChildren(index, processingResult.getChildrenAction());

        // process self
        if (markerBlock.acceptAction(processingResult.getSelfAction())) {
            myMarkersStack.remove(index);
            relaxTopConstraints();
        }
    }

    private void closeChildren(int index, MarkerBlock.ClosingAction childrenAction) {
        if (childrenAction != MarkerBlock.ClosingAction.NOTHING) {
            int latterIndex = myMarkersStack.size() - 1;
            while (latterIndex > index) {
                boolean result = myMarkersStack.get(latterIndex).acceptAction(childrenAction);
                Compat.assertCondition(result, "If closing action is not NOTHING, marker should be gone");

                myMarkersStack.remove(latterIndex);
                --latterIndex;
            }
            relaxTopConstraints();
        }
    }

    private void relaxTopConstraints() {
        myTopBlockConstraints = myMarkersStack.isEmpty()
                ? myStartConstraints
                : myMarkersStack.get(myMarkersStack.size() - 1).getBlockConstraints();
    }

    public static class StateInfo {
        public final MarkdownConstraints myCurrentConstraints;
        public final MarkdownConstraints myNextConstraints;
        private final List<MarkerBlock> myMarkersStack;

        public StateInfo(MarkdownConstraints currentConstraints,
                         MarkdownConstraints nextConstraints,
                         List<MarkerBlock> markersStack) {
            myCurrentConstraints = currentConstraints;
            myNextConstraints = nextConstraints;
            myMarkersStack = markersStack;
        }

        public MarkdownConstraints getCurrentConstraints() {
            return myCurrentConstraints;
        }

        public MarkdownConstraints getNextConstraints() {
            return myNextConstraints;
        }

        public ParagraphMarkerBlock getParagraphBlock() {
            for (MarkerBlock block : myMarkersStack) {
                if (block instanceof ParagraphMarkerBlock) {
                    return (ParagraphMarkerBlock) block;
                }
            }
            return null;
        }

        public MarkerBlock getLastBlock() {
            return myMarkersStack.isEmpty() ? null : myMarkersStack.get(myMarkersStack.size() - 1);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof StateInfo)) return false;
            StateInfo other = (StateInfo) o;
            return myCurrentConstraints.equals(other.myCurrentConstraints)
                    && myNextConstraints.equals(other.myNextConstraints)
                    && myMarkersStack.equals(other.myMarkersStack);
        }

        @Override
        public int hashCode() {
            int result = myCurrentConstraints.hashCode();
            result = result * 37 + myNextConstraints.hashCode();
            result = result * 37 + myMarkersStack.hashCode();
            return result;
        }
    }
}
