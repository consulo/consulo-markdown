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

import org.intellij.markdown.MarkdownElementTypes;
import consulo.language.ast.ASTNode;
import org.intellij.markdown.MarkdownAstUtil;


import org.intellij.markdown.html.GeneratingProvider;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.markdown.html.InlineHolderGeneratingProvider;
import org.intellij.markdown.html.SimpleTagProvider;
import org.intellij.markdown.lexer.Compat;

class CheckedListItemGeneratingProvider extends SimpleTagProvider {
    CheckedListItemGeneratingProvider() {
        super("li");
    }

    @Override
    public void openTag(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
    }

    @Override
    public void processNode(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
        Compat.assertCondition(node.getElementType() == MarkdownElementTypes.LIST_ITEM, "expected a list item");

        ASTNode checkBoxElement = MarkdownAstUtil.findChildOfType(node, GFMTokenTypes.CHECK_BOX);
        CharSequence inputHtml;
        CharSequence listItemClass;
        if (checkBoxElement != null) {
            listItemClass = "class=\"task-list-item\"";
            String checkedString = getIsCheckedString(checkBoxElement, text);
            inputHtml = "<input type=\"checkbox\" class=\"task-list-item-checkbox\"" + checkedString + " disabled />";
        } else {
            listItemClass = null;
            inputHtml = "";
        }

        visitor.consumeTagOpen(node, "li", listItemClass);

        ASTNode listNode = node.getTreeParent();
        Compat.assertCondition(listNode.getElementType() == MarkdownElementTypes.UNORDERED_LIST
                || listNode.getElementType() == MarkdownElementTypes.ORDERED_LIST, "expected a list");

        boolean isLoose = MarkdownAstUtil.isLooseList(listNode);

        boolean flushedInput = false;
        for (ASTNode child : MarkdownAstUtil.children(node)) {
            if (MarkdownAstUtil.isLeaf(child)) {
                continue;
            }
            if (!flushedInput) {
                if (child.getElementType() == MarkdownElementTypes.PARAGRAPH) {
                    new SubParagraphGeneratingProvider(isLoose, inputHtml.toString()).processNode(visitor, text, child);
                } else {
                    visitor.consumeHtml(inputHtml);
                    MarkdownAstUtil.accept(child, visitor);
                }
                flushedInput = true;
            } else {
                MarkdownAstUtil.accept(child, visitor);
            }
        }

        closeTag(visitor, text, node);
    }

    private String getIsCheckedString(ASTNode node, String text) {
        boolean isChecked = false;
        if (node != null) {
            CharSequence nodeText = MarkdownAstUtil.getTextInNode(node, text);
            if (nodeText.length() > 1 && nodeText.charAt(1) != ' ') {
                isChecked = true;
            }
        }
        return isChecked ? " checked" : "";
    }

    private static class SubParagraphGeneratingProvider extends InlineHolderGeneratingProvider {
        private final boolean myWrapInParagraph;
        private final String myInputHtml;

        SubParagraphGeneratingProvider(boolean wrapInParagraph, String inputHtml) {
            myWrapInParagraph = wrapInParagraph;
            myInputHtml = inputHtml;
        }

        @Override
        public void openTag(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
            if (myWrapInParagraph) {
                visitor.consumeTagOpen(node, "p");
            }
            visitor.consumeHtml(myInputHtml);
        }

        @Override
        public void closeTag(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
            if (myWrapInParagraph) {
                visitor.consumeTagClose("p");
            }
        }
    }
}
