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
package org.intellij.markdown.flavours.gfm.table;

import consulo.language.ast.IElementType;
import org.intellij.markdown.flavours.gfm.GFMElementTypes;
import org.intellij.markdown.flavours.gfm.GFMTokenTypes;
import org.intellij.markdown.parser.LookaheadText;
import org.intellij.markdown.parser.ProductionHolder;
import org.intellij.markdown.parser.constraints.MarkdownConstraints;
import org.intellij.markdown.parser.markerblocks.MarkerBlock;
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GitHubTableMarkerBlock extends MarkerBlockImpl {
    private final ProductionHolder myProductionHolder;
    private final int myTableColumnsNumber;
    private int myCurrentLine;

    public GitHubTableMarkerBlock(LookaheadText.Position pos,
                                   MarkdownConstraints constraints,
                                   ProductionHolder productionHolder,
                                   int tableColumnsNumber) {
        super(constraints, productionHolder.mark());
        myProductionHolder = productionHolder;
        myTableColumnsNumber = tableColumnsNumber;
        myCurrentLine = 0;

        productionHolder.addProduction(Collections.singletonList(
                new SequentialParser.Node(pos.getOffset(), pos.getNextLineOrEofOffset(), GFMElementTypes.HEADER)));
        productionHolder.addProduction(fillCells(pos));
    }

    @Override
    protected ProcessingResult doProcessToken(LookaheadText.Position pos, MarkdownConstraints currentConstraints) {
        myCurrentLine++;
        // That means it's table header separator line
        if (myCurrentLine == 1) {
            myProductionHolder.addProduction(Collections.singletonList(
                    new SequentialParser.Node(pos.getOffset() + 1, pos.getNextLineOrEofOffset(),
                            GFMTokenTypes.TABLE_SEPARATOR)));
            return ProcessingResult.CANCEL;
        }

        String line = pos.getCurrentLine();
        if (!isProbablyTableLine(line)) {
            return ProcessingResult.DEFAULT;
        }
        List<SequentialParser.Node> cellsAndSeps = fillCells(pos);
        if (cellsAndSeps.isEmpty()) {
            return ProcessingResult.DEFAULT;
        }
        List<SequentialParser.Node> production = new ArrayList<>();
        production.add(new SequentialParser.Node(
                cellsAndSeps.get(0).getStartOffset(),
                cellsAndSeps.get(cellsAndSeps.size() - 1).getEndOffset(),
                GFMElementTypes.ROW));
        production.addAll(cellsAndSeps);
        myProductionHolder.addProduction(production);
        return ProcessingResult.CANCEL;
    }

    @Override
    protected ClosingAction getDefaultAction() {
        return ClosingAction.DONE;
    }

    @Override
    protected int calcNextInterestingOffset(LookaheadText.Position pos) {
        return pos.getNextLineOrEofOffset();
    }

    @Override
    public IElementType getDefaultNodeType() {
        return GFMElementTypes.TABLE;
    }

    @Override
    public boolean isInterestingOffset(LookaheadText.Position pos) {
        return pos.getOffsetInCurrentLine() == -1;
    }

    @Override
    public boolean allowsSubBlocks() {
        return false;
    }

    private List<SequentialParser.Node> fillCells(LookaheadText.Position pos) {
        List<SequentialParser.Node> result = new ArrayList<>();

        int offset = pos.getOffset();
        if (pos.getOffsetInCurrentLine() == -1) {
            offset += 1 + MarkdownConstraints.getCharsEaten(myConstraints, pos.getCurrentLine());
        }

        CharSequence line = MarkdownConstraints.eatItselfFromString(myConstraints, pos.getCurrentLine());

        List<String> cells = splitByPipes(line);
        int cellNodesAdded = 0;
        for (int i = 0; i < cells.size(); i++) {
            String cell = cells.get(i);
            if (!cell.trim().isEmpty() || (i > 0 && i < cells.size() - 1)) {
                result.add(new SequentialParser.Node(offset, offset + cell.length(), GFMTokenTypes.CELL));
                cellNodesAdded++;
            }
            offset += cell.length();
            if (i < cells.size() - 1) {
                result.add(new SequentialParser.Node(offset, offset + 1, GFMTokenTypes.TABLE_SEPARATOR));
            }
            offset += 1;

            if (cellNodesAdded >= myTableColumnsNumber) {
                if (offset < pos.getNextLineOrEofOffset()) {
                    result.add(new SequentialParser.Node(offset, pos.getNextLineOrEofOffset(), GFMTokenTypes.TABLE_SEPARATOR));
                }
                break;
            }
        }
        return result;
    }

    private boolean isProbablyTableLine(CharSequence line) {
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '|') {
                return true;
            }
        }
        return false;
    }

    public static List<String> splitByPipes(CharSequence text) {
        List<String> result = new ArrayList<>();
        int startIndex = 0;
        for (int index = 0; index < text.length(); index++) {
            char c = text.charAt(index);
            if (c != '|') {
                continue;
            }
            char charBefore = text.charAt(Math.max(index - 1, 0));
            if (charBefore == '\\') {
                continue;
            }
            String substring = text.subSequence(startIndex, index).toString();
            result.add(substring);
            startIndex = index + 1;
        }
        result.add(text.subSequence(startIndex, text.length()).toString());
        return result;
    }
}
