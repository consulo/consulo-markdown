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
package org.intellij.markdown.flavours.commonmark;

import consulo.language.ast.IElementType;
import org.intellij.markdown.MarkdownTokenTypes;
import org.intellij.markdown.parser.LookaheadText;
import org.intellij.markdown.parser.MarkerProcessor;
import org.intellij.markdown.parser.MarkerProcessorFactory;
import org.intellij.markdown.parser.ProductionHolder;
import org.intellij.markdown.parser.constraints.CommonMarkdownConstraints;
import org.intellij.markdown.parser.constraints.MarkdownConstraints;
import org.intellij.markdown.parser.markerblocks.MarkerBlock;
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider;
import org.intellij.markdown.parser.markerblocks.providers.*;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommonMarkMarkerProcessor extends MarkerProcessor<MarkerProcessor.StateInfo> {
    private MarkerProcessor.StateInfo myStateInfo;

    private final List<MarkerBlockProvider<MarkerProcessor.StateInfo>> myMarkerBlockProviders;

    public CommonMarkMarkerProcessor(ProductionHolder productionHolder, MarkdownConstraints constraintsBase) {
        super(productionHolder, constraintsBase);
        myStateInfo = new MarkerProcessor.StateInfo(myStartConstraints, myStartConstraints, myMarkersStack);
        myMarkerBlockProviders = Arrays.asList(
                new CodeBlockProvider(),
                new HorizontalRuleProvider(),
                new CodeFenceProvider(),
                new SetextHeaderProvider(),
                new BlockQuoteProvider(),
                new ListMarkerProvider(),
                new AtxHeaderProvider(),
                new HtmlBlockProvider(),
                new LinkReferenceDefinitionProvider()
        );
    }

    @Override
    protected StateInfo getStateInfo() {
        return myStateInfo;
    }

    @Override
    protected List<MarkerBlockProvider<StateInfo>> getMarkerBlockProviders() {
        return myMarkerBlockProviders;
    }

    @Override
    protected void updateStateInfo(LookaheadText.Position pos) {
        if (pos.getOffsetInCurrentLine() == -1) {
            myStateInfo = new MarkerProcessor.StateInfo(myStartConstraints,
                    myTopBlockConstraints.applyToNextLine(pos),
                    myMarkersStack);
        } else if (MarkerBlockProvider.isStartOfLineWithConstraints(pos, myStateInfo.myNextConstraints)) {
            MarkdownConstraints added = myStateInfo.myNextConstraints.addModifierIfNeeded(pos);
            MarkdownConstraints nextConstraints = added != null ? added : myStateInfo.myNextConstraints;
            myStateInfo = new MarkerProcessor.StateInfo(myStateInfo.myNextConstraints,
                    nextConstraints,
                    myMarkersStack);
        }
    }

    @Override
    protected void populateConstraintsTokens(LookaheadText.Position pos,
                                              MarkdownConstraints constraints,
                                              ProductionHolder productionHolder) {
        if (constraints.getIndent() == 0) {
            return;
        }

        int startOffset = pos.getOffset();
        int endOffset = Math.min(
                pos.getOffset() - pos.getOffsetInCurrentLine() + MarkdownConstraints.getCharsEaten(constraints, pos.getCurrentLine()),
                pos.getNextLineOrEofOffset());

        char[] types = constraints.getTypes();
        char lastType = types.length > 0 ? types[types.length - 1] : 0;
        IElementType type;
        if (lastType == '>') {
            type = MarkdownTokenTypes.BLOCK_QUOTE;
        } else if (lastType == '.' || lastType == ')') {
            type = MarkdownTokenTypes.LIST_NUMBER;
        } else {
            type = MarkdownTokenTypes.LIST_BULLET;
        }
        productionHolder.addProduction(Collections.singletonList(
                new SequentialParser.Node(startOffset, endOffset, type)));
    }

    @Override
    public List<MarkerBlock> createNewMarkerBlocks(LookaheadText.Position pos,
                                                    ProductionHolder productionHolder) {
        if (pos.getOffsetInCurrentLine() == -1) {
            return NO_BLOCKS;
        }

        return super.createNewMarkerBlocks(pos, productionHolder);
    }

    public static final MarkerProcessorFactory FACTORY = new MarkerProcessorFactory() {
        @Override
        public MarkerProcessor<?> createMarkerProcessor(ProductionHolder productionHolder) {
            return new CommonMarkMarkerProcessor(productionHolder, CommonMarkdownConstraints.BASE);
        }
    };
}
