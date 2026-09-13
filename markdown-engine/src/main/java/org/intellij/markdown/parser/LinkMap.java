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
package org.intellij.markdown.parser;

import consulo.language.ast.IElementType;
import org.intellij.markdown.MarkdownElementTypes;
import consulo.language.ast.ASTNode;
import org.intellij.markdown.MarkdownAstUtil;

import org.intellij.markdown.MarkdownRecursiveVisitor;
import org.intellij.markdown.html.CommonDefs;
import org.intellij.markdown.html.entities.EntityConverter;
import org.intellij.markdown.lexer.Compat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public final class LinkMap {
    private final Map<CharSequence, LinkInfo> myMap;

    private static final Pattern SPACES_REGEX = Pattern.compile("\\s+");

    public LinkMap(Map<CharSequence, LinkInfo> map) {
        myMap = map;
    }

    public LinkInfo getLinkInfo(CharSequence label) {
        return myMap.get(normalizeLabel(label));
    }

    public static CharSequence normalizeLabel(CharSequence label) {
        return SPACES_REGEX.matcher(label).replaceAll(" ").toLowerCase();
    }

    public static LinkMap buildLinkMap(ASTNode root, CharSequence text) {
        Map<CharSequence, LinkInfo> map = new HashMap<>();

        MarkdownAstUtil.accept(root, new MarkdownRecursiveVisitor() {
            @Override
            public void visitNode(ASTNode node) {
                if (node.getElementType() == MarkdownElementTypes.LINK_DEFINITION) {
                    ASTNode labelNode = null;
                    for (ASTNode child : MarkdownAstUtil.children(node)) {
                        if (child.getElementType() == MarkdownElementTypes.LINK_LABEL) {
                            labelNode = child;
                            break;
                        }
                    }
                    if (labelNode != null) {
                        CharSequence linkLabel = normalizeLabel(MarkdownAstUtil.getTextInNode(labelNode, text));
                        if (!map.containsKey(linkLabel)) {
                            map.put(linkLabel, LinkInfo.create(node, text));
                        }
                    }
                } else {
                    super.visitNode(node);
                }
            }
        });

        return new LinkMap(map);
    }

    public static CharSequence normalizeDestination(CharSequence s, boolean processEscapes) {
        String destination = EntityConverter.replaceEntities(clearBounding(s, "<>"), true, processEscapes);
        StringBuilder sb = new StringBuilder();
        Compat.forEachCodePoint(destination, code -> {
            char c = (char) code;
            if (code == 32) {
                sb.append("%20");
            } else if (code < 32 || code >= 128 || "\".<>\\^_`{|}".indexOf(c) >= 0) {
                sb.append(CommonDefs.urlEncode(Compat.codePointToString(code)));
            } else {
                sb.append(c);
            }
        });
        return sb.toString();
    }

    public static CharSequence normalizeTitle(CharSequence s) {
        return EntityConverter.replaceEntities(
                clearBounding(s, "\"\"", "''", "()"),
                true,
                true
        );
    }

    private static CharSequence clearBounding(CharSequence s, String... boundQuotes) {
        if (s.length() == 0) {
            return s;
        }
        for (String quotePair : boundQuotes) {
            if (s.charAt(0) == quotePair.charAt(0) && s.charAt(s.length() - 1) == quotePair.charAt(1)) {
                return s.subSequence(1, s.length() - 1);
            }
        }
        return s;
    }

    public static final class LinkInfo {
        private final ASTNode myNode;
        private final CharSequence myDestination;
        private final CharSequence myTitle;

        public LinkInfo(ASTNode node, CharSequence destination, CharSequence title) {
            myNode = node;
            myDestination = destination;
            myTitle = title;
        }

        public ASTNode getNode() {
            return myNode;
        }

        public CharSequence getDestination() {
            return myDestination;
        }

        public CharSequence getTitle() {
            return myTitle;
        }

        public static LinkInfo create(ASTNode node, CharSequence fileText) {
            ASTNode destNode = null;
            ASTNode titleNode = null;
            for (ASTNode child : MarkdownAstUtil.children(node)) {
                if (child.getElementType() == MarkdownElementTypes.LINK_DESTINATION) {
                    destNode = child;
                } else if (child.getElementType() == MarkdownElementTypes.LINK_TITLE) {
                    titleNode = child;
                }
            }
            CharSequence destination = destNode != null
                    ? normalizeDestination(MarkdownAstUtil.getTextInNode(destNode, fileText), true)
                    : "";
            CharSequence title = titleNode != null
                    ? normalizeTitle(MarkdownAstUtil.getTextInNode(titleNode, fileText))
                    : null;
            return new LinkInfo(node, destination, title);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LinkInfo)) return false;
            LinkInfo other = (LinkInfo) o;
            return Objects.equals(myNode, other.myNode)
                    && Objects.equals(myDestination.toString(), other.myDestination.toString())
                    && Objects.equals(myTitle != null ? myTitle.toString() : null,
                                     other.myTitle != null ? other.myTitle.toString() : null);
        }

        @Override
        public int hashCode() {
            return Objects.hash(myNode, myDestination.toString(), myTitle != null ? myTitle.toString() : null);
        }

        @Override
        public String toString() {
            return "LinkInfo(node=" + myNode + ", destination=" + myDestination + ", title=" + myTitle + ")";
        }
    }
}
