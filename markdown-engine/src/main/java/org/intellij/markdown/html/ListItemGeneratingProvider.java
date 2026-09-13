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

import org.intellij.markdown.MarkdownElementTypes;
import consulo.language.ast.ASTNode;
import org.intellij.markdown.MarkdownAstUtil;
import org.intellij.markdown.lexer.Compat;

public final class ListItemGeneratingProvider extends SimpleTagProvider {
    public ListItemGeneratingProvider() {
        super("li");
    }

    private static final InlineHolderGeneratingProvider SILENT_PARAGRAPH_GENERATING_PROVIDER =
            new InlineHolderGeneratingProvider() {
                @Override
                public void openTag(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
                }

                @Override
                public void closeTag(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
                }
            };

    @Override
    public void processNode(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
        Compat.assertCondition(node.getElementType() == MarkdownElementTypes.LIST_ITEM, "expected a list item");

        openTag(visitor, text, node);

        ASTNode listNode = node.getTreeParent();
        Compat.assertCondition(listNode.getElementType() == MarkdownElementTypes.UNORDERED_LIST
                || listNode.getElementType() == MarkdownElementTypes.ORDERED_LIST, "expected a list");
        boolean isLoose = MarkdownAstUtil.isLooseList(listNode);

        for (ASTNode child : MarkdownAstUtil.children(node)) {
            if (child.getElementType() == MarkdownElementTypes.PARAGRAPH && !isLoose) {
                SILENT_PARAGRAPH_GENERATING_PROVIDER.processNode(visitor, text, child);
            } else {
                MarkdownAstUtil.accept(child, visitor);
            }
        }

        closeTag(visitor, text, node);
    }
}
