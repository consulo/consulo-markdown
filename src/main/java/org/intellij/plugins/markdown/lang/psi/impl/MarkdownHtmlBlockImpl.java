package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.navigation.ItemPresentation;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElement;
import org.intellij.plugins.markdown.structureView.MarkdownBasePresentation;

import java.util.Collections;
import java.util.List;

public class MarkdownHtmlBlockImpl extends ASTWrapperPsiElement implements MarkdownPsiElement {

  public MarkdownHtmlBlockImpl(@Nonnull ASTNode node) {
    super(node);
  }

  @Override
  public ItemPresentation getPresentation() {
    return new MarkdownBasePresentation() {
      @Nullable
      @Override
      public String getPresentableText() {
        return "HTML block";
      }

      @Nullable
      @Override
      public String getLocationString() {
        return null;
      }
    };
  }

  @Nonnull
  @Override
  public List<MarkdownPsiElement> getCompositeChildren() {
    return Collections.emptyList();
  }
}
