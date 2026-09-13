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

import consulo.language.ast.ASTNode;
import org.intellij.markdown.MarkdownAstUtil;

import java.util.List;

public class SimpleInlineTagProvider extends InlineHolderGeneratingProvider {
    private final String myTagName;
    private final int myRenderFrom;
    private final int myRenderTo;

    public SimpleInlineTagProvider(String tagName, int renderFrom, int renderTo) {
        myTagName = tagName;
        myRenderFrom = renderFrom;
        myRenderTo = renderTo;
    }

    public SimpleInlineTagProvider(String tagName) {
        this(tagName, 0, 0);
    }

    public String getTagName() {
        return myTagName;
    }

    public int getRenderFrom() {
        return myRenderFrom;
    }

    public int getRenderTo() {
        return myRenderTo;
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
        return MarkdownAstUtil.children(node).subList(myRenderFrom, MarkdownAstUtil.children(node).size() + myRenderTo);
    }
}
