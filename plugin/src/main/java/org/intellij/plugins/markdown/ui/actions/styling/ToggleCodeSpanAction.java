package org.intellij.plugins.markdown.ui.actions.styling;

import consulo.annotation.component.ActionImpl;
import consulo.language.ast.IElementType;
import consulo.markdown.icon.MarkdownIconGroup;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;

@ActionImpl(id = "org.intellij.plugins.markdown.ui.actions.styling.ToggleCodeSpanAction")
public class ToggleCodeSpanAction extends BaseToggleStateAction {
  public ToggleCodeSpanAction() {
    super("Toggle monospaced (code span) mode", "Toggles monospaced mode (rendered as code span) on caret/selection", MarkdownIconGroup.editor_actionsCode_span());
  }

  @Nonnull
  @Override
  protected String getBoundString(@Nonnull CharSequence text, int selectionStart, int selectionEnd) {
    int maxBacktickSequenceSeen = 0;
    int curBacktickSequence = 0;
    for (int i = selectionStart; i < selectionEnd; ++i) {
      if (text.charAt(i) != '`') {
        curBacktickSequence = 0;
      }
      else {
        curBacktickSequence++;
        maxBacktickSequenceSeen = Math.max(maxBacktickSequenceSeen, curBacktickSequence);
      }
    }

    return StringUtil.repeat("`", maxBacktickSequenceSeen + 1);
  }

  @Nullable
  @Override
  protected String getExistingBoundString(@Nonnull CharSequence text, int startOffset) {
    int to = startOffset;
    while (to < text.length() && text.charAt(to) == '`') {
      to++;
    }

    return text.subSequence(startOffset, to).toString();
  }

  @Override
  protected boolean shouldMoveToWordBounds() {
    return false;
  }

  @Nonnull
  @Override
  protected IElementType getTargetNodeType() {
    return MarkdownElementTypes.CODE_SPAN;
  }
}
