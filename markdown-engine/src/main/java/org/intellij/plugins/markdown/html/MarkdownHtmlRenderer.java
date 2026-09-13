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
package org.intellij.plugins.markdown.html;

import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor;
import consulo.virtualFileSystem.VirtualFile;
import org.intellij.markdown.html.GeneratingProvider;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.markdown.parser.LinkMap;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.markdown.parser.MarkdownParserManager;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.net.URI;
import java.util.Map;

/**
 * Renders markdown to the HTML shown in the preview.
 * <p>
 * Split out of {@link MarkdownPreviewFileEditor} so the conversion is reachable without an editor
 * or a virtual file: {@link #render} depends only on the engine, while the virtual-file-dependent
 * part is confined to {@link #baseUriFor}.
 */
public final class MarkdownHtmlRenderer {
    /** The preview renders source positions so that scroll sync can map HTML back to the document. */
    private static final boolean INCLUDE_SRC_POSITIONS = true;

    private MarkdownHtmlRenderer() {
    }

    /**
     * Base URI against which relative links and images in {@code file} resolve, or {@code null}
     * for a file with no parent directory.
     */
    public static @Nullable URI baseUriFor(VirtualFile file) {
        VirtualFile parent = file.getParent();
        return parent != null ? new File(parent.getPath()).toURI() : null;
    }

    /** Renders from an already-parsed tree, in the preview's configuration. */
    public static String renderTree(ASTNode root, String text, @Nullable URI baseUri) {
        return renderTree(root, text, baseUri, MarkdownParserManager.FLAVOUR, INCLUDE_SRC_POSITIONS, null);
    }

    /**
     * Renders from an already-parsed tree.
     * <p>
     * The tree is whatever the platform parsed - always the GFM flavour for a markdown file. The
     * {@code flavour} argument only chooses the HTML generating providers, so a caller such as the
     * Rust doc renderer can supply its own link handling without needing its own parser.
     *
     * @param tagRenderer {@code null} for the default renderer
     */
    public static String renderTree(ASTNode root,
                                    String text,
                                    @Nullable URI baseUri,
                                    MarkdownFlavourDescriptor flavour,
                                    boolean includeSrcPositions,
                                    HtmlGenerator.@Nullable TagRenderer tagRenderer) {
        Map<IElementType, GeneratingProvider> providers =
            flavour.createHtmlGeneratingProviders(LinkMap.buildLinkMap(root, text), baseUri);
        HtmlGenerator generator = new HtmlGenerator(text, root, providers, includeSrcPositions);
        return tagRenderer != null
            ? generator.generateHtml(tagRenderer)
            : generator.generateHtml();
    }

}
