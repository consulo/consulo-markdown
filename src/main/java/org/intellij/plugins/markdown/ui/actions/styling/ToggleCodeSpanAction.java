package org.intellij.plugins.markdown.ui.actions.styling;

import consulo.language.ast.IElementType;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import jakarta.annotation.Nullable;

public class ToggleCodeSpanAction extends BaseToggleStateAction {
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
