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
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.MarkdownTokenTypes;
import consulo.language.ast.ASTNode;
import org.intellij.markdown.MarkdownAstUtil;

import org.intellij.markdown.flavours.MarkdownFlavourDescriptor;
import org.intellij.markdown.html.*;
import org.intellij.markdown.html.entities.EntityConverter;
import org.intellij.markdown.lexer.MarkdownLexer;
import org.intellij.markdown.lexer._MarkdownLexer;
import org.intellij.markdown.parser.LinkMap;
import org.intellij.markdown.parser.MarkerProcessorFactory;
import org.intellij.markdown.parser.sequentialparsers.EmphasisLikeParser;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;
import org.intellij.markdown.parser.sequentialparsers.SequentialParserManager;
import org.intellij.markdown.parser.sequentialparsers.impl.*;

import java.net.URI;
import java.util.*;

/**
 * CommonMark spec based flavour, to be used as a base for other flavours.
 */
public class CommonMarkFlavourDescriptor implements MarkdownFlavourDescriptor {
    protected final boolean myUseSafeLinks;
    protected final boolean myAbsolutizeAnchorLinks;

    private final MarkerProcessorFactory myMarkerProcessorFactory;
    private final SequentialParserManager mySequentialParserManager;

    public CommonMarkFlavourDescriptor(boolean useSafeLinks, boolean absolutizeAnchorLinks) {
        myUseSafeLinks = useSafeLinks;
        myAbsolutizeAnchorLinks = absolutizeAnchorLinks;
        myMarkerProcessorFactory = CommonMarkMarkerProcessor.FACTORY;
        mySequentialParserManager = new SequentialParserManager() {
            @Override
            public List<SequentialParser> getParserSequence() {
                return Arrays.asList(
                        new AutolinkParser(Collections.singletonList(MarkdownTokenTypes.AUTOLINK)),
                        new BacktickParser(),
                        new ImageParser(),
                        new InlineLinkParser(),
                        new ReferenceLinkParser(),
                        new EmphasisLikeParser(new EmphStrongDelimiterParser())
                );
            }
        };
    }

    public CommonMarkFlavourDescriptor(boolean useSafeLinks) {
        this(useSafeLinks, false);
    }

    public CommonMarkFlavourDescriptor() {
        this(true, false);
    }

    @Override
    public MarkerProcessorFactory getMarkerProcessorFactory() {
        return myMarkerProcessorFactory;
    }

    @Override
    public SequentialParserManager getSequentialParserManager() {
        return mySequentialParserManager;
    }

    @Override
    public MarkdownLexer createInlinesLexer() {
        return new MarkdownLexer(new _MarkdownLexer());
    }

