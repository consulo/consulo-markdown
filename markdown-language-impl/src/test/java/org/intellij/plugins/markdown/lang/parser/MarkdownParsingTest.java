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
package org.intellij.plugins.markdown.lang.parser;

import consulo.language.file.LanguageFileType;
import consulo.language.psi.PsiFile;
import consulo.test.junit.impl.language.SimpleParsingTest;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.plugins.markdown.lang.MarkdownFileType;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * PSI-level parsing gate.
 * <p>
 * Where the engine's golden tests check the parse result in isolation, this one goes through the
 * real platform path — {@code MarkdownParserDefinition} -> lexer -> {@code PsiBuilder} -> PSI —
 * so it covers the wiring the engine tests cannot: chameleon reparse of inline content, element
 * types reaching the tree, and whitespace placement in the built tree.
 */
public class MarkdownParsingTest extends SimpleParsingTest<Object> {
    public MarkdownParsingTest() {
        super("parsing", "md");
    }

    @Test
    public void testBlockquotes(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testBrokenTable(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testBug28(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testCheckLists(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testCodeBlocks(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testCodeFence(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testCodeSpan(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testEa79689(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testEmphStrong(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testEscapedPipeInsideTableCell(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testExample208(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testExample221(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testExample226(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testGfmAlerts(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testGfmAutolink(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testGfmAutolinkWithUsername(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testGfmTable(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testGfmTableWithBlockQuote(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testHardLineBreaks(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testHeaders(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testHruleAndSetext(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testHtmlBlocks(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testImages(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testIndentedCodeBlockInsideListItem(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testInlineLinks(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testLeadingAndTrailingWhitespaces(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testLinkDefinitions(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testMath(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testMultilevel(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testNewlinesAndAnyChars(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testNonEnglishSentences(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testOrderedLists(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testPuppetApache(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testReferenceLinks(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testRuby16750(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testRuby17337(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testRuby18237(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testRuby18936(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testSfmAutolink(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testSfmAutolinkWithUsername(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testSimple(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testStrikethrough(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testStrikethroughNoSpacesPunctuationInside(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testStrikethroughNoSpacesWordInside(Context context) throws Exception {
        doTest(context, null);
    }

    /**
     * A table whose last line is a bare {@code |}: the row is one character long and ends at the
     * same offset as the table. Regression test - suppression used to be keyed on a live offset
     * range, which accepted such a row's open event and rejected its close, unbalancing the marker
     * stack and reporting it as intersecting nodes.
     */
    @Test
    public void testTableTrailingPipe(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testTabStops(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testTableInsideBlockQuoteInListItem(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testTableInsideBlockQuoteWithMissingLastPipe(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testTableInsideNestedListItem(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testTableInsideOrderedListItem(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testTableInsideUnorderedListItem(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testTightLooseLists(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testUnorderedLists(Context context) throws Exception {
        doTest(context, null);
    }

    /**
     * Writes the expectation instead of asserting when {@code -Dmarkdown.golden.overwrite=true},
     * matching the engine module's golden tests. Review every diff before accepting one.
     */
    @Override
    protected void checkResult(@Nonnull Context context, @Nullable Object o, @Nonnull PsiFile file) throws Exception {
        if (!Boolean.getBoolean("markdown.golden.overwrite")) {
            super.checkResult(context, o, file);
            return;
        }
        String actual = toParseTreeText(file, skipSpaces(), includeRanges()).trim();
        Path expected = Paths.get("src", "test", "resources", "parsing",
                                  getFileName(context, o).replaceAll("\\.md$", "") + ".txt");
        Files.createDirectories(expected.getParent());
        Files.writeString(expected, actual, StandardCharsets.UTF_8);
    }

    @Nonnull
    @Override
    protected LanguageFileType getFileType(@Nonnull Context context, @Nullable Object o) {
        return MarkdownFileType.INSTANCE;
    }
}
