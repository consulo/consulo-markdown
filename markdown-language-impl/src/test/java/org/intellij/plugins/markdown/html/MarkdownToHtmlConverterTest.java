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

import consulo.language.file.LanguageFileType;
import consulo.language.psi.PsiFile;
import consulo.test.junit.impl.language.SimpleParsingTest;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.markdown.parser.MarkdownParserManager;
import org.intellij.plugins.markdown.lang.MarkdownFileType;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link MarkdownToHtmlConverter} parses into a standalone tree; for the same flavour its HTML must
 * be identical to rendering the PSI tree of a markdown file.
 */
public class MarkdownToHtmlConverterTest extends SimpleParsingTest<Object> {
    private static final Path CORPUS = Paths.get("src", "test", "resources", "preview");

    public MarkdownToHtmlConverterTest() {
        super("preview", "md");
    }

    @Test public void testSimple(Context c) throws Exception { check(c, "simple"); }
    @Test public void testHeaders(Context c) throws Exception { check(c, "headers"); }
    @Test public void testLinks(Context c) throws Exception { check(c, "links"); }
    @Test public void testCodeFence(Context c) throws Exception { check(c, "codeFence"); }
    @Test public void testGfmTable(Context c) throws Exception { check(c, "gfmTable"); }
    @Test public void testEntities(Context c) throws Exception { check(c, "entities"); }
    @Test public void testEscaping(Context c) throws Exception { check(c, "escaping"); }

    private void check(Context context, String name) throws Exception {
        String source = Files.readString(CORPUS.resolve(name + ".md"), StandardCharsets.UTF_8);

        PsiFile file = createFile(context, null, name + ".md", MarkdownFileType.INSTANCE, source);
        ensureParsed(file);
        String expected = MarkdownHtmlRenderer.renderTree(file.getNode(), source, null, MarkdownParserManager.FLAVOUR, false, null);

        String actual = new MarkdownToHtmlConverter(MarkdownParserManager.FLAVOUR).convertMarkdownToHtml(source);
        assertEquals(expected, actual, () -> "Standalone HTML differs from the PSI tree HTML for '" + name + "'");
    }

    @Nonnull
    @Override
    protected LanguageFileType getFileType(@Nonnull Context context, @Nullable Object o) {
        return MarkdownFileType.INSTANCE;
    }
}
