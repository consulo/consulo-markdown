package org.intellij.plugins.markdown.ui.actions.styling;

import consulo.annotation.component.ActionImpl;
import consulo.language.ast.IElementType;
import consulo.markdown.icon.MarkdownIconGroup;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;

@ActionImpl(id = "org.intellij.plugins.markdown.ui.actions.styling.ToggleItalicAction")
public class ToggleItalicAction extends BaseToggleStateAction {
  public ToggleItalicAction() {
    super("Toggle italic mode", "Toggles italic mode on caret/selection", MarkdownIconGroup.editor_actionsItalic());
  }

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
