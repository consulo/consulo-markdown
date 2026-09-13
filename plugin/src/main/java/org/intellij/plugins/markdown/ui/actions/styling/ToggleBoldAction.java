package org.intellij.plugins.markdown.ui.actions.styling;

import consulo.annotation.component.ActionImpl;
import consulo.language.ast.IElementType;
import consulo.markdown.icon.MarkdownIconGroup;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;

@ActionImpl(id = "org.intellij.plugins.markdown.ui.actions.styling.ToggleBoldActio")
public class ToggleBoldAction extends BaseToggleStateAction {
  public ToggleBoldAction() {
    super("Toggle bold mode", "Toggles bold mode on caret/selection", MarkdownIconGroup.editor_actionsBold());
  }

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
