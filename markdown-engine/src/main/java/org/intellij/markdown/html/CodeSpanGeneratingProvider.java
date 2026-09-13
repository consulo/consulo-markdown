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

public final class CodeSpanGeneratingProvider implements GeneratingProvider {
    @Override
    public void processNode(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
        List<ASTNode> children = MarkdownAstUtil.children(node);
        List<ASTNode> nodes = children.subList(1, children.size() - 1);
        StringBuilder sb = new StringBuilder();
        for (ASTNode n : nodes) {
            sb.append(HtmlGenerator.leafText(text, n, false));
        }
        String output = sb.toString().trim();
        visitor.consumeTagOpen(node, "code");
        visitor.consumeHtml(output);
        visitor.consumeTagClose("code");
    }
}
