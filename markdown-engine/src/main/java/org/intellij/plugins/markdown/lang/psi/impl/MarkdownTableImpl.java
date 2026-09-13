package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.language.ast.ASTNode;
import jakarta.annotation.Nonnull;

public class MarkdownTableImpl extends MarkdownCompositePsiElementBase {
  public MarkdownTableImpl(@Nonnull ASTNode node) {
    super(node);
  }

  @Override
  protected String getPresentableTagName() {
    return "table";
  }
}
