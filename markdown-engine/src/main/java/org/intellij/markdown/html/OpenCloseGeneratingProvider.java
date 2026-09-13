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

import org.intellij.markdown.MarkdownAstUtil;
import consulo.language.ast.ASTNode;

public abstract class OpenCloseGeneratingProvider implements GeneratingProvider {
    public abstract void openTag(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node);

    public abstract void closeTag(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node);

    @Override
    public void processNode(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
        openTag(visitor, text, node);
        MarkdownAstUtil.acceptChildren(node, visitor);
        closeTag(visitor, text, node);
    }
}
