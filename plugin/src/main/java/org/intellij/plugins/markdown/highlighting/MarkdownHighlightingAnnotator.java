/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package org.intellij.plugins.markdown.highlighting;

import consulo.colorScheme.TextAttributesKey;
import consulo.language.ast.IElementType;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;

public class MarkdownHighlightingAnnotator implements Annotator {

  private static final SyntaxHighlighter SYNTAX_HIGHLIGHTER = new MarkdownSyntaxHighlighter();

  @Override
  public void annotate(@Nonnull PsiElement element, @Nonnull AnnotationHolder holder) {
    final IElementType type = element.getNode().getElementType();

    if (type == MarkdownTokenTypes.EMPH) {
      final PsiElement parent = element.getParent();
      if (parent == null) {
        return;
      }

      final IElementType parentType = parent.getNode().getElementType();
      if (parentType == MarkdownElementTypes.EMPH || parentType == MarkdownElementTypes.STRONG) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
              .range(element)
              .textAttributes(parentType == MarkdownElementTypes.EMPH
                                ? MarkdownHighlighterColors.ITALIC_MARKER_ATTR_KEY
                                : MarkdownHighlighterColors.BOLD_MARKER_ATTR_KEY)
              .create();
      }
      return;
    }

    final TextAttributesKey[] tokenHighlights = SYNTAX_HIGHLIGHTER.getTokenHighlights(type);

    if (tokenHighlights.length > 0 && !MarkdownHighlighterColors.TEXT_ATTR_KEY.equals(tokenHighlights[0])) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(element)
            .textAttributes(tokenHighlights[0])
            .create();
    }
  }
}