    @Override
    public Map<IElementType, GeneratingProvider> createHtmlGeneratingProviders(LinkMap linkMap, URI baseURI) {
        HashMap<IElementType, GeneratingProvider> map = new HashMap<>();

        map.put(MarkdownElementTypes.MARKDOWN_FILE, new SimpleTagProvider("body"));
        map.put(MarkdownElementTypes.HTML_BLOCK, new HtmlBlockGeneratingProvider());
        map.put(MarkdownTokenTypes.HTML_TAG, new GeneratingProvider() {
            @Override
            public void processNode(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
                visitor.consumeHtml(MarkdownAstUtil.getTextInNode(node, text));
            }
        });

        map.put(MarkdownElementTypes.BLOCK_QUOTE, new SimpleTagProvider("blockquote"));

        map.put(MarkdownElementTypes.ORDERED_LIST, new SimpleTagProvider("ol") {
            @Override
            public void openTag(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
                String attribute = null;
                ASTNode listItem = MarkdownAstUtil.findChildOfType(node, MarkdownElementTypes.LIST_ITEM);
                if (listItem != null) {
                    ASTNode listNumber = MarkdownAstUtil.findChildOfType(listItem, MarkdownTokenTypes.LIST_NUMBER);
                    if (listNumber != null) {
                        String numberText = MarkdownAstUtil.getTextInNode(listNumber, text).toString().trim();
                        String number = numberText.substring(0, numberText.length() - 1);
                        // trimStart('0')
                        int startIdx = 0;
                        while (startIdx < number.length() && number.charAt(startIdx) == '0') {
                            startIdx++;
                        }
                        number = number.substring(startIdx);
                        if (!number.equals("1")) {
                            attribute = "start=\"" + (number.isEmpty() ? "0" : number) + "\"";
                        }
                    }
                }
                visitor.consumeTagOpen(node, "ol", attribute);
            }
        });
        map.put(MarkdownElementTypes.UNORDERED_LIST, new SimpleTagProvider("ul"));
        map.put(MarkdownElementTypes.LIST_ITEM, new ListItemGeneratingProvider());

        map.put(MarkdownTokenTypes.SETEXT_CONTENT, new TrimmingInlineHolderProvider());
        map.put(MarkdownElementTypes.SETEXT_1, new SimpleTagProvider("h1"));
        map.put(MarkdownElementTypes.SETEXT_2, new SimpleTagProvider("h2"));

        map.put(MarkdownTokenTypes.ATX_CONTENT, new TrimmingInlineHolderProvider());
        map.put(MarkdownElementTypes.ATX_1, new SimpleTagProvider("h1"));
        map.put(MarkdownElementTypes.ATX_2, new SimpleTagProvider("h2"));
        map.put(MarkdownElementTypes.ATX_3, new SimpleTagProvider("h3"));
        map.put(MarkdownElementTypes.ATX_4, new SimpleTagProvider("h4"));
        map.put(MarkdownElementTypes.ATX_5, new SimpleTagProvider("h5"));
        map.put(MarkdownElementTypes.ATX_6, new SimpleTagProvider("h6"));

        map.put(MarkdownElementTypes.AUTOLINK, new GeneratingProvider() {
            @Override
            public void processNode(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
                CharSequence linkText = MarkdownAstUtil.getTextInNode(node, text);
                CharSequence linkLabel = EntityConverter.replaceEntities(
                        linkText.subSequence(1, linkText.length() - 1),
                        true,
                        false
                );
                CharSequence linkDestination = LinkMap.normalizeDestination(linkText, false);
                if (myUseSafeLinks) {
                    linkDestination = XssSafeLinks.makeXssSafeDestination(linkDestination);
                }
                visitor.consumeTagOpen(node, "a", "href=\"" + linkDestination + "\"");
                visitor.consumeHtml(linkLabel);
                visitor.consumeTagClose("a");
            }
        });

        map.put(MarkdownElementTypes.LINK_LABEL, new TransparentInlineHolderProvider());
        map.put(MarkdownElementTypes.LINK_TEXT, new TransparentInlineHolderProvider());
        map.put(MarkdownElementTypes.LINK_TITLE, new TransparentInlineHolderProvider());

        map.put(MarkdownElementTypes.INLINE_LINK,
                XssSafeLinks.makeXssSafe(new InlineLinkGeneratingProvider(baseURI, myAbsolutizeAnchorLinks), myUseSafeLinks));

        map.put(MarkdownElementTypes.FULL_REFERENCE_LINK,
                XssSafeLinks.makeXssSafe(new ReferenceLinksGeneratingProvider(linkMap, baseURI, myAbsolutizeAnchorLinks), myUseSafeLinks));
        map.put(MarkdownElementTypes.SHORT_REFERENCE_LINK,
                XssSafeLinks.makeXssSafe(new ReferenceLinksGeneratingProvider(linkMap, baseURI, myAbsolutizeAnchorLinks), myUseSafeLinks));

        map.put(MarkdownElementTypes.IMAGE,
                XssSafeLinks.makeXssSafe(new ImageGeneratingProvider(linkMap, baseURI), myUseSafeLinks));

        map.put(MarkdownElementTypes.LINK_DEFINITION, new GeneratingProvider() {
            @Override
            public void processNode(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
            }
        });

        map.put(MarkdownElementTypes.CODE_FENCE, new CodeFenceGeneratingProvider());

        map.put(MarkdownElementTypes.CODE_BLOCK, new GeneratingProvider() {
            @Override
            public void processNode(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
                visitor.consumeHtml("<pre>");
                visitor.consumeTagOpen(node, "code");

                for (ASTNode child : MarkdownAstUtil.children(node)) {
                    if (child.getElementType() == MarkdownTokenTypes.CODE_LINE) {
                        visitor.consumeHtml(HtmlGenerator.trimIndents(HtmlGenerator.leafText(text, child, false), 4));
                    } else if (child.getElementType() == MarkdownTokenTypes.EOL) {
                        visitor.consumeHtml("\n");
                    }
                }

                visitor.consumeHtml("\n");
                visitor.consumeTagClose("code");
                visitor.consumeHtml("</pre>");
            }
        });

        map.put(MarkdownTokenTypes.HORIZONTAL_RULE, new GeneratingProvider() {
            @Override
            public void processNode(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
                visitor.consumeHtml("<hr />");
            }
        });
        map.put(MarkdownTokenTypes.HARD_LINE_BREAK, new GeneratingProvider() {
            @Override
            public void processNode(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
                visitor.consumeHtml("<br />");
            }
        });

        map.put(MarkdownElementTypes.PARAGRAPH, new TrimmingInlineHolderProvider() {
            @Override
            public void openTag(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
                visitor.consumeTagOpen(node, "p");
            }

            @Override
            public void closeTag(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
                visitor.consumeTagClose("p");
            }
        });
        map.put(MarkdownElementTypes.EMPH, new SimpleInlineTagProvider("em", 1, -1));
        map.put(MarkdownElementTypes.STRONG, new SimpleInlineTagProvider("strong", 2, -2));
        map.put(MarkdownElementTypes.CODE_SPAN, new CodeSpanGeneratingProvider());

        return map;
    }
}
