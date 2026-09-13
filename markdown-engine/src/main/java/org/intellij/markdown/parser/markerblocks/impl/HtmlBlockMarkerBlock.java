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
import org.intellij.markdown.parser.markerblocks.MarkdownParserUtil;
import org.intellij.markdown.parser.markerblocks.MarkerBlock;
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;

import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.regex.Pattern;

public final class HtmlBlockMarkerBlock extends MarkerBlockImpl {

    private final ProductionHolder myProductionHolder;
    private final @Nullable Pattern myEndCheckingPattern;

    public HtmlBlockMarkerBlock(MarkdownConstraints constraints,
                                ProductionHolder productionHolder,
                                @Nullable Pattern endCheckingPattern,
                                LookaheadText.Position startPosition) {
        super(constraints, productionHolder.mark());
        myProductionHolder = productionHolder;
        myEndCheckingPattern = endCheckingPattern;

        productionHolder.addProduction(Collections.singletonList(new SequentialParser.Node(
                startPosition.getOffset(), startPosition.getNextLineOrEofOffset(), MarkdownTokenTypes.HTML_BLOCK_CONTENT)));
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
    
    protected MarkerBlock.ProcessingResult doProcessToken(LookaheadText.Position pos,
                                                          MarkdownConstraints currentConstraints) {
        if (pos.getOffsetInCurrentLine() != -1) {
            return MarkerBlock.ProcessingResult.CANCEL;
        }

        String prevLine = pos.getPrevLine();
        if (prevLine == null) {
            return MarkerBlock.ProcessingResult.DEFAULT;
        }
        if (!MarkdownConstraints.extendsPrev(myConstraints.applyToNextLine(pos), myConstraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT;
        }

        if (myEndCheckingPattern == null && MarkdownParserUtil.calcNumberOfConsequentEols(pos, myConstraints) >= 2) {
            return MarkerBlock.ProcessingResult.DEFAULT;
        } else if (myEndCheckingPattern != null && myEndCheckingPattern.matcher(prevLine).find()) {
            return MarkerBlock.ProcessingResult.DEFAULT;
        }

        if (pos.getCurrentLine().length() > 0) {
            myProductionHolder.addProduction(Collections.singletonList(new SequentialParser.Node(
                    pos.getOffset() + 1 + MarkdownConstraints.getCharsEaten(myConstraints, pos.getCurrentLine()),
                    pos.getNextLineOrEofOffset(),
                    MarkdownTokenTypes.HTML_BLOCK_CONTENT)));
        }

        return MarkerBlock.ProcessingResult.CANCEL;
    }

    @Override
    protected int calcNextInterestingOffset(LookaheadText.Position pos) {
        return pos.getNextLineOrEofOffset();
    }

    @Override
    
    public IElementType getDefaultNodeType() {
        return MarkdownElementTypes.HTML_BLOCK;
    }
}
