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
import org.intellij.markdown.parser.markerblocks.MarkerBlock;
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;

import java.util.Collections;
import java.util.regex.Pattern;

public final class CodeFenceMarkerBlock extends MarkerBlockImpl {

    private final ProductionHolder myProductionHolder;
    private final Pattern myEndLinePattern;
    private int myRealInterestingOffset = -1;

    public CodeFenceMarkerBlock(MarkdownConstraints constraints,
                                ProductionHolder productionHolder,
                                String fenceStart) {
        super(constraints, productionHolder.mark());
        myProductionHolder = productionHolder;
        myEndLinePattern = Pattern.compile("^ {0,3}" + Pattern.quote(fenceStart) + "+ *$");
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

        MarkdownConstraints nextLineConstraints = MarkdownConstraints.applyToNextLineAndAddModifiers(myConstraints, pos);
        if (!MarkdownConstraints.extendsPrev(nextLineConstraints, myConstraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT;
        }

        int nextLineOffset = pos.getNextLineOrEofOffset();
        myRealInterestingOffset = nextLineOffset;

        CharSequence currentLine = MarkdownConstraints.eatItselfFromString(nextLineConstraints, pos.getCurrentLine());
        if (endsThisFence(currentLine)) {
            myProductionHolder.addProduction(Collections.singletonList(new SequentialParser.Node(
                    pos.getOffset() + 1, pos.getNextLineOrEofOffset(), MarkdownTokenTypes.CODE_FENCE_END)));
            scheduleProcessingResult(nextLineOffset, MarkerBlock.ProcessingResult.DEFAULT);
        } else {
            int contentStart = Math.min(pos.getOffset() + 1 + MarkdownConstraints.getCharsEaten(myConstraints, pos.getCurrentLine()), nextLineOffset);
            int contentEnd = nextLineOffset;
            if (contentStart < contentEnd) {
                myProductionHolder.addProduction(Collections.singletonList(new SequentialParser.Node(
                        contentStart, contentEnd, MarkdownTokenTypes.CODE_FENCE_CONTENT)));
            }
        }

        return MarkerBlock.ProcessingResult.CANCEL;
    }

    private boolean endsThisFence(CharSequence line) {
        return myEndLinePattern.matcher(line).matches();
    }

    @Override
    
    public IElementType getDefaultNodeType() {
        return MarkdownElementTypes.CODE_FENCE;
    }
}
