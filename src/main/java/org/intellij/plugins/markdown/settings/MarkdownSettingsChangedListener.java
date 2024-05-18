package org.intellij.plugins.markdown.settings;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.TopicAPI;
import org.jetbrains.annotations.NotNull;

@TopicAPI(ComponentScope.APPLICATION)
public interface MarkdownSettingsChangedListener {
  void onSettingsChange(@NotNull MarkdownApplicationSettings settings);
}
