package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.language.ast.ASTNode;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.jetbrains.annotations.NotNull;

public class MarkdownListImpl extends MarkdownCompositePsiElementBase {
  public MarkdownListImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected String getPresentableTagName() {
    return getNode().getElementType() == MarkdownElementTypes.ORDERED_LIST
           ? "ol"
           : "ul";
  }
}
