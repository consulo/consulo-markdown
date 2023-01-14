package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.language.ast.IElementType;
import consulo.language.impl.psi.LeafPsiElement;
import org.jetbrains.annotations.NotNull;

public class MarkdownCodeFenceContentImpl extends LeafPsiElement {
  public MarkdownCodeFenceContentImpl(@NotNull IElementType type, CharSequence text) {
    super(type, text);
  }
}
