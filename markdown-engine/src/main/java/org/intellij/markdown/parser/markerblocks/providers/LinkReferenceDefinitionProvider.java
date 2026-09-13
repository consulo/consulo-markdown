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

import consulo.language.ast.IElementType;
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.parser.LookaheadText;
import org.intellij.markdown.parser.MarkerProcessor;
import org.intellij.markdown.parser.ProductionHolder;
import org.intellij.markdown.parser.constraints.MarkdownConstraints;
import org.intellij.markdown.parser.markerblocks.MarkerBlock;
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider;
import org.intellij.markdown.parser.markerblocks.impl.LinkReferenceDefinitionMarkerBlock;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LinkReferenceDefinitionProvider implements MarkerBlockProvider<MarkerProcessor.StateInfo> {

    @Override
    
    public List<MarkerBlock> createMarkerBlocks(LookaheadText.Position pos,
                                                ProductionHolder productionHolder,
                                                MarkerProcessor.StateInfo stateInfo) {
        if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, stateInfo.getCurrentConstraints())) {
            return Collections.emptyList();
        }

        List<int[]> matchResult = matchLinkDefinition(pos.getOriginalText(), pos.getOffset());
        if (matchResult == null) {
            return Collections.emptyList();
        }
        for (int i = 0; i < matchResult.size(); i++) {
            int[] range = matchResult.get(i);
            IElementType type;
            switch (i) {
                case 0:
                    type = MarkdownElementTypes.LINK_LABEL;
                    break;
                case 1:
                    type = MarkdownElementTypes.LINK_DESTINATION;
                    break;
                case 2:
                    type = MarkdownElementTypes.LINK_TITLE;
                    break;
                default:
                    throw new AssertionError("There are no more than three groups in this regex");
            }
            int[] widened = addToRangeAndWiden(range, 0);
            productionHolder.addProduction(Collections.singletonList(
                    new SequentialParser.Node(widened[0], widened[1], type)));
        }

        int[] lastRange = matchResult.get(matchResult.size() - 1);
        int matchLength = lastRange[1] - pos.getOffset() + 1;
        LookaheadText.Position endPosition = pos.nextPosition(matchLength);

        if (endPosition != null && !isEndOfLine(endPosition)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new LinkReferenceDefinitionMarkerBlock(
                stateInfo.getCurrentConstraints(), productionHolder.mark(),
                pos.getOffset() + matchLength));
    }

    @Override
    public boolean interruptsParagraph(LookaheadText.Position pos,
                                       MarkdownConstraints constraints) {
        return false;
    }

    
    public static int[] addToRangeAndWiden(int[] range, int t) {
        return new int[]{range[0] + t, range[1] + t + 1};
    }

    public static boolean isEndOfLine(LookaheadText.Position pos) {
        return pos.getOffsetInCurrentLine() == -1 || pos.charsToNonWhitespace() == null;
    }

    public static @Nullable List<int[]> matchLinkDefinition(CharSequence text, int startOffset) {
        int offset = MarkerBlockProvider.passSmallIndent(text, startOffset);
        int[] linkLabel = matchLinkLabel(text, offset);
        if (linkLabel == null) {
            return null;
        }
        offset = linkLabel[1] + 1;
        if (offset >= text.length() || text.charAt(offset) != ':') {
            return null;
        }
        offset++;

        offset = passOneNewline(text, offset);

        int[] destination = matchLinkDestination(text, offset);
        if (destination == null) {
            return null;
        }
        offset = destination[1] + 1;
        offset = passOneNewline(text, offset);

        int[] title = matchLinkTitle(text, offset);

        List<int[]> result = new ArrayList<>();
        result.add(linkLabel);
        result.add(destination);
        if (title != null) {
            offset = title[1] + 1;
            while (offset < text.length() && isSpace(text.charAt(offset))) {
                offset++;
            }
            if (offset >= text.length() || text.charAt(offset) == '\n') {
                result.add(title);
            }
        }

        return result;
    }

    @Nullable
    public static int[] matchLinkDestination(CharSequence text, int start) {
        if (start >= text.length()) {
            return null;
        }

        int offset = start;
        if (text.charAt(offset) == '<') {
            offset++;
            while (offset < text.length()) {
                char c = text.charAt(offset);
                if (c == '>') {
                    return new int[]{start, offset};
                }
                if (c == '<' || isSpaceOrNewline(c)) {
                    return null;
                }
                if (c == '\\' && offset + 1 < text.length() && !isSpaceOrNewline(text.charAt(offset + 1))) {
                    offset++;
                }
                offset++;
            }
            return null;
        } else {
            boolean hasParens = false;
            while (offset < text.length()) {
                char c = text.charAt(offset);
                if (isSpaceOrNewline(c) || (int) c <= 27) {
                    break;
                }
                if (c == '(') {
                    if (hasParens) {
                        break;
                    } else {
                        hasParens = true;
                    }
                } else if (c == ')') {
                    if (!hasParens) {
                        break;
                    } else {
                        hasParens = false;
                    }
                } else if (c == '\\' && offset + 1 < text.length() && !isSpaceOrNewline(text.charAt(offset + 1))) {
                    offset++;
                }
                offset++;
            }
            if (start == offset) {
                return null;
            } else {
                return new int[]{start, offset - 1};
            }
        }
    }

    @Nullable
    public static int[] matchLinkTitle(CharSequence text, int start) {
        if (start >= text.length()) {
            return null;
        }

        char startChar = text.charAt(start);
        char endDelim;
        switch (startChar) {
            case '\'':
                endDelim = '\'';
                break;
            case '"':
                endDelim = '"';
                break;
            case '(':
                endDelim = ')';
                break;
            default:
                return null;
        }

        int offset = start + 1;
        boolean isBlank = false;
        while (offset < text.length()) {
            char c = text.charAt(offset);
            if (c == endDelim) {
                return new int[]{start, offset};
            }
            if (c == '\n') {
                if (isBlank) {
                    return null;
                } else {
                    isBlank = true;
                }
            } else if (!isSpace(c)) {
                isBlank = false;
            }

            if (c == '\\' && offset + 1 < text.length() && !isSpaceOrNewline(text.charAt(offset + 1))) {
                offset++;
            }
            offset++;
        }
        return null;
    }

    @Nullable
    public static int[] matchLinkLabel(CharSequence text, int start) {
        int offset = start;
        if (offset >= text.length() || text.charAt(offset) != '[') {
            return null;
        }
        offset++;

        boolean seenNonWhitespace = false;

        for (int i = 1; i <= 999; i++) {
            if (offset >= text.length()) {
                return null;
            }
            char c = text.charAt(offset);
            if (c == '[' || c == ']') {
                break;
            }
            if (c == '\\') {
                offset++;
                if (offset >= text.length()) {
                    return null;
                }
                c = text.charAt(offset);
            }
            if (!Character.isWhitespace(c)) {
                seenNonWhitespace = true;
            }
            offset++;
        }
        if (!seenNonWhitespace || offset >= text.length() || text.charAt(offset) != ']') {
            return null;
        }
        return new int[]{start, offset};
    }

    private static int passOneNewline(CharSequence text, int start) {
        int offset = start;
        while (offset < text.length() && isSpace(text.charAt(offset))) {
            offset++;
        }
        if (offset < text.length() && text.charAt(offset) == '\n') {
            offset++;
            while (offset < text.length() && isSpace(text.charAt(offset))) {
                offset++;
            }
        }
        return offset;
    }

    private static boolean isSpace(char c) {
        return c == ' ' || c == '\t';
    }

    private static boolean isSpaceOrNewline(char c) {
        return isSpace(c) || c == '\n';
    }
}
