package org.intellij.plugins.markdown.ui.actions.styling;

import consulo.language.ast.IElementType;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;

public class ToggleBoldAction extends BaseToggleStateAction {
  @Nonnull
  @Override
  protected String getBoundString(@Nonnull CharSequence text, int selectionStart, int selectionEnd) {
    return "**";
  }

  @Nullable
  @Override
  protected String getExistingBoundString(@Nonnull CharSequence text, int startOffset) {
    return text.subSequence(startOffset, startOffset + 2).toString();
  }

  @Override
  protected boolean shouldMoveToWordBounds() {
    return true;
  }

  @Nonnull
  @Override
  protected IElementType getTargetNodeType() {
    return MarkdownElementTypes.STRONG;
  }
}
