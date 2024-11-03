package org.intellij.plugins.markdown.ui.actions.styling;

import consulo.language.ast.IElementType;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;

public class ToggleItalicAction extends BaseToggleStateAction {
  @Nonnull
  protected String getBoundString(@Nonnull CharSequence text, int selectionStart, int selectionEnd) {
    return isWord(text, selectionStart, selectionEnd) ? "_" : "*";
  }

  protected boolean shouldMoveToWordBounds() {
    return true;
  }

  @Nonnull
  protected IElementType getTargetNodeType() {
    return MarkdownElementTypes.EMPH;
  }

  private static boolean isWord(@Nonnull CharSequence text, int from, int to) {
    return (from == 0 || !Character.isLetterOrDigit(text.charAt(from - 1)))
           && (to == text.length() || !Character.isLetterOrDigit(text.charAt(to)));
  }
}
