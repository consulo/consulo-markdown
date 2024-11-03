package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.psi.AbstractElementManipulator;
import consulo.language.util.IncorrectOperationException;
import jakarta.annotation.Nonnull;

@ExtensionImpl
public class MarkdownCodeFenceManipulator extends AbstractElementManipulator<MarkdownCodeFenceImpl> {

  @Override
  public MarkdownCodeFenceImpl handleContentChange(@Nonnull MarkdownCodeFenceImpl element, @Nonnull TextRange range, String newContent)
    throws IncorrectOperationException {
    return null;
  }

  @Nonnull
  @Override
  public Class<MarkdownCodeFenceImpl> getElementClass() {
    return MarkdownCodeFenceImpl.class;
  }
}
