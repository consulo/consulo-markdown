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
package org.intellij.markdown.flavours.gfm;

import consulo.language.ast.IElementType;
import org.intellij.markdown.MarkdownTokenTypes;
import org.intellij.markdown.flavours.commonmark.CommonMarkMarkerProcessor;
import org.intellij.markdown.flavours.gfm.table.GitHubTableMarkerProvider;
import org.intellij.markdown.parser.LookaheadText;
import org.intellij.markdown.parser.MarkerProcessor;
import org.intellij.markdown.parser.MarkerProcessorFactory;
import org.intellij.markdown.parser.ProductionHolder;
import org.intellij.markdown.parser.constraints.CommonMarkdownConstraints;
import org.intellij.markdown.parser.constraints.MarkdownConstraints;
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class GFMMarkerProcessor extends CommonMarkMarkerProcessor {
    private final List<MarkerBlockProvider<StateInfo>> myMarkerBlockProviders;

    public GFMMarkerProcessor(ProductionHolder productionHolder, CommonMarkdownConstraints constraintsBase) {
        super(productionHolder, constraintsBase);
        List<MarkerBlockProvider<StateInfo>> parentProviders = super.getMarkerBlockProviders();
        myMarkerBlockProviders = new ArrayList<>(parentProviders.size() + 1);
        myMarkerBlockProviders.addAll(parentProviders);
        myMarkerBlockProviders.add(new GitHubTableMarkerProvider());
    }

    @Override
    protected List<MarkerBlockProvider<StateInfo>> getMarkerBlockProviders() {
        return myMarkerBlockProviders;
    }

    @Override
    protected void populateConstraintsTokens(LookaheadText.Position pos,
                                              MarkdownConstraints constraints,
                                              ProductionHolder productionHolder) {
        if (!(constraints instanceof GFMConstraints) || !((GFMConstraints) constraints).hasCheckbox()) {
            super.populateConstraintsTokens(pos, constraints, productionHolder);
            return;
        }

        String line = pos.getCurrentLine();
        int offset = pos.getOffsetInCurrentLine();
        while (offset < line.length() && line.charAt(offset) != '[') {
            offset++;
        }
        if (offset == line.length()) {
            super.populateConstraintsTokens(pos, constraints, productionHolder);
            return;
        }

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
        int middleOffset = pos.getOffset() - pos.getOffsetInCurrentLine() + offset;
        int endOffset = Math.min(
                pos.getOffset() - pos.getOffsetInCurrentLine() + MarkdownConstraints.getCharsEaten(constraints, pos.getCurrentLine()),
                pos.getNextLineOrEofOffset());

        productionHolder.addProduction(Arrays.asList(
                new SequentialParser.Node(pos.getOffset(), middleOffset, type),
                new SequentialParser.Node(middleOffset, endOffset, GFMTokenTypes.CHECK_BOX)
        ));
    }

    public static final MarkerProcessorFactory FACTORY = new MarkerProcessorFactory() {
        @Override
        public MarkerProcessor<?> createMarkerProcessor(ProductionHolder productionHolder) {
            return new GFMMarkerProcessor(productionHolder, GFMConstraints.BASE);
        }
    };
}
