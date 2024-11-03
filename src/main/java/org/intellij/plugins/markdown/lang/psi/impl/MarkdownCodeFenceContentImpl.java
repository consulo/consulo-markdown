package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.language.ast.IElementType;
import consulo.language.impl.psi.LeafPsiElement;
import jakarta.annotation.Nonnull;

public class MarkdownCodeFenceContentImpl extends LeafPsiElement {
  public MarkdownCodeFenceContentImpl(@Nonnull IElementType type, CharSequence text) {
    super(type, text);
  }
}
