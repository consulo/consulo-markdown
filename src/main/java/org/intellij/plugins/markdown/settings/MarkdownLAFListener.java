package org.intellij.plugins.markdown.settings;

import consulo.ui.style.Style;
import consulo.ui.style.StyleChangeListener;
import jakarta.annotation.Nonnull;

class MarkdownLAFListener implements StyleChangeListener {
  public void updateCssSettingsForced() {
    final MarkdownCssSettings currentCssSettings = MarkdownApplicationSettings.getInstance().getMarkdownCssSettings();
    MarkdownApplicationSettings.getInstance().setMarkdownCssSettings(new MarkdownCssSettings(
      currentCssSettings.isUriEnabled(),
      MarkdownCssSettings.getDefaultCssSettings().getStylesheetUri(),
      currentCssSettings.isTextEnabled(),
      currentCssSettings.getStylesheetText()
    ));
  }

  @Override
  public void styleChanged(@Nonnull Style oldStyle, @Nonnull Style newStyle) {
    if (oldStyle.isDark() == newStyle.isDark()) {
      return;
    }

    updateCssSettingsForced();
  }
}