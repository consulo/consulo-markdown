package org.intellij.plugins.markdown.structureView;

import org.jetbrains.annotations.Nullable;
import com.intellij.icons.AllIcons;
import com.intellij.navigation.ItemPresentation;
import consulo.ui.image.Image;

public abstract class MarkdownBasePresentation implements ItemPresentation {

  @Nullable
  @Override
  public Image getIcon() {
    return AllIcons.Nodes.Tag;
  }
}
