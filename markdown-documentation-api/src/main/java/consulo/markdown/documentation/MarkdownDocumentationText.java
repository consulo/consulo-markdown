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
package consulo.markdown.documentation;

import consulo.document.util.TextRange;

import java.util.ArrayList;
import java.util.List;

/**
 * The Markdown of a documentation comment: its text without the prefix of each line ({@code ///}), and the offset in the comment
 * of each of its chars.
 */
public final class MarkdownDocumentationText {
    private final String myText;
    private final int[] myOffsets;

    private MarkdownDocumentationText(String text, int[] offsets) {
        myText = text;
        myOffsets = offsets;
    }

    /**
     * @param commentText  the text of the comment
     * @param linePrefixes the prefixes of the lines, the first one found on a line is removed with everything before it
     */
    public static MarkdownDocumentationText create(CharSequence commentText, List<String> linePrefixes) {
        String text = commentText.toString();
        StringBuilder markdown = new StringBuilder(text.length());
        int[] offsets = new int[text.length() + 1];
        int lineStart = 0;
        while (lineStart <= text.length()) {
            int lineEnd = text.indexOf('\n', lineStart);
            if (lineEnd < 0) {
                lineEnd = text.length();
            }
            int contentStart = lineStart;
            int prefixStart = -1;
            for (String prefix : linePrefixes) {
                int index = text.indexOf(prefix, lineStart);
                if (index >= 0 && index < lineEnd && (prefixStart < 0 || index < prefixStart)) {
                    prefixStart = index;
                    contentStart = index + prefix.length();
                }
            }
            int contentEnd = lineEnd < text.length() ? lineEnd + 1 : lineEnd;
            for (int i = contentStart; i < contentEnd; i++) {
                offsets[markdown.length()] = i;
                markdown.append(text.charAt(i));
            }
            lineStart = lineEnd + 1;
        }
        offsets[markdown.length()] = text.length();
        return new MarkdownDocumentationText(markdown.toString(), offsets);
    }

    /**
     * @return The Markdown, without the line prefixes
     */
    public String getText() {
        return myText;
    }

    /**
     * @return The offset in the comment of an offset in {@link #getText()}
     */
    public int toCommentOffset(int markdownOffset) {
        return myOffsets[markdownOffset];
    }

    /**
     * @return The ranges in the comment a range of {@link #getText()} covers - one per line, as the prefixes between them are not part
     * of it
     */
    public List<TextRange> toCommentRanges(TextRange markdownRange) {
        List<TextRange> result = new ArrayList<>();
        int start = markdownRange.getStartOffset();
        int end = markdownRange.getEndOffset();
        int segmentStart = start;
        for (int i = start + 1; i <= end; i++) {
            if (i == end || myOffsets[i] != myOffsets[i - 1] + 1) {
                int from = myOffsets[segmentStart];
                int to = myOffsets[i - 1] + 1;
                if (from < to) {
                    result.add(new TextRange(from, to));
                }
                segmentStart = i;
            }
        }
        return result;
    }
}
