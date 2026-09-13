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
import org.intellij.markdown.lexer.Compat;
import org.intellij.markdown.parser.LookaheadText;
import org.intellij.markdown.parser.ProductionHolder;
import org.intellij.markdown.parser.constraints.MarkdownConstraints;
import org.intellij.markdown.parser.markerblocks.MarkdownParserUtil;
import org.intellij.markdown.parser.markerblocks.MarkerBlock;
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl;

import java.util.function.BiFunction;

public final class ParagraphMarkerBlock extends MarkerBlockImpl {

    private final BiFunction<LookaheadText.Position, MarkdownConstraints, Boolean> myInterruptsParagraph;

    public ParagraphMarkerBlock(MarkdownConstraints constraints,
                                ProductionHolder.Marker marker,
                                BiFunction<LookaheadText.Position, MarkdownConstraints, Boolean> interruptsParagraph) {
        super(constraints, marker);
        myInterruptsParagraph = interruptsParagraph;
    }

    
    public BiFunction<LookaheadText.Position, MarkdownConstraints, Boolean> getInterruptsParagraph() {
        return myInterruptsParagraph;
    }

    @Override
    public boolean allowsSubBlocks() {
        return false;
    }

    @Override
    public boolean isInterestingOffset(LookaheadText.Position pos) {
        return true;
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
        if (pos.getOffsetInCurrentLine() != -1) {
            return MarkerBlock.ProcessingResult.CANCEL;
        }

        Compat.assertCondition(pos.getOffsetInCurrentLine() == -1, "");

        if (MarkdownParserUtil.calcNumberOfConsequentEols(pos, myConstraints) >= 2) {
            return MarkerBlock.ProcessingResult.DEFAULT;
        }

        MarkdownConstraints nextLineConstraints = MarkdownConstraints.applyToNextLineAndAddModifiers(myConstraints, pos);
        if (!MarkdownConstraints.upstreamWith(nextLineConstraints, myConstraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT;
        }

        LookaheadText.Position posToCheck = pos.nextPosition(1 + MarkdownConstraints.getCharsEaten(nextLineConstraints, pos.getCurrentLine()));
        if (posToCheck == null || myInterruptsParagraph.apply(posToCheck, nextLineConstraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT;
        }

        return MarkerBlock.ProcessingResult.CANCEL;
    }

    @Override
    
    public IElementType getDefaultNodeType() {
        return MarkdownElementTypes.PARAGRAPH;
    }
}
