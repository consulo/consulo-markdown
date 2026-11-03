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
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.Language;
import consulo.language.editor.codeBlock.CodeBlockHighlighter;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor;
import org.intellij.markdown.html.CodeFenceGeneratingProvider;
import org.intellij.markdown.html.GeneratingProvider;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.markdown.parser.LinkMap;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.plugins.markdown.html.TreeBuildingSink;
import org.intellij.plugins.markdown.injection.aliases.CodeFenceLanguageGuesser;
import org.intellij.plugins.markdown.html.MarkdownToHtmlConverter;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Renders the Markdown of a documentation comment to the HTML of the documentation, with the code highlighted by
 * {@link CodeBlockHighlighter}.
 */
public final class MarkdownDocumentationRenderer {
    private final MarkdownFlavourDescriptor myFlavour;
    private final MarkdownToHtmlConverter myConverter;

    public MarkdownDocumentationRenderer(MarkdownFlavourDescriptor flavour) {
        myFlavour = flavour;
        myConverter = new MarkdownToHtmlConverter(flavour);
    }

    /**
     * @param markdown the Markdown without the line prefixes - it may contain HTML already rendered, as links of the language
     */
    public String toHtml(String markdown) {
        return myConverter.convertMarkdownToHtml(markdown.stripIndent());
    }

    /**
     * Renders like {@link #toHtml(String)}, and highlights the code of the fences with the language the fence names - for a language
     * whose documentation has no PSI of its code fences.
     */
    @RequiredReadAction
    public String toHtml(String markdown, Project project) {
        String text = markdown.stripIndent();
        MarkdownParser parser = new MarkdownParser(myFlavour);
        TreeBuildingSink sink = new TreeBuildingSink(text);
        parser.walkBlocks(parser.parseBlocks(MarkdownElementTypes.MARKDOWN_FILE, text), text, true, sink);
        ASTNode root = sink.getRoot();

        Map<IElementType, GeneratingProvider> providers = new HashMap<>(myFlavour.createHtmlGeneratingProviders(LinkMap.buildLinkMap(root, text), null));
        providers.put(MarkdownElementTypes.CODE_FENCE, new CodeFenceGeneratingProvider((infoString, code) -> {
            Language language = CodeFenceLanguageGuesser.guessLanguageForInjection(infoString);
            if (language == null) {
                return null;
            }
            return project.getApplication().getInstance(CodeBlockHighlighter.class).newHtmlBuilder(project, language, code).build();
        }));
        return new HtmlGenerator(text, root, providers, false).generateHtml();
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
