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

import org.intellij.markdown.MarkdownAstUtil;
import consulo.language.ast.IElementType;
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.MarkdownTokenTypes;
import consulo.language.ast.ASTNode;

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor;
import org.intellij.markdown.flavours.gfm.lexer._GFMLexer;
import org.intellij.markdown.html.*;
import org.intellij.markdown.html.entities.EntityConverter;
import org.intellij.markdown.lexer.MarkdownLexer;
import org.intellij.markdown.parser.LinkMap;
import org.intellij.markdown.parser.MarkerProcessorFactory;
import org.intellij.markdown.parser.sequentialparsers.EmphasisLikeParser;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;
import org.intellij.markdown.parser.sequentialparsers.SequentialParserManager;
import org.intellij.markdown.parser.sequentialparsers.impl.*;

import java.net.URI;
import java.util.*;

/**
 * GitHub Markdown spec based flavour, to be used as a base for other flavours.
 */
public class GFMFlavourDescriptor extends CommonMarkFlavourDescriptor {
    private final boolean myMakeHttpsAutoLinks;
    private final MarkerProcessorFactory myMarkerProcessorFactory;
    private final SequentialParserManager mySequentialParserManager;

    public GFMFlavourDescriptor(boolean useSafeLinks, boolean absolutizeAnchorLinks, boolean makeHttpsAutoLinks) {
        super(useSafeLinks, absolutizeAnchorLinks);
        myMakeHttpsAutoLinks = makeHttpsAutoLinks;
        myMarkerProcessorFactory = GFMMarkerProcessor.FACTORY;
        mySequentialParserManager = new SequentialParserManager() {
            @Override
            public List<SequentialParser> getParserSequence() {
                return Arrays.asList(
                        new AutolinkParser(Arrays.asList(MarkdownTokenTypes.AUTOLINK, GFMTokenTypes.GFM_AUTOLINK)),
                        new BacktickParser(),
                        new MathParser(),
                        new ImageParser(),
                        new InlineLinkParser(),
                        new ReferenceLinkParser(),
                        new EmphasisLikeParser(new EmphStrongDelimiterParser(), new StrikeThroughDelimiterParser())
                );
            }
        };
    }

    public GFMFlavourDescriptor(boolean useSafeLinks, boolean absolutizeAnchorLinks) {
        this(useSafeLinks, absolutizeAnchorLinks, false);
    }

    public GFMFlavourDescriptor(boolean useSafeLinks) {
        this(useSafeLinks, false, false);
    }

    public GFMFlavourDescriptor() {
        this(true, false, false);
    }

    @Override
    public MarkerProcessorFactory getMarkerProcessorFactory() {
        return myMarkerProcessorFactory;
    }

    @Override
    public MarkdownLexer createInlinesLexer() {
        return new MarkdownLexer(new _GFMLexer());
    }

    @Override
    public SequentialParserManager getSequentialParserManager() {
        return mySequentialParserManager;
    }

    @Override
    public Map<IElementType, GeneratingProvider> createHtmlGeneratingProviders(LinkMap linkMap, URI baseURI) {
        Map<IElementType, GeneratingProvider> parentMap = super.createHtmlGeneratingProviders(linkMap, baseURI);
        HashMap<IElementType, GeneratingProvider> map = new HashMap<>(parentMap);

        map.put(GFMElementTypes.STRIKETHROUGH, new EqualDelimiterTrimmingInlineTagProvider("span", GFMTokenTypes.TILDE) {
            @Override
            public void openTag(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
                visitor.consumeTagOpen(node, getTagName(), "class=\"user-del\"");
            }
        });

        map.put(GFMElementTypes.TABLE, new TablesGeneratingProvider());

        map.put(GFMTokenTypes.CELL, new TrimmingInlineHolderProvider());
        map.put(MarkdownElementTypes.CODE_SPAN, new TableAwareCodeSpanGeneratingProvider());

        map.put(GFMTokenTypes.GFM_AUTOLINK, new GeneratingProvider() {
            @Override
            public void processNode(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
                CharSequence linkText = MarkdownAstUtil.getTextInNode(node, text);

                // #28: do not render GFM autolinks under link titles
                // (though it's "OK" according to CommonMark spec)
                if (MarkdownAstUtil.getParentOfType(node, MarkdownElementTypes.LINK_LABEL,
                        MarkdownElementTypes.LINK_TEXT) != null) {
                    visitor.consumeHtml(linkText);
                    return;
                }

                // according to GFM_AUTOLINK rule in lexer, link either starts with scheme or with 'www.'
                CharSequence absoluteLink;
                if (hasSchema(linkText)) {
                    absoluteLink = linkText;
                } else {
                    if (myMakeHttpsAutoLinks) {
                        absoluteLink = "https://" + linkText;
                    } else {
                        absoluteLink = "http://" + linkText;
                    }
                }

                CharSequence link = EntityConverter.replaceEntities(linkText, true, false);
                CharSequence normalizedDestination = LinkMap.normalizeDestination(absoluteLink, false);
                if (myUseSafeLinks) {
                    normalizedDestination = XssSafeLinks.makeXssSafeDestination(normalizedDestination);
                }
                visitor.consumeTagOpen(node, "a", "href=\"" + normalizedDestination + "\"");
                visitor.consumeHtml(link);
                visitor.consumeTagClose("a");
            }

            private boolean hasSchema(CharSequence linkText) {
                int index = -1;
                for (int i = 0; i < linkText.length(); i++) {
                    if (linkText.charAt(i) == '/') {
                        index = i;
                        break;
                    }
                }
                if (index == -1) return false;
                return index != 0
                        && index + 1 < linkText.length()
                        && linkText.charAt(index - 1) == ':'
                        && linkText.charAt(index + 1) == '/';
            }
        });

        map.put(MarkdownElementTypes.LIST_ITEM, new CheckedListItemGeneratingProvider());

        map.put(GFMElementTypes.INLINE_MATH, new MathGeneratingProvider(true));
        map.put(GFMElementTypes.BLOCK_MATH, new MathGeneratingProvider());

        return map;
    }
}
