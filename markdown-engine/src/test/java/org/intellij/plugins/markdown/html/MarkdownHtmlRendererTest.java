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
import org.intellij.plugins.markdown.lang.MarkdownFileType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Golden test for the preview's markdown-to-HTML conversion, in the configuration that ships:
 * the GFM flavour and {@code includeSrcPositions = true}, which scroll sync depends on.
 * <p>
 * The expectations here were generated while the generator still walked the engine's own tree.
 * They are unchanged, so this test doubles as the proof that rendering from the real PSI tree
 * produces byte-identical output.
 */
public class MarkdownHtmlRendererTest extends SimpleParsingTest<Object> {
    private static final String OVERWRITE_PROPERTY = "markdown.golden.overwrite";
    private static final URI BASE_URI = URI.create("file:/docs/");
    private static final Path CORPUS = Paths.get("src", "test", "resources", "preview");

    public MarkdownHtmlRendererTest() {
        super("preview", "md");
    }

    @Test public void testSimple(Context c) throws Exception { check(c, "simple"); }
    @Test public void testHeaders(Context c) throws Exception { check(c, "headers"); }
    @Test public void testLinks(Context c) throws Exception { check(c, "links"); }
    @Test public void testCodeFence(Context c) throws Exception { check(c, "codeFence"); }
    @Test public void testGfmTable(Context c) throws Exception { check(c, "gfmTable"); }
    @Test public void testEntities(Context c) throws Exception { check(c, "entities"); }
    @Test public void testEscaping(Context c) throws Exception { check(c, "escaping"); }

    @Test
    public void testEmitsSrcPositions(Context context) throws Exception {
        assertTrue(render(context, "hi", "# hi\n\ntext\n").contains("md-src-pos"),
                   "the preview relies on md-src-pos attributes for scroll sync, but none were emitted");
    }

    private void check(Context context, String name) throws Exception {
        String source = Files.readString(CORPUS.resolve(name + ".md"), StandardCharsets.UTF_8);
        String actual = render(context, name, source);

        Path expected = CORPUS.resolve(name + ".html");
        if (Boolean.getBoolean(OVERWRITE_PROPERTY)) {
            Files.writeString(expected, actual, StandardCharsets.UTF_8);
            return;
        }
        assertEquals(Files.readString(expected, StandardCharsets.UTF_8), actual,
                     () -> "Preview HTML changed for '" + name + "'");
    }

    private String render(Context context, String name, String source) throws Exception {
        PsiFile file = createFile(context, null, name + ".md", MarkdownFileType.INSTANCE, source);
        ensureParsed(file);
        return MarkdownHtmlRenderer.renderTree(file.getNode(), source, BASE_URI);
    }

    @Nonnull
    @Override
    protected LanguageFileType getFileType(@Nonnull Context context, @Nullable Object o) {
        return MarkdownFileType.INSTANCE;
    }
}
