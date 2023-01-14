package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.language.ast.ASTNode;
import org.jetbrains.annotations.NotNull;

public class MarkdownParagraphImpl extends MarkdownCompositePsiElementBase {

  public MarkdownParagraphImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected String getPresentableTagName() {
    return "p";
  }
}
