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
package org.intellij.markdown.parser;

import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;

import java.util.List;

public class MarkdownParserManager {
    public static final GFMFlavourDescriptor FLAVOUR = new GFMFlavourDescriptor();

    private static final ThreadLocal<ParsingInfo> ourLastParsingResult = new ThreadLocal<>();

    /**
     * Block productions for {@code buffer}, cached per thread.
     * <p>
     * One reparse asks for these twice - once by the lexer, which needs the leaf tokens, and once
     * by the parser, which needs the structure - because markdown token identity depends on block
     * context and so cannot be decided by a streaming lexer. Without the cache every document
     * would be parsed twice on every keystroke.
     */
    public static List<SequentialParser.Node> parseBlocks(CharSequence buffer) {
        ParsingInfo info = ourLastParsingResult.get();
        if (info != null && info.myBufferHash == buffer.hashCode() && info.myBuffer.equals(buffer)) {
            return info.myProductions;
        }

        List<SequentialParser.Node> productions =
            new MarkdownParser(FLAVOUR).parseBlocks(MarkdownElementTypes.MARKDOWN_FILE, buffer);
        ourLastParsingResult.set(new ParsingInfo(buffer, productions));
        return productions;
    }

    private static class ParsingInfo {
        final CharSequence myBuffer;
        final int myBufferHash;
        final List<SequentialParser.Node> myProductions;

        ParsingInfo(CharSequence buffer, List<SequentialParser.Node> productions) {
            myBuffer = buffer;
            myBufferHash = myBuffer.hashCode();
            myProductions = productions;
        }
    }
}
