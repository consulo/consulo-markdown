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
import consulo.colorScheme.TextAttributesKey;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.language.psi.PsiElement;
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.plugins.markdown.highlighting.MarkdownHighlighterColors;
import org.intellij.plugins.markdown.highlighting.MarkdownSyntaxHighlighter;
import org.intellij.plugins.markdown.html.TreeBuildingSink;

/**
 * Highlights the Markdown of a documentation comment in the editor as the Markdown language is highlighted - the attributes come
 * from {@link MarkdownSyntaxHighlighter}.
 * <p>
 * The code of the fences is left to the language injected into them.
 */
public final class MarkdownDocumentationHighlighter {
    private static final SyntaxHighlighter MARKDOWN_HIGHLIGHTER = new MarkdownSyntaxHighlighter();

    private MarkdownDocumentationHighlighter() {
    }

    /**
     * Highlights the comment if a {@link MarkdownDocumentationProvider} describes it as Markdown
     */
    @RequiredReadAction
    public static void highlight(PsiElement comment, AnnotationHolder holder) {
        MarkdownDocumentationProvider provider = MarkdownDocumentationProvider.findByComment(comment);
        if (provider != null) {
            highlight(comment, provider, holder);
        }
    }

    @RequiredReadAction
    public static void highlight(PsiElement comment, MarkdownDocumentationProvider provider, AnnotationHolder holder) {
        MarkdownDocumentationText markdown = MarkdownDocumentationText.create(comment.getText(), provider.getLinePrefixes());
        String text = markdown.getText();

        MarkdownParser parser = new MarkdownParser(provider.getFlavour());
        TreeBuildingSink sink = new TreeBuildingSink(text);
        parser.walkBlocks(parser.parseBlocks(MarkdownElementTypes.MARKDOWN_FILE, text), text, true, sink);

        visit(sink.getRoot(), markdown, comment.getTextRange().getStartOffset(), holder);
    }

    private static void visit(ASTNode node, MarkdownDocumentationText markdown, int commentStart, AnnotationHolder holder) {
        IElementType type = node.getElementType();
        if (type == MarkdownElementTypes.CODE_FENCE) {
            // the code of a fence is highlighted by its injected language
            return;
        }

        TextAttributesKey[] tokenHighlights = MARKDOWN_HIGHLIGHTER.getTokenHighlights(type);
        if (tokenHighlights.length > 0 && !MarkdownHighlighterColors.TEXT_ATTR_KEY.equals(tokenHighlights[0])) {
            for (TextRange range : markdown.toCommentRanges(node.getTextRange())) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(range.shiftRight(commentStart))
                    .textAttributes(tokenHighlights[0])
                    .create();
            }
        }

        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            visit(child, markdown, commentStart, holder);
        }
    }
}
