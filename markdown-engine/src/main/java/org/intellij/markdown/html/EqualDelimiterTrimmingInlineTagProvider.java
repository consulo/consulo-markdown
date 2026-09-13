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
import consulo.language.ast.ASTNode;
import org.intellij.markdown.MarkdownAstUtil;

import java.util.List;

/**
 * Trims an equal number of delimiters from the start and end off the children.
 * Meant to be used to trim surrounding tildes ('~') for strikethroughs.
 */
public class EqualDelimiterTrimmingInlineTagProvider extends InlineHolderGeneratingProvider {
    private final String myTagName;
    private final IElementType myDelimiterType;

    public EqualDelimiterTrimmingInlineTagProvider(String tagName, IElementType delimiterType) {
        myTagName = tagName;
        myDelimiterType = delimiterType;
    }

    public String getTagName() {
        return myTagName;
    }

    public IElementType getDelimiterType() {
        return myDelimiterType;
    }

    @Override
    public void openTag(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
        visitor.consumeTagOpen(node, myTagName);
    }

    @Override
    public void closeTag(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
        visitor.consumeTagClose(myTagName);
    }

    @Override
    public List<ASTNode> childrenToRender(ASTNode node) {
        List<ASTNode> children = MarkdownAstUtil.children(node);
        if (children.isEmpty()) return children;

        int left = 0;
        int right = children.size() - 1;

        while (children.get(left).getElementType() == myDelimiterType
                && children.get(right).getElementType() == myDelimiterType) {
            left += 1;
            right -= 1;

            if (left >= right) break;
        }

        return children.subList(left, right + 1);
    }
}
