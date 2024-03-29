package org.intellij.plugins.markdown.ui.actions.styling;

import consulo.language.ast.IElementType;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.jetbrains.annotations.NotNull;

public class ToggleItalicAction extends BaseToggleStateAction {
  @NotNull
  protected String getBoundString(@NotNull CharSequence text, int selectionStart, int selectionEnd) {
    return isWord(text, selectionStart, selectionEnd) ? "_" : "*";
  }

  protected boolean shouldMoveToWordBounds() {
    return true;
  }

  @NotNull
  protected IElementType getTargetNodeType() {
    return MarkdownElementTypes.EMPH;
  }

  private static boolean isWord(@NotNull CharSequence text, int from, int to) {
    return (from == 0 || !Character.isLetterOrDigit(text.charAt(from - 1)))
           && (to == text.length() || !Character.isLetterOrDigit(text.charAt(to)));
  }
}
