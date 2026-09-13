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
package org.intellij.markdown.html;

import consulo.language.ast.IElementType;
import org.intellij.markdown.MarkdownTokenTypes;
import consulo.language.ast.ASTNode;
import org.intellij.markdown.MarkdownAstUtil;

import org.intellij.markdown.MarkdownRecursiveVisitor;
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor;
import org.intellij.markdown.html.entities.EntityConverter;
import org.intellij.markdown.parser.LinkMap;

import java.util.Arrays;
import java.util.Map;

public final class HtmlGenerator {
    private final String myMarkdownText;
    private final ASTNode myRoot;
    private final Map<IElementType, GeneratingProvider> myProviders;
    private final boolean myIncludeSrcPositions;
    private final StringBuilder myHtmlString;

    public static final AttributesCustomizer DUMMY_ATTRIBUTES_CUSTOMIZER = (node, tagName, attributes) -> attributes;

    public static final String SRC_ATTRIBUTE_NAME = "md-src-pos";

    public HtmlGenerator(String markdownText,
                         ASTNode root,
                         Map<IElementType, GeneratingProvider> providers,
                         boolean includeSrcPositions) {
        myMarkdownText = markdownText;
        myRoot = root;
        myProviders = providers;
        myIncludeSrcPositions = includeSrcPositions;
        myHtmlString = new StringBuilder();
    }

    public HtmlGenerator(String markdownText,
                         ASTNode root,
                         Map<IElementType, GeneratingProvider> providers) {
        this(markdownText, root, providers, false);
    }

    public HtmlGenerator(String markdownText,
                         ASTNode root,
                         MarkdownFlavourDescriptor flavour,
                         boolean includeSrcPositions) {
        this(markdownText,
                root,
                flavour.createHtmlGeneratingProviders(LinkMap.buildLinkMap(root, markdownText), null),
                includeSrcPositions);
    }

    public HtmlGenerator(String markdownText,
                         ASTNode root,
                         MarkdownFlavourDescriptor flavour) {
        this(markdownText, root, flavour, false);
    }

    @Deprecated
    public String generateHtml(AttributesCustomizer customizer) {
        new HtmlGeneratingVisitor(new DefaultTagRenderer(customizer, myIncludeSrcPositions)).visitNode(myRoot);
        return myHtmlString.toString();
    }

    public String generateHtml(TagRenderer tagRenderer) {
        new HtmlGeneratingVisitor(tagRenderer).visitNode(myRoot);
        return myHtmlString.toString();
    }

    public String generateHtml() {
        return generateHtml(new DefaultTagRenderer(DUMMY_ATTRIBUTES_CUSTOMIZER, myIncludeSrcPositions));
    }

    public class HtmlGeneratingVisitor extends MarkdownRecursiveVisitor {
        private final TagRenderer myTagRenderer;

        public HtmlGeneratingVisitor(TagRenderer tagRenderer) {
            myTagRenderer = tagRenderer;
        }

        @Override
        public void visitNode(ASTNode node) {
            GeneratingProvider provider = myProviders.get(node.getElementType());
            if (provider != null) {
                provider.processNode(this, myMarkdownText, node);
            } else {
                MarkdownAstUtil.acceptChildren(node, this);
            }
        }

        public void visitLeaf(ASTNode node) {
            GeneratingProvider provider = myProviders.get(node.getElementType());
            if (provider != null) {
                provider.processNode(this, myMarkdownText, node);
            } else {
                consumeHtml(leafText(myMarkdownText, node));
            }
        }

        public void consumeTagOpen(ASTNode node,
                                   CharSequence tagName,
                                   CharSequence... attributes) {
            myHtmlString.append(myTagRenderer.openTag(node, tagName, false, attributes));
        }

        public void consumeTagOpen(ASTNode node,
                                   CharSequence tagName,
                                   boolean autoClose,
                                   CharSequence... attributes) {
            myHtmlString.append(myTagRenderer.openTag(node, tagName, autoClose, attributes));
        }

        public void consumeTagClose(CharSequence tagName) {
            myHtmlString.append(myTagRenderer.closeTag(tagName));
        }

        public void consumeHtml(CharSequence html) {
            myHtmlString.append(myTagRenderer.printHtml(html));
        }
    }

    public static class DefaultTagRenderer implements TagRenderer {
        protected final AttributesCustomizer myCustomizer;
        protected final boolean myIncludeSrcPositions;

        public DefaultTagRenderer(AttributesCustomizer customizer, boolean includeSrcPositions) {
            myCustomizer = customizer;
            myIncludeSrcPositions = includeSrcPositions;
        }

        @Override
        public CharSequence openTag(ASTNode node, CharSequence tagName, boolean autoClose, CharSequence... attributes) {
            StringBuilder sb = new StringBuilder();
            sb.append("<").append(tagName);
            for (CharSequence attribute : myCustomizer.customize(node, tagName, Arrays.asList(attributes))) {
                if (attribute != null) {
                    sb.append(" ").append(attribute);
                }
            }
            if (myIncludeSrcPositions) {
                sb.append(" ").append(getSrcPosAttribute(node));
            }
            if (autoClose) {
                sb.append(" />");
            } else {
                sb.append(">");
            }
            return sb;
        }

        @Override
        public CharSequence closeTag(CharSequence tagName) {
            return "</" + tagName + ">";
        }

        @Override
        public CharSequence printHtml(CharSequence html) {
            return html;
        }
    }

    public interface TagRenderer {
        CharSequence openTag(ASTNode node,
                             CharSequence tagName,
                             boolean autoClose,
                             CharSequence... attributes);

        CharSequence closeTag(CharSequence tagName);

        CharSequence printHtml(CharSequence html);
    }

    public static CharSequence leafText(String text, ASTNode node, boolean replaceEscapesAndEntities) {
        if (node.getElementType() == MarkdownTokenTypes.BLOCK_QUOTE) {
            return "";
        }
        return EntityConverter.replaceEntities(
                MarkdownAstUtil.getTextInNode(node, text),
                replaceEscapesAndEntities,
                replaceEscapesAndEntities
        );
    }

    public static CharSequence leafText(String text, ASTNode node) {
        return leafText(text, node, true);
    }

    public static CharSequence getSrcPosAttribute(ASTNode node) {
        return SRC_ATTRIBUTE_NAME + "=\"" + node.getStartOffset() + ".." + MarkdownAstUtil.endOffset(node) + "\"";
    }

    public static CharSequence trimIndents(CharSequence text, int indent) {
        if (indent == 0) {
            return text;
        }

        StringBuilder buffer = new StringBuilder();
        int lastFlushed = 0;
        int offset = 0;
        while (offset < text.length()) {
            if (offset == 0 || text.charAt(offset - 1) == '\n') {
                buffer.append(text, lastFlushed, offset);
                int indentEaten = 0;

                while (indentEaten < indent && offset < text.length()) {
                    char ch = text.charAt(offset);
                    if (ch == ' ') {
                        indentEaten++;
                    } else if (ch == '\t') {
                        indentEaten += 4 - indentEaten % 4;
                    } else {
                        break;
                    }
                    offset++;
                }

                if (indentEaten > indent) {
                    for (int i = 0; i < indentEaten - indent; i++) {
                        buffer.append(' ');
                    }
                }
                lastFlushed = offset;
            }
            offset++;
        }

        buffer.append(text, lastFlushed, text.length());
        return buffer;
    }
}
