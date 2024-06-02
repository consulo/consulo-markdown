package org.intellij.plugins.markdown.ui.actions.editorLayout;

import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import jakarta.annotation.Nullable;

public class CyclicSplitLayoutChangeAction extends BaseChangeSplitLayoutAction {
  public CyclicSplitLayoutChangeAction() {
    super(null);
  }

  @Nullable
  @Override
  protected Image getTemplateIcon() {
    return PlatformIconGroup.actionsPreviewdetails();
  }
}
