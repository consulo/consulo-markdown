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
import java.util.regex.Pattern;

public class ImageGeneratingProvider extends LinkGeneratingProvider {
    private static final Pattern REGEX = Pattern.compile("[^a-zA-Z0-9 ]");

    protected final ReferenceLinksGeneratingProvider myReferenceLinkProvider;
    protected final InlineLinkGeneratingProvider myInlineLinkProvider;

    public ImageGeneratingProvider(LinkMap linkMap, URI baseURI) {
        super(baseURI);
        myReferenceLinkProvider = new ReferenceLinksGeneratingProvider(linkMap, baseURI);
        myInlineLinkProvider = new InlineLinkGeneratingProvider(baseURI);
    }

    @Override
    public RenderInfo getRenderInfo(String text, ASTNode node) {
        ASTNode inlineLinkNode = MarkdownAstUtil.findChildOfType(node, MarkdownElementTypes.INLINE_LINK);
        if (inlineLinkNode != null) {
            return myInlineLinkProvider.getRenderInfo(text, inlineLinkNode);
        }

        ASTNode refLinkNode = MarkdownAstUtil.findChildOfType(node, MarkdownElementTypes.FULL_REFERENCE_LINK);
        if (refLinkNode == null) {
            refLinkNode = MarkdownAstUtil.findChildOfType(node, MarkdownElementTypes.SHORT_REFERENCE_LINK);
        }
        if (refLinkNode != null) {
            return myReferenceLinkProvider.getRenderInfo(text, refLinkNode);
        }

        return null;
    }

    @Override
    public void renderLink(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node, RenderInfo info) {
        visitor.consumeTagOpen(node, "img",
                true,
                "src=\"" + makeAbsoluteUrl(info.getDestination()) + "\"",
                "alt=\"" + getPlainTextFrom(info.getLabel(), text) + "\"",
                info.getTitle() != null ? "title=\"" + info.getTitle() + "\"" : null);
    }

    private CharSequence getPlainTextFrom(ASTNode node, String text) {
        return REGEX.matcher(MarkdownAstUtil.getTextInNode(node, text)).replaceAll("");
    }
}
