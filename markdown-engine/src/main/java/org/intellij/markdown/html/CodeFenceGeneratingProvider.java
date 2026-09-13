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

import org.intellij.markdown.MarkdownTokenTypes;
import consulo.language.ast.ASTNode;
import org.intellij.markdown.MarkdownAstUtil;


import java.util.ArrayList;
import java.util.List;

public final class CodeFenceGeneratingProvider implements GeneratingProvider {
    @Override
    public void processNode(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
        CharSequence nodeText = MarkdownAstUtil.getTextInNode(node, text);
        int indentBefore = commonPrefixLength(nodeText, "          ");

        visitor.consumeHtml("<pre>");

        int state = 0;

        List<ASTNode> childrenToConsider = MarkdownAstUtil.children(node);
        if (childrenToConsider.get(childrenToConsider.size() - 1).getElementType() == MarkdownTokenTypes.CODE_FENCE_END) {
            childrenToConsider = childrenToConsider.subList(0, childrenToConsider.size() - 1);
        }

        boolean lastChildWasContent = false;

        ArrayList<String> attributes = new ArrayList<>();
        for (ASTNode child : childrenToConsider) {
            if (state == 1 && (child.getElementType() == MarkdownTokenTypes.CODE_FENCE_CONTENT
                    || child.getElementType() == MarkdownTokenTypes.EOL)) {
                visitor.consumeHtml(
                        HtmlGenerator.trimIndents(
                                HtmlGenerator.leafText(text, child, false),
                                indentBefore
                        )
                );
                lastChildWasContent = child.getElementType() == MarkdownTokenTypes.CODE_FENCE_CONTENT;
            }
            if (state == 0 && child.getElementType() == MarkdownTokenTypes.FENCE_LANG) {
                String langText = HtmlGenerator.leafText(text, child).toString().trim().split(" ")[0];
                attributes.add("class=\"language-" + langText + "\"");
            }
            if (state == 0 && child.getElementType() == MarkdownTokenTypes.EOL) {
                visitor.consumeTagOpen(node, "code", attributes.toArray(new CharSequence[0]));
                state = 1;
            }
        }
        if (state == 0) {
            visitor.consumeTagOpen(node, "code", attributes.toArray(new CharSequence[0]));
        }
        if (lastChildWasContent) {
            visitor.consumeHtml("\n");
        }
        visitor.consumeHtml("</code></pre>");
    }

    private static int commonPrefixLength(CharSequence text, String prefix) {
        int len = Math.min(text.length(), prefix.length());
        for (int i = 0; i < len; i++) {
            if (text.charAt(i) != prefix.charAt(i)) {
                return i;
            }
        }
        return len;
    }
}
