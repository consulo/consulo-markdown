package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.language.ast.ASTNode;
import org.jetbrains.annotations.NotNull;

public class MarkdownTableCellImpl extends MarkdownCompositePsiElementBase {
  public MarkdownTableCellImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected String getPresentableTagName() {
    return "td";
  }
}
