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

import consulo.language.file.LanguageFileType;
import consulo.language.psi.PsiFile;
import consulo.test.junit.impl.language.SimpleParsingTest;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor;
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor;
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.plugins.markdown.lang.MarkdownFileType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Conformance against the upstream HTML corpus.
 * <p>
 * Reproduces upstream's own generator configuration - no source positions, the default tag
 * renderer, and upstream's tag-splitting formatter - so the shipped expectations apply unchanged.
 * The parse tree, however, is always what the platform produces for a markdown file, i.e. GFM.
 * Documents whose expectations were generated with a flavour that parses them differently are
 * listed in {@link #FLAVOUR_DIVERGENT} and reported rather than silently skipped.
 */
public class UpstreamHtmlCorpusTest extends SimpleParsingTest<Object> {
    private static final Path CORPUS = Paths.get("src", "test", "resources", "html");

    private static final Map<String, String> BASE_URI = Map.of(
        "baseUriHttp", "http://example.com/foo/bar.html",
        "baseUriRelativeRoot", "/user/repo-name/blob/master",
        "baseUriRelativeNoRoot", "user/repo-name/blob/master",
        "baseUriWithBadRelativeUrl", "user/repo-name/blob/master",
        "baseUriWithAnchorLink", "/user/repo-name/blob/master",
        "baseUriWithAnchorLinkForceResolve", "/user/repo-name/blob/master");

    /** Needs a bespoke tag renderer, or the Space flavour, which is not vendored. */
    private static final Set<String> UNSUPPORTED =
        Set.of("customRenderer", "images", "sfmAutolink", "sfmAutolinkWithUsername");

    /** Documents upstream drives with GFM rather than CommonMark. */
    private static final Set<String> GFM = Set.of(
        "bug28", "checkedLists", "gfmAutolink", "gfmAutolinkWithUsername", "gfmTable",
        "gfmTableWithBlockQuote", "ruby17351", "strikethrough", "tableInsideBlockQuoteInListItem",
        "tableInsideNestedListItem", "tableInsideOrderedListItem", "tableInsideUnorderedListItem");

    /**
     * Documents that do not reproduce upstream's expectation, asserted so the set cannot drift.
     * <p>
     * Nine are the engine's version gap - its baseline is upstream 0.7.5-0.7.7 while these
     * expectations come from 0.7.9+, and they are the code-fence and tables-in-list-item families
     * that changed in between. {@code gitBook} and {@code goPlugin} differ for a structural reason
     * instead: the parse tree is always GFM here, because that is what the platform produces for a
     * markdown file, whereas upstream generated those two from a CommonMark tree.
     */
    private static final Set<String> FLAVOUR_DIVERGENT = Set.of(
        "codeFence", "diffCodeFences", "gfmTableWithBlockQuote", "gitBook", "goPlugin",
        "htmlBlocks", "indentedCodeBlockInsideListItem", "tableInsideBlockQuoteInListItem",
        "tableInsideNestedListItem", "tableInsideOrderedListItem", "tableInsideUnorderedListItem");

    private static MarkdownFlavourDescriptor flavourFor(String name) {
        if ("gfmAutolinkWithHttps".equals(name)) {
            return new GFMFlavourDescriptor(true, false, true);
        }
        if ("baseUriWithAnchorLinkForceResolve".equals(name)) {
            return new CommonMarkFlavourDescriptor(true, true);
        }
        return GFM.contains(name) ? new GFMFlavourDescriptor() : new CommonMarkFlavourDescriptor();
    }

    public UpstreamHtmlCorpusTest() {
        super("html", "md");
    }

    @Test
    public void upstreamCorpus(Context context) throws Exception {
        List<String> matched = new ArrayList<>();
        List<String> differed = new ArrayList<>();
        List<Path> inputs = new ArrayList<>();
        try (java.util.stream.Stream<Path> s = Files.list(CORPUS)) {
            s.filter(p -> p.getFileName().toString().endsWith(".md")).sorted().forEach(inputs::add);
        }
        for (Path input : inputs) {
            String name = input.getFileName().toString().replaceAll("\\.md$", "");
            Path expected = CORPUS.resolve(name + ".txt");
            if (UNSUPPORTED.contains(name) || !Files.exists(expected)) {
                continue;
            }
            String source = Files.readString(input, StandardCharsets.UTF_8);
            PsiFile file = createFile(context, null, name + ".md", MarkdownFileType.INSTANCE, source);
            ensureParsed(file);
            URI baseUri = BASE_URI.containsKey(name) ? new URI(BASE_URI.get(name)) : null;
            String actual = format(MarkdownHtmlRenderer.renderTree(
                file.getNode(), source, baseUri, flavourFor(name), false,
                new HtmlGenerator.DefaultTagRenderer(HtmlGenerator.DUMMY_ATTRIBUTES_CUSTOMIZER, false)));
            (Files.readString(expected, StandardCharsets.UTF_8).equals(actual) ? matched : differed).add(name);
        }
        System.out.println("upstream-html: matched=" + matched.size() + " differed=" + differed.size());
        System.out.println("differed: " + differed);
        assertTrue(matched.size() + differed.size() > 0, "corpus not found");
        assertEquals(FLAVOUR_DIVERGENT, Set.copyOf(differed),
                     "the set of documents diverging from upstream changed");
    }

    /** Port of upstream {@code HtmlGeneratorTestBase.formatHtmlForTests}. */
    private static String format(String html) {
        Matcher m = Pattern.compile("</?[^>]+>").matcher(html);
        List<int[]> spans = new ArrayList<>();
        while (m.find()) {
            spans.add(new int[]{m.start(), m.end()});
        }
        StringBuilder out = new StringBuilder();
        int last = 0;
        for (int i = 0; i < spans.size(); i++) {
            int[] span = spans.get(i);
            out.append(html, last, span[0]);
            String value = html.substring(span[0], span[1]);
            int[] next = i + 1 < spans.size() ? spans.get(i + 1) : null;
            String nextValue = next != null ? html.substring(next[0], next[1]) : "";
            boolean inline = (html.charAt(span[0] + 1) != '/' && next != null && html.charAt(next[0] + 1) == '/')
                             || (value.contains("pre") && nextValue.contains("code"))
                             || (value.contains("/code") && nextValue.contains("/pre"));
            out.append(value);
            if (!inline) {
                out.append('\n');
            }
            last = span[1];
        }
        return out.append(html, last, html.length()).toString();
    }

    @Nonnull
    @Override
    protected LanguageFileType getFileType(@Nonnull Context context, @Nullable Object o) {
        return MarkdownFileType.INSTANCE;
    }
}
