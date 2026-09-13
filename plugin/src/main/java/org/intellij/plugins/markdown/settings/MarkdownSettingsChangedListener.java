package org.intellij.plugins.markdown.settings;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.TopicAPI;
import jakarta.annotation.Nonnull;

@TopicAPI(ComponentScope.APPLICATION)
public interface MarkdownSettingsChangedListener {
  void onSettingsChange(@Nonnull MarkdownApplicationSettings settings);
}
