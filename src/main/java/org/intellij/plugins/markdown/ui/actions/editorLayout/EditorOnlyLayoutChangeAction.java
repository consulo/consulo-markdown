package org.intellij.plugins.markdown.ui.actions.editorLayout;

import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import jakarta.annotation.Nullable;
import org.intellij.plugins.markdown.ui.split.SplitFileEditor;

public class EditorOnlyLayoutChangeAction extends BaseChangeSplitLayoutAction {
  public EditorOnlyLayoutChangeAction() {
    super(SplitFileEditor.SplitEditorLayout.FIRST);
  }

  @Nullable
  @Override
  protected Image getTemplateIcon() {
    return PlatformIconGroup.generalLayouteditoronly();
  }
}
