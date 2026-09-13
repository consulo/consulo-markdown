package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.language.ast.ASTNode;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;

public class MarkdownListImpl extends MarkdownCompositePsiElementBase {
  public MarkdownListImpl(@Nonnull ASTNode node) {
    super(node);
  }

  @Override
  protected String getPresentableTagName() {
    return getNode().getElementType() == MarkdownElementTypes.ORDERED_LIST
           ? "ol"
           : "ul";
  }
}
