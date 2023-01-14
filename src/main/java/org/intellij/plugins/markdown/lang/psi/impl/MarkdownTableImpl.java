package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.language.ast.ASTNode;
import org.jetbrains.annotations.NotNull;

public class MarkdownTableImpl extends MarkdownCompositePsiElementBase {
  public MarkdownTableImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected String getPresentableTagName() {
    return "table";
  }
}
