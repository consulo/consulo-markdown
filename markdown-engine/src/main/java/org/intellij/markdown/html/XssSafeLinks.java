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
import java.util.regex.Pattern;

public final class XssSafeLinks {
    private XssSafeLinks() {
    }

    private static final Pattern UNSAFE_LINK_REGEX = Pattern.compile(
            "^(vbscript|javascript|file|data):", Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ALLOWED_DATA_LINK_REGEX = Pattern.compile(
            "^data:image/(gif|png|jpeg|webp);", Pattern.CASE_INSENSITIVE
    );

    public static CharSequence makeXssSafeDestination(CharSequence s) {
        String trimmed = s.toString().trim();
        if (UNSAFE_LINK_REGEX.matcher(trimmed).find()) {
            if (ALLOWED_DATA_LINK_REGEX.matcher(trimmed).find()) {
                return s;
            }
            return "#";
        }
        return s;
    }

    public static LinkGeneratingProvider makeXssSafe(LinkGeneratingProvider provider, boolean useSafeLinks) {
        if (!useSafeLinks) return provider;

        return new LinkGeneratingProvider(provider.getBaseURI(), provider.getResolveAnchors()) {
            @Override
            public void renderLink(
                    HtmlGenerator.HtmlGeneratingVisitor visitor,
                    String text,
                    ASTNode node,
                    RenderInfo info) {
                provider.renderLink(visitor, text, node, info);
            }

            @Override
            public RenderInfo getRenderInfo(String text, ASTNode node) {
                RenderInfo info = provider.getRenderInfo(text, node);
                if (info == null) {
                    return null;
                }
                return new RenderInfo(
                        info.getLabel(),
                        makeXssSafeDestination(info.getDestination()),
                        info.getTitle()
                );
            }
        };
    }

    public static LinkGeneratingProvider makeXssSafe(LinkGeneratingProvider provider) {
        return makeXssSafe(provider, true);
    }
}
