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

import consulo.language.ast.ASTNode;
import org.intellij.markdown.MarkdownAstUtil;

import org.intellij.markdown.html.GeneratingProvider;
import org.intellij.markdown.html.HtmlGenerator;

import java.util.List;

class MathGeneratingProvider implements GeneratingProvider {
    private final boolean myInline;

    MathGeneratingProvider(boolean inline) {
        myInline = inline;
    }

    MathGeneratingProvider() {
        this(false);
    }

    @Override
    public void processNode(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
        List<ASTNode> nodes = MarkdownAstUtil.children(node).subList(1, MarkdownAstUtil.children(node).size() - 1);
        StringBuilder sb = new StringBuilder();
        for (ASTNode child : nodes) {
            sb.append(MarkdownAstUtil.getTextInNode(child, text));
        }
        String output = trimSurroundingBackticksAndWhitespaces(sb.toString());
        visitor.consumeTagOpen(node, "span", "class=\"math\"", "inline = \"" + myInline + "\"");
        visitor.consumeHtml(output);
        visitor.consumeTagClose("span");
    }

    private String trimSurroundingBackticksAndWhitespaces(String s) {
        int i = 0;
        while (s.length() - i > 2 && s.charAt(i) == '`' && s.charAt(s.length() - 1 - i) == '`') {
            i++;
        }
        return s.substring(i, s.length() - i).trim();
    }
}
