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
import org.intellij.markdown.lexer.Compat;
import org.intellij.markdown.parser.LookaheadText;
import org.intellij.markdown.parser.ProductionHolder;
import org.intellij.markdown.parser.constraints.MarkdownConstraints;
import org.intellij.markdown.parser.markerblocks.MarkdownParserUtil;
import org.intellij.markdown.parser.markerblocks.MarkerBlock;
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;

import java.util.Collections;

public final class CodeBlockMarkerBlock extends MarkerBlockImpl {

    private final ProductionHolder myProductionHolder;
    private int myRealInterestingOffset = -1;

    public CodeBlockMarkerBlock(MarkdownConstraints constraints,
                                ProductionHolder productionHolder,
                                LookaheadText.Position startPosition) {
        super(constraints, productionHolder.mark());
        myProductionHolder = productionHolder;

        productionHolder.addProduction(Collections.singletonList(new SequentialParser.Node(
                startPosition.getOffset(), startPosition.getNextLineOrEofOffset(), MarkdownTokenTypes.CODE_LINE)));
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
    protected int calcNextInterestingOffset(LookaheadText.Position pos) {
        return pos.getNextLineOrEofOffset();
    }

    @Override
    
    protected MarkerBlock.ClosingAction getDefaultAction() {
        return MarkerBlock.ClosingAction.DONE;
    }

    @Override
    
    protected MarkerBlock.ProcessingResult doProcessToken(LookaheadText.Position pos,
                                                          MarkdownConstraints currentConstraints) {
        if (pos.getOffset() < myRealInterestingOffset) {
            return MarkerBlock.ProcessingResult.CANCEL;
        }

        // Eat everything if we're on code line
        if (pos.getOffsetInCurrentLine() != -1) {
            return MarkerBlock.ProcessingResult.CANCEL;
        }

        Compat.assertCondition(pos.getOffsetInCurrentLine() == -1, "");

        LookaheadText.Position nonemptyPos = MarkdownParserUtil.findNonEmptyLineWithSameConstraints(myConstraints, pos);
        if (nonemptyPos == null) {
            return MarkerBlock.ProcessingResult.DEFAULT;
        }

        MarkdownConstraints nextConstraints = MarkdownConstraints.applyToNextLineAndAddModifiers(myConstraints, nonemptyPos);
        LookaheadText.Position shifted = nonemptyPos.nextPosition(1 + MarkdownConstraints.getCharsEaten(nextConstraints, nonemptyPos.getCurrentLine()));
        Integer charsToNonWs = shifted != null ? shifted.charsToNonWhitespace() : null;
        LookaheadText.Position nonWhitespace = shifted != null ? shifted.nextPosition(charsToNonWs != null ? charsToNonWs : 0) : null;
        if (nonWhitespace == null) {
            return MarkerBlock.ProcessingResult.DEFAULT;
        }

        if (!MarkdownParserUtil.hasCodeBlockIndent(nonWhitespace, nextConstraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT;
        } else {
            // We'll add the current line anyway
            MarkdownConstraints nextLineConstraints = MarkdownConstraints.applyToNextLineAndAddModifiers(myConstraints, pos);
            int rangeStart = pos.getOffset() + 1 + MarkdownConstraints.getCharsEaten(nextLineConstraints, pos.getCurrentLine());
            int rangeEnd = pos.getNextLineOrEofOffset();
            if (rangeEnd - rangeStart > 0) {
                myProductionHolder.addProduction(Collections.singletonList(new SequentialParser.Node(
                        rangeStart, rangeEnd, MarkdownTokenTypes.CODE_LINE)));
            }

            myRealInterestingOffset = pos.getNextLineOrEofOffset();
            return MarkerBlock.ProcessingResult.CANCEL;
        }
    }

    @Override
    
    public IElementType getDefaultNodeType() {
        return MarkdownElementTypes.CODE_BLOCK;
    }
}
