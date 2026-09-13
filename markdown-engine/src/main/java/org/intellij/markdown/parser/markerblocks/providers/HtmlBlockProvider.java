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
package org.intellij.markdown.parser.markerblocks.providers;

import org.intellij.markdown.lexer.Compat;
import org.intellij.markdown.parser.LookaheadText;
import org.intellij.markdown.parser.MarkerProcessor;
import org.intellij.markdown.parser.ProductionHolder;
import org.intellij.markdown.parser.constraints.MarkdownConstraints;
import org.intellij.markdown.parser.markerblocks.MarkerBlock;
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider;
import org.intellij.markdown.parser.markerblocks.impl.HtmlBlockMarkerBlock;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class HtmlBlockProvider implements MarkerBlockProvider<MarkerProcessor.StateInfo> {

    public static final String TAG_NAMES =
            "address, article, aside, base, basefont, blockquote, body, caption, center, col, colgroup, dd, details, " +
                    "dialog, dir, div, dl, dt, fieldset, figcaption, figure, footer, form, frame, frameset, h1, " +
                    "head, header, hr, html, legend, li, link, main, menu, menuitem, meta, nav, noframes, ol, " +
                    "optgroup, option, p, param, pre, section, source, title, summary, table, tbody, td, tfoot, " +
                    "th, thead, title, tr, track, ul";

    public static final String TAG_NAME = "[a-zA-Z][a-zA-Z0-9-]*";

    public static final String ATTR_NAME = "[A-Za-z:_][A-Za-z0-9_.:-]*";

    public static final String ATTR_VALUE = "\\s*=\\s*(?:[^ \"'=<>`]+|'[^']*'|\"[^\"]*\")";

    public static final String ATTRIBUTE = "\\s+" + ATTR_NAME + "(?:" + ATTR_VALUE + ")?";

    public static final String OPEN_TAG = "<" + TAG_NAME + "(?:" + ATTRIBUTE + ")*\\s*/?>";

    /**
     * Closing tag allowance is not in public spec version yet
     */
    public static final String CLOSE_TAG = "</" + TAG_NAME + "\\s*>";

    private static final String TAG_NAMES_ALTERNATION = TAG_NAMES.replace(", ", "|");

    /**
     * see http://spec.commonmark.org/0.21/#html-blocks
     *
     * nulls mean "Next line should be blank"
     */
    private static final OpenCloseEntry[] OPEN_CLOSE_REGEXES = {
            new OpenCloseEntry(
                    Pattern.compile("<(?:script|pre|style)(?: |>|$)", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("</(?:script|style|pre)>", Pattern.CASE_INSENSITIVE)),
            new OpenCloseEntry(
                    Pattern.compile("<!--"),
                    Pattern.compile("-->")),
            new OpenCloseEntry(
                    Pattern.compile("<\\?"),
                    Pattern.compile("\\?>")),
            new OpenCloseEntry(
                    Pattern.compile("<![A-Z]"),
                    Pattern.compile(">")),
            new OpenCloseEntry(
                    Pattern.compile("<!\\[CDATA\\["),
                    Pattern.compile("\\]\\]>")),
            new OpenCloseEntry(
                    Pattern.compile("</?(?:" + TAG_NAMES_ALTERNATION + ")(?: |/?>|$)", Pattern.CASE_INSENSITIVE),
                    null),
            new OpenCloseEntry(
                    Pattern.compile("(?:" + OPEN_TAG + "|" + CLOSE_TAG + ")(?: |$)"),
                    null)
    };

    private static final Pattern FIND_START_REGEX;

    static {
        StringBuilder sb = new StringBuilder("^(");
        for (int i = 0; i < OPEN_CLOSE_REGEXES.length; i++) {
            if (i > 0) {
                sb.append("|");
            }
            sb.append("(").append(OPEN_CLOSE_REGEXES[i].myOpenPattern.pattern()).append(")");
        }
        sb.append(")");
        FIND_START_REGEX = Pattern.compile(sb.toString());
    }

    @Override
    
    public List<MarkerBlock> createMarkerBlocks(LookaheadText.Position pos,
                                                ProductionHolder productionHolder,
                                                MarkerProcessor.StateInfo stateInfo) {
        int matchingGroup = matches(pos, stateInfo.getCurrentConstraints());
        if (matchingGroup != -1) {
            Pattern closePattern = OPEN_CLOSE_REGEXES[matchingGroup].myClosePattern;
            return Collections.singletonList(new HtmlBlockMarkerBlock(
                    stateInfo.getCurrentConstraints(), productionHolder, closePattern, pos));
        }
        return Collections.emptyList();
    }

    @Override
    public boolean interruptsParagraph(LookaheadText.Position pos,
                                       MarkdownConstraints constraints) {
        int m = matches(pos, constraints);
        return m >= 0 && m <= 5;
    }

    private int matches(LookaheadText.Position pos,
                        MarkdownConstraints constraints) {
        if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, constraints)) {
            return -1;
        }
        CharSequence text = pos.getCurrentLineFromPosition();
        int offset = MarkerBlockProvider.passSmallIndent(text);
        if (offset >= text.length() || text.charAt(offset) != '<') {
            return -1;
        }

        java.util.regex.Matcher matchResult = FIND_START_REGEX.matcher(text.subSequence(offset, text.length()));
        if (!matchResult.find()) {
            return -1;
        }
        Compat.assertCondition(matchResult.groupCount() == OPEN_CLOSE_REGEXES.length + 1,
                "There are some excess capturing groups probably!");
        for (int i = 0; i < OPEN_CLOSE_REGEXES.length; i++) {
            if (matchResult.group(i + 2) != null) {
                return i;
            }
        }
        Compat.assertCondition(false, "Match found but all groups are empty!");
        return -1;
    }

    private static final class OpenCloseEntry {
        final Pattern myOpenPattern;
        final @Nullable Pattern myClosePattern;

        OpenCloseEntry(Pattern openPattern, @Nullable Pattern closePattern) {
            myOpenPattern = openPattern;
            myClosePattern = closePattern;
        }
    }
}
