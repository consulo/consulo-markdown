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
package consulo.markdown.preview;

import consulo.annotation.access.RequiredReadAction;
import consulo.colorScheme.EditorColorsScheme;
import consulo.colorScheme.TextAttributes;
import consulo.colorScheme.TextAttributesKey;
import consulo.language.Language;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.language.editor.highlight.SyntaxHighlighterFactory;
import consulo.language.file.light.LightVirtualFile;
import consulo.language.lexer.Lexer;
import consulo.markdown.injection.LanguageGuesser;
import consulo.project.Project;
import consulo.ui.color.ColorValue;
import consulo.ui.util.ColorValueUtil;
import consulo.util.lang.StringUtil;
import org.intellij.plugins.markdown.html.CodeFenceContentRenderer;
import org.jspecify.annotations.Nullable;

/**
 * Highlights the code of a code fence in the preview by the syntax highlighter of the language of its info string: every token
 * gets the foreground color of the color scheme, as an inline style
 */
public class CodeFenceHighlighter implements CodeFenceContentRenderer {
    private final Project myProject;
    private final EditorColorsScheme myColorsScheme;

    public CodeFenceHighlighter(Project project, EditorColorsScheme colorsScheme) {
        myProject = project;
        myColorsScheme = colorsScheme;
    }

    @Override
    @RequiredReadAction
    public @Nullable CharSequence render(String infoString, String code) {
        if (infoString.isEmpty()) {
            return null;
        }

        Language language = myProject.getApplication()
            .getExtensionPoint(LanguageGuesser.class)
            .computeSafeIfAny(guesser -> guesser.guessLanguage(infoString));
        if (language == null) {
            return null;
        }

        SyntaxHighlighter highlighter =
            SyntaxHighlighterFactory.getSyntaxHighlighter(language, myProject, new LightVirtualFile("markdown_temp", language, code));
        Lexer lexer = highlighter.getHighlightingLexer();
        lexer.start(code);

        StringBuilder html = new StringBuilder(code.length() * 2);
        while (lexer.getTokenType() != null) {
            String tokenHtml = StringUtil.escapeXmlEntities(code.substring(lexer.getTokenStart(), lexer.getTokenEnd()));
            ColorValue color = getForegroundColor(highlighter.getTokenHighlights(lexer.getTokenType()));
            if (color != null) {
                html.append("<span style=\"color: #").append(ColorValueUtil.toHex(color)).append(";\">").append(tokenHtml).append("</span>");
            }
            else {
                html.append(tokenHtml);
            }
            lexer.advance();
        }
        return html;
    }

    /**
     * The color of the last key, the most specific one
     */
    private @Nullable ColorValue getForegroundColor(TextAttributesKey[] keys) {
        if (keys.length == 0) {
            return null;
        }
        TextAttributesKey key = keys[keys.length - 1];
        TextAttributes attributes = myColorsScheme.getAttributes(key);
        if (attributes != null && attributes.getForegroundColor() != null) {
            return attributes.getForegroundColor();
        }
        TextAttributes defaultAttributes = key.getDefaultAttributes();
        return defaultAttributes == null ? null : defaultAttributes.getForegroundColor();
    }
}
