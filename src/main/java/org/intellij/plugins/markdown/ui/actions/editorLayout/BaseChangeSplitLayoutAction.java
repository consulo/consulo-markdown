package org.intellij.plugins.markdown.ui.actions.editorLayout;

import consulo.application.dumb.DumbAware;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.Toggleable;
import org.intellij.plugins.markdown.ui.actions.MarkdownActionUtil;
import org.intellij.plugins.markdown.ui.split.SplitFileEditor;
import org.jetbrains.annotations.Nullable;

abstract class BaseChangeSplitLayoutAction extends AnAction implements DumbAware, Toggleable {
  @Nullable
  private final SplitFileEditor.SplitEditorLayout myLayoutToSet;

  protected BaseChangeSplitLayoutAction(@Nullable SplitFileEditor.SplitEditorLayout layoutToSet) {
    myLayoutToSet = layoutToSet;
  }

  @Override
  public void update(AnActionEvent e) {
    final SplitFileEditor splitFileEditor = MarkdownActionUtil.findSplitEditor(e);
    e.getPresentation().setEnabled(splitFileEditor != null);

    if (myLayoutToSet != null && splitFileEditor != null) {
      e.getPresentation().putClientProperty(SELECTED_PROPERTY, splitFileEditor.getCurrentEditorLayout() == myLayoutToSet);
    }
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    final SplitFileEditor splitFileEditor = MarkdownActionUtil.findSplitEditor(e);

    if (splitFileEditor != null) {
      if (myLayoutToSet == null) {
        splitFileEditor.triggerLayoutChange();
      }
      else {
        splitFileEditor.triggerLayoutChange(myLayoutToSet);
        e.getPresentation().putClientProperty(SELECTED_PROPERTY, true);
      }
    }
  }
}
