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

import org.intellij.markdown.html.entities.EntityConverter;
import org.intellij.markdown.parser.LinkMap;

import java.net.URI;

public class ReferenceLinksGeneratingProvider extends LinkGeneratingProvider {
    private final LinkMap myLinkMap;

    public ReferenceLinksGeneratingProvider(LinkMap linkMap, URI baseURI, boolean resolveAnchors) {
        super(baseURI, resolveAnchors);
        myLinkMap = linkMap;
    }

    public ReferenceLinksGeneratingProvider(LinkMap linkMap, URI baseURI) {
        this(linkMap, baseURI, false);
    }

    @Override
    public RenderInfo getRenderInfo(String text, ASTNode node) {
        ASTNode label = null;
        for (ASTNode child : MarkdownAstUtil.children(node)) {
            if (child.getElementType() == MarkdownElementTypes.LINK_LABEL) {
                label = child;
                break;
            }
        }
        if (label == null) {
            return null;
        }

        LinkMap.LinkInfo linkInfo = myLinkMap.getLinkInfo(MarkdownAstUtil.getTextInNode(label, text));
        if (linkInfo == null) {
            return null;
        }

        ASTNode linkTextNode = null;
        for (ASTNode child : MarkdownAstUtil.children(node)) {
            if (child.getElementType() == MarkdownElementTypes.LINK_TEXT) {
                linkTextNode = child;
                break;
            }
        }

        return new RenderInfo(
                linkTextNode != null ? linkTextNode : label,
                EntityConverter.replaceEntities(linkInfo.getDestination(), true, true),
                linkInfo.getTitle() != null
                        ? EntityConverter.replaceEntities(linkInfo.getTitle(), true, true)
                        : null
        );
    }
}
