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

import java.net.URI;

public abstract class LinkGeneratingProvider implements GeneratingProvider {
    private final URI myBaseURI;
    private final boolean myResolveAnchors;

    protected static final TransparentInlineHolderProvider FALLBACK_PROVIDER = new TransparentInlineHolderProvider();
    protected static final TransparentInlineHolderProvider LABEL_PROVIDER = new TransparentInlineHolderProvider(1, -1);

    public LinkGeneratingProvider(URI baseURI, boolean resolveAnchors) {
        myBaseURI = baseURI;
        myResolveAnchors = resolveAnchors;
    }

    public LinkGeneratingProvider(URI baseURI) {
        this(baseURI, false);
    }

    public URI getBaseURI() {
        return myBaseURI;
    }

    public boolean getResolveAnchors() {
        return myResolveAnchors;
    }

    @Override
    public final void processNode(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
        RenderInfo info = getRenderInfo(text, node);
        if (info == null) {
            FALLBACK_PROVIDER.processNode(visitor, text, node);
            return;
        }
        renderLink(visitor, text, node, info);
    }

    protected CharSequence makeAbsoluteUrl(CharSequence destination) {
        if (!myResolveAnchors && destination.length() > 0 && destination.charAt(0) == '#') {
            return destination;
        }
        if (myBaseURI != null) {
            return CommonDefs.resolveToStringSafe(myBaseURI, destination.toString());
        }
        return destination;
    }

    public void renderLink(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node, RenderInfo info) {
        CharSequence titleAttr = info.getTitle() != null ? "title=\"" + info.getTitle() + "\"" : null;
        visitor.consumeTagOpen(node, "a", "href=\"" + makeAbsoluteUrl(info.getDestination()) + "\"", titleAttr);
        LABEL_PROVIDER.processNode(visitor, text, info.getLabel());
        visitor.consumeTagClose("a");
    }

    public abstract RenderInfo getRenderInfo(String text, ASTNode node);

    public static final class RenderInfo {
        private final ASTNode myLabel;
        private final CharSequence myDestination;
        private final CharSequence myTitle;

        public RenderInfo(ASTNode label, CharSequence destination, CharSequence title) {
            myLabel = label;
            myDestination = destination;
            myTitle = title;
        }

        public ASTNode getLabel() {
            return myLabel;
        }

        public CharSequence getDestination() {
            return myDestination;
        }

        public CharSequence getTitle() {
            return myTitle;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RenderInfo that = (RenderInfo) o;
            if (!myLabel.equals(that.myLabel)) return false;
            if (!myDestination.equals(that.myDestination)) return false;
            return myTitle != null ? myTitle.equals(that.myTitle) : that.myTitle == null;
        }

        @Override
        public int hashCode() {
            int result = myLabel.hashCode();
            result = 31 * result + myDestination.hashCode();
            result = 31 * result + (myTitle != null ? myTitle.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "RenderInfo{" +
                    "label=" + myLabel +
                    ", destination=" + myDestination +
                    ", title=" + myTitle +
                    '}';
        }
    }
}
