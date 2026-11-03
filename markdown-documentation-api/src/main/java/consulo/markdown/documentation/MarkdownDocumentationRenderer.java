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
import consulo.language.Language;
import consulo.language.editor.codeBlock.CodeBlockHighlighter;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor;
import org.intellij.plugins.markdown.html.MarkdownToHtmlConverter;
import org.jspecify.annotations.Nullable;

/**
 * Renders the Markdown of a documentation comment to the HTML of the documentation, with the code highlighted by
 * {@link CodeBlockHighlighter}.
 */
public final class MarkdownDocumentationRenderer {
    private final MarkdownToHtmlConverter myConverter;

    public MarkdownDocumentationRenderer(MarkdownFlavourDescriptor flavour) {
        myConverter = new MarkdownToHtmlConverter(flavour);
    }

    /**
     * @param markdown the Markdown without the line prefixes - it may contain HTML already rendered, as links of the language
     */
    public String toHtml(String markdown) {
        return myConverter.convertMarkdownToHtml(markdown.stripIndent());
    }

    /**
     * Appends a code block, highlighted when its language is known
     */
    @RequiredReadAction
    public static StringBuilder appendCodeBlock(StringBuilder builder, Project project, @Nullable Language language, String code) {
        if (language == null) {
            return builder.append("<pre><code>").append(StringUtil.escapeXmlEntities(code)).append("</code></pre>");
        }
        return builder.append(project.getApplication().getInstance(CodeBlockHighlighter.class)
                                  .newHtmlBuilder(project, language, code)
                                  .trimIndent(true)
                                  .generateWrappedTags()
                                  .generateBackground()
                                  .build());
    }

    /**
     * Appends inline code, highlighted when its language is known
     */
    @RequiredReadAction
    public static StringBuilder appendInlineCode(StringBuilder builder, Project project, @Nullable Language language, String code) {
        builder.append("<code>");
        if (language == null) {
            builder.append(StringUtil.escapeXmlEntities(code));
        }
        else {
            builder.append(project.getApplication().getInstance(CodeBlockHighlighter.class).newHtmlBuilder(project, language, code).build());
        }
        return builder.append("</code>");
    }
}
