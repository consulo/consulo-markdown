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
package org.intellij.markdown.parser.markerblocks.providers;

import org.intellij.markdown.MarkdownTokenTypes;
import org.intellij.markdown.parser.LookaheadText;
import org.intellij.markdown.parser.MarkerProcessor;
import org.intellij.markdown.parser.ProductionHolder;
import org.intellij.markdown.parser.constraints.MarkdownConstraints;
import org.intellij.markdown.parser.markerblocks.MarkerBlock;
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider;
import org.intellij.markdown.parser.markerblocks.impl.CodeFenceMarkerBlock;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;

import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeFenceProvider implements MarkerBlockProvider<MarkerProcessor.StateInfo> {

    private static final Pattern REGEX = Pattern.compile("^ {0,3}(~~~+|```+)([^`]*)$");

    @Override
    
    public List<MarkerBlock> createMarkerBlocks(LookaheadText.Position pos,
                                                ProductionHolder productionHolder,
                                                MarkerProcessor.StateInfo stateInfo) {
        OpeningInfo openingInfo = obtainFenceOpeningInfo(pos, stateInfo.getCurrentConstraints());
        if (openingInfo == null) {
            return Collections.emptyList();
        }
        createNodesForFenceStart(pos, openingInfo, productionHolder);
        return Collections.singletonList(new CodeFenceMarkerBlock(stateInfo.getCurrentConstraints(), productionHolder, openingInfo.myDelimiter));
    }

    @Override
    public boolean interruptsParagraph(LookaheadText.Position pos,
                                       MarkdownConstraints constraints) {
        return obtainFenceOpeningInfo(pos, constraints) != null;
    }

    private void createNodesForFenceStart(LookaheadText.Position pos,
                                          OpeningInfo openingInfo,
                                          ProductionHolder productionHolder) {
        String info = openingInfo.myInfo;
        int infoStartPosition = pos.getNextLineOrEofOffset() - info.length();
        productionHolder.addProduction(Collections.singletonList(
                new SequentialParser.Node(pos.getOffset(), infoStartPosition, MarkdownTokenTypes.CODE_FENCE_START)));
        if (!openingInfo.myInfo.isEmpty()) {
            productionHolder.addProduction(Collections.singletonList(
                    new SequentialParser.Node(infoStartPosition, pos.getNextLineOrEofOffset(), MarkdownTokenTypes.FENCE_LANG)));
        }
    }

    /**
     * Holds information of fence opening parts.
     */
    protected static final class OpeningInfo {
        final String myDelimiter;
        final String myInfo;

        OpeningInfo(String delimiter, String info) {
            myDelimiter = delimiter;
            myInfo = info;
        }
    }

    /**
     * Can be used for customizing conditions for the fence opening.
     */
    protected @Nullable OpeningInfo obtainFenceOpeningInfo(LookaheadText.Position pos,
                                                MarkdownConstraints constraints) {
        if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, constraints)) {
            return null;
        }
        Matcher matcher = REGEX.matcher(pos.getCurrentLineFromPosition());
        if (!matcher.matches()) {
            return null;
        }
        return new OpeningInfo(matcher.group(1), matcher.group(2));
    }
}
