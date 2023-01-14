package org.intellij.plugins.markdown.structureView;

import consulo.application.AllIcons;
import consulo.navigation.ItemPresentation;
import consulo.ui.image.Image;

import javax.annotation.Nullable;

public abstract class MarkdownBasePresentation implements ItemPresentation {

  @Nullable
  @Override
  public Image getIcon() {
    return AllIcons.Nodes.Tag;
  }
}
