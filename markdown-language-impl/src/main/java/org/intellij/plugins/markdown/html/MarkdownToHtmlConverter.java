// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
/*
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
package org.intellij.plugins.markdown.html;

import consulo.language.ast.ASTNode;
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.markdown.parser.LinkMap;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.plugins.markdown.html.TreeBuildingSink;
import org.jspecify.annotations.Nullable;

import java.net.URI;

/**
 * Converts markdown text to HTML, outside of any file - the markdown is parsed into a standalone
 * tree with the given flavour.
 */
public final class MarkdownToHtmlConverter {
    private final MarkdownFlavourDescriptor myFlavourDescriptor;

    public MarkdownToHtmlConverter(MarkdownFlavourDescriptor flavourDescriptor) {
        myFlavourDescriptor = flavourDescriptor;
    }

    public String convertMarkdownToHtml(String markdownText) {
        return convertMarkdownToHtml(markdownText, null);
    }

    public String convertMarkdownToHtml(String markdownText, @Nullable String server) {
        MarkdownParser parser = new MarkdownParser(myFlavourDescriptor);
        TreeBuildingSink sink = new TreeBuildingSink(markdownText);
        parser.walkBlocks(parser.parseBlocks(MarkdownElementTypes.MARKDOWN_FILE, markdownText), markdownText, true, sink);
        ASTNode parsedTree = sink.getRoot();
        var providers = myFlavourDescriptor.createHtmlGeneratingProviders(
            LinkMap.buildLinkMap(parsedTree, markdownText),
            server != null ? URI.create(server) : null
        );

        return new HtmlGenerator(markdownText, parsedTree, providers, false).generateHtml();
    }
}
