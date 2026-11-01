/*
 * Copyright 2000-2026 JetBrains s.r.o. and contributors.
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
package org.intellij.plugins.markdown.lang.parser;

import consulo.language.ast.IElementType;
import org.intellij.markdown.MarkdownTokenTypes;
import org.intellij.markdown.flavours.commonmark.CommonMarkMarkerProcessor;
import org.intellij.markdown.flavours.gfm.GFMConstraints;
import org.intellij.markdown.flavours.gfm.GFMTokenTypes;
import org.intellij.markdown.flavours.gfm.table.GitHubTableMarkerProvider;
import org.intellij.markdown.parser.LookaheadText;
import org.intellij.markdown.parser.MarkerProcessor;
import org.intellij.markdown.parser.MarkerProcessorFactory;
import org.intellij.markdown.parser.ProductionHolder;
import org.intellij.markdown.parser.constraints.MarkdownConstraints;
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider;
import org.intellij.markdown.parser.markerblocks.providers.AtxHeaderProvider;
import org.intellij.markdown.parser.markerblocks.providers.BlockQuoteProvider;
import org.intellij.markdown.parser.markerblocks.providers.CodeBlockProvider;
import org.intellij.markdown.parser.markerblocks.providers.HorizontalRuleProvider;
import org.intellij.markdown.parser.markerblocks.providers.HtmlBlockProvider;
import org.intellij.markdown.parser.markerblocks.providers.LinkReferenceDefinitionProvider;
import org.intellij.markdown.parser.markerblocks.providers.ListMarkerProvider;
import org.intellij.markdown.parser.markerblocks.providers.SetextHeaderProvider;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;
import org.intellij.plugins.markdown.lang.parser.blocks.CodeFenceMarkerProvider;

import java.util.List;

/**
 * The marker processor of the JetBrains Markdown plugin, for flavours that extend it (MDX).
 * <p>
 * Consulo: reduced to the block providers the markdown engine has, in JetBrains' order. JetBrains' indented code
 * fences, GitHub alerts, definition lists, admonitions, front matter headers and comment-aware link reference
 * definitions have no Consulo port; their places are taken by the plain CommonMark providers where one exists.
 * The default Markdown file type keeps parsing with {@link org.intellij.markdown.parser.MarkdownParserManager#FLAVOUR}.
 */
public class MarkdownDefaultMarkerProcessor extends CommonMarkMarkerProcessor {
    public static final MarkerProcessorFactory FACTORY = new MarkerProcessorFactory() {
        @Override
        public MarkerProcessor<?> createMarkerProcessor(ProductionHolder productionHolder) {
            return new MarkdownDefaultMarkerProcessor(productionHolder, GFMConstraints.BASE);
        }
    };

    private final List<MarkerBlockProvider<StateInfo>> myMarkerBlockProviders = List.of(
        new CodeBlockProvider(),
        new CodeFenceMarkerProvider(),
        new SetextHeaderProvider(),
        new BlockQuoteProvider(),
        new ListMarkerProvider(),
        new HtmlBlockProvider(),
        new HorizontalRuleProvider(),
        new GitHubTableMarkerProvider(),
        new AtxHeaderProvider(),
        new LinkReferenceDefinitionProvider()
    );

    public MarkdownDefaultMarkerProcessor(ProductionHolder productionHolder, MarkdownConstraints constraintsBase) {
        super(productionHolder, constraintsBase);
    }

    @Override
    protected List<MarkerBlockProvider<StateInfo>> getMarkerBlockProviders() {
        return myMarkerBlockProviders;
    }

    @Override
    protected void populateConstraintsTokens(LookaheadText.Position pos,
                                             MarkdownConstraints constraints,
                                             ProductionHolder productionHolder) {
        if (!(constraints instanceof GFMConstraints gfmConstraints) || !gfmConstraints.hasCheckbox()) {
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
        if (types.length == 0) {
            throw new IllegalStateException("checkbox constraints without a list marker");
        }
        IElementType type = switch (types[types.length - 1]) {
            case '>' -> MarkdownTokenTypes.BLOCK_QUOTE;
            case '.', ')' -> MarkdownTokenTypes.LIST_NUMBER;
            default -> MarkdownTokenTypes.LIST_BULLET;
        };
        int middleOffset = pos.getOffset() - pos.getOffsetInCurrentLine() + offset;
        int endOffset = Math.min(
            pos.getOffset() - pos.getOffsetInCurrentLine() + MarkdownConstraints.getCharsEaten(constraints, pos.getCurrentLine()),
            pos.getNextLineOrEofOffset());

        productionHolder.addProduction(List.of(
            new SequentialParser.Node(pos.getOffset(), middleOffset, type),
            new SequentialParser.Node(middleOffset, endOffset, GFMTokenTypes.CHECK_BOX)
        ));
    }
}
