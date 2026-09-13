package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.language.ast.ASTNode;
import jakarta.annotation.Nonnull;

public class MarkdownParagraphImpl extends MarkdownCompositePsiElementBase {

  public MarkdownParagraphImpl(@Nonnull ASTNode node) {
    super(node);
  }

  @Override
  protected String getPresentableTagName() {
    return "p";
  }
}
