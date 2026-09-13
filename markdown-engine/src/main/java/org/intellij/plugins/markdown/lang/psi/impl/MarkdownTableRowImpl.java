package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.language.ast.ASTNode;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import jakarta.annotation.Nonnull;

public class MarkdownTableRowImpl extends MarkdownCompositePsiElementBase {
  public MarkdownTableRowImpl(@Nonnull ASTNode node) {
    super(node);
  }

  @Override
  protected String getPresentableTagName() {
    if (getNode().getElementType() == MarkdownElementTypes.TABLE_HEADER) {
      return "th";
    }
    else {
      return "tr";
    }
  }
}
