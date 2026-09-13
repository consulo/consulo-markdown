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
import org.intellij.markdown.MarkdownElementTypes;
import consulo.language.ast.ASTNode;

import org.intellij.markdown.parser.LinkMap;

import java.net.URI;

public class InlineLinkGeneratingProvider extends LinkGeneratingProvider {
    public InlineLinkGeneratingProvider(URI baseURI, boolean resolveAnchors) {
        super(baseURI, resolveAnchors);
    }

    public InlineLinkGeneratingProvider(URI baseURI) {
        super(baseURI, false);
    }

    @Override
    public RenderInfo getRenderInfo(String text, ASTNode node) {
        ASTNode label = MarkdownAstUtil.findChildOfType(node, MarkdownElementTypes.LINK_TEXT);
        if (label == null) {
            return null;
        }

        ASTNode destNode = MarkdownAstUtil.findChildOfType(node, MarkdownElementTypes.LINK_DESTINATION);
        CharSequence destination;
        if (destNode != null) {
            destination = LinkMap.normalizeDestination(MarkdownAstUtil.getTextInNode(destNode, text), true);
        } else {
            destination = "";
        }

        ASTNode titleNode = MarkdownAstUtil.findChildOfType(node, MarkdownElementTypes.LINK_TITLE);
        CharSequence title = null;
        if (titleNode != null) {
            title = LinkMap.normalizeTitle(MarkdownAstUtil.getTextInNode(titleNode, text));
        }

        return new RenderInfo(label, destination, title);
    }
}
