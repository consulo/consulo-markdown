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

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.util.dataholder.Key;
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor;
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class MarkdownParserManager {
    public static final GFMFlavourDescriptor FLAVOUR = new GFMFlavourDescriptor();

    /**
     * The flavour a Markdown-based file is parsed with, put on the file by the parser definition of a dialect that
     * brings its own flavour (MDX). Files without it use {@link #FLAVOUR}.
     */
    public static final Key<MarkdownFlavourDescriptor> FLAVOUR_DESCRIPTION = Key.create("Markdown.Flavour");

    private static final ThreadLocal<ParsingInfo> ourLastParsingResult = new ThreadLocal<>();

    /**
     * The flavour of the file containing {@code element}: {@link #FLAVOUR_DESCRIPTION} of the file (or of the file it
     * is a copy of), else {@link #FLAVOUR}.
     */
    public static MarkdownFlavourDescriptor getFlavour(@Nullable PsiElement element) {
        PsiFile file = element == null ? null : element.getContainingFile();
        if (file == null) {
            return FLAVOUR;
        }
        MarkdownFlavourDescriptor flavour = file.getUserData(FLAVOUR_DESCRIPTION);
        if (flavour == null && file.getOriginalFile() != file) {
            flavour = file.getOriginalFile().getUserData(FLAVOUR_DESCRIPTION);
        }
        return flavour == null ? FLAVOUR : flavour;
    }

    /**
     * Block productions for {@code buffer}, cached per thread.
     * <p>
     * One reparse asks for these twice - once by the lexer, which needs the leaf tokens, and once
     * by the parser, which needs the structure - because markdown token identity depends on block
     * context and so cannot be decided by a streaming lexer. Without the cache every document
     * would be parsed twice on every keystroke.
     */
    public static List<SequentialParser.Node> parseBlocks(CharSequence buffer) {
        return parseBlocks(buffer, FLAVOUR);
    }

    /**
     * {@link #parseBlocks(CharSequence)} with the block parser of {@code flavour}.
     */
    public static List<SequentialParser.Node> parseBlocks(CharSequence buffer, MarkdownFlavourDescriptor flavour) {
        ParsingInfo info = ourLastParsingResult.get();
        if (info != null && info.myFlavour == flavour && info.myBufferHash == buffer.hashCode() && info.myBuffer.equals(buffer)) {
            return info.myProductions;
        }

        List<SequentialParser.Node> productions =
            new MarkdownParser(flavour).parseBlocks(MarkdownElementTypes.MARKDOWN_FILE, buffer);
        ourLastParsingResult.set(new ParsingInfo(buffer, flavour, productions));
        return productions;
    }

    private static class ParsingInfo {
        final CharSequence myBuffer;
        final int myBufferHash;
        final MarkdownFlavourDescriptor myFlavour;
        final List<SequentialParser.Node> myProductions;

        ParsingInfo(CharSequence buffer, MarkdownFlavourDescriptor flavour, List<SequentialParser.Node> productions) {
            myBuffer = buffer;
            myBufferHash = myBuffer.hashCode();
            myFlavour = flavour;
            myProductions = productions;
        }
    }
}
