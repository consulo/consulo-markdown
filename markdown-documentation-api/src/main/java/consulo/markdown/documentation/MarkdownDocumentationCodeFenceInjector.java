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

import consulo.annotation.access.RequiredReadAction;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.inject.MultiHostInjector;
import consulo.language.inject.MultiHostRegistrar;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiLanguageInjectionHost;
import org.intellij.plugins.markdown.injection.aliases.CodeFenceLanguageGuesser;

import java.util.ArrayList;
import java.util.List;

/**
 * Injects the language a code fence of a Markdown documentation comment names, for instance {@code java} in {@code ```java}, so its
 * code is highlighted in the editor. Problems in the injected code are not shown - code in documentation is usually a fragment.
 * <p>
 * Every line of the code becomes a separate place, without its line prefix. A fence without a language is left alone.
 * <p>
 * A language registers a subclass for the code fence element of its {@link MarkdownDocumentationProvider}.
 */
public abstract class MarkdownDocumentationCodeFenceInjector implements MultiHostInjector {
    @Override
    public abstract Class<? extends PsiLanguageInjectionHost> getElementClass();

    @Override
    @RequiredReadAction
    public void injectLanguages(MultiHostRegistrar registrar, PsiElement context) {
        if (!(context instanceof PsiLanguageInjectionHost codeFence) || !codeFence.isValidHost()) {
            return;
        }

        MarkdownDocumentationProvider provider = MarkdownDocumentationProvider.findByCodeFence(codeFence);
        if (provider == null) {
            return;
        }

        String info = provider.getCodeFenceInfo(codeFence);
        if (info == null) {
            return;
        }

        Language language = CodeFenceLanguageGuesser.guessLanguageForInjection(info);
        if (language == null) {
            return;
        }

        List<TextRange> ranges = findCodeRanges(codeFence.getText(), provider.getLinePrefixes());
        if (ranges.isEmpty()) {
            return;
        }

        registrar.startInjecting(language);
        for (TextRange range : ranges) {
            registrar.addPlace(null, null, codeFence, range);
        }
        registrar.doneInjecting();
    }

    /**
     * @return The ranges of the code lines inside the fence text - between the opening and the closing fence lines, without the line
     * prefix and its following space, each with its line break except the last one
     */
    public static List<TextRange> findCodeRanges(String text, List<String> linePrefixes) {
        List<TextRange> lines = new ArrayList<>();
        int lineStart = text.indexOf('\n');
        if (lineStart < 0) {
            return lines;
        }
        lineStart++;

        while (lineStart < text.length()) {
            int lineEnd = text.indexOf('\n', lineStart);
            boolean lastLine = lineEnd < 0;
            if (lastLine) {
                lineEnd = text.length();
            }

            int codeStart = lineStart;
            for (String prefix : linePrefixes) {
                int index = text.indexOf(prefix, lineStart);
                if (index >= 0 && index < lineEnd) {
                    codeStart = index + prefix.length();
                    break;
                }
            }
            if (codeStart < lineEnd && text.charAt(codeStart) == ' ') {
                codeStart++;
            }

            String trimmed = text.substring(codeStart, lineEnd).trim();
            if (lastLine && (trimmed.startsWith("```") || trimmed.startsWith("~~~"))) {
                break;
            }

            lines.add(new TextRange(codeStart, lastLine ? lineEnd : lineEnd + 1));
            if (lastLine) {
                break;
            }
            lineStart = lineEnd + 1;
        }

        // the line break of the last code line belongs to the closing fence line
        if (!lines.isEmpty()) {
            TextRange last = lines.get(lines.size() - 1);
            if (last.getEndOffset() > 0 && text.charAt(last.getEndOffset() - 1) == '\n') {
                lines.set(lines.size() - 1, new TextRange(last.getStartOffset(), last.getEndOffset() - 1));
            }
        }
        return lines;
    }
}
