package org.intellij.plugins.markdown.settings;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.ApplicationConfigurable;
import consulo.configurable.ConfigurationException;
import consulo.configurable.SearchableConfigurable;
import jakarta.inject.Inject;
import org.intellij.plugins.markdown.MarkdownBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

@ExtensionImpl
public class MarkdownPreviewConfigurable implements SearchableConfigurable, ApplicationConfigurable {
  @Nullable
  private MarkdownPreviewSettingsForm myForm = null;
  @NotNull
  private MarkdownApplicationSettings myMarkdownApplicationSettings;

  @Inject
  public MarkdownPreviewConfigurable(@NotNull MarkdownApplicationSettings markdownApplicationSettings) {
    myMarkdownApplicationSettings = markdownApplicationSettings;
  }

  @NotNull
  @Override
  public String getId() {
    return "Settings.Markdown.Preview";
  }

  @javax.annotation.Nullable
  @Override
  public String getParentId() {
    return MarkdownCssConfigurable.ID;
  }

  @Nls
  @Override
  public String getDisplayName() {
    return MarkdownBundle.message("settings.markdown.preview.name");
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    return getForm().getComponent();
  }

  @Override
  public boolean isModified() {
    return !getForm().getMarkdownPreviewSettings().equals(myMarkdownApplicationSettings.getMarkdownPreviewSettings());
  }

  @Override
  public void apply() throws ConfigurationException {
    myMarkdownApplicationSettings.setMarkdownPreviewSettings(getForm().getMarkdownPreviewSettings());
  }

  @Override
  public void reset() {
    getForm().setMarkdownPreviewSettings(myMarkdownApplicationSettings.getMarkdownPreviewSettings());
  }

  @Override
  public void disposeUIResources() {
    myForm = null;
  }

  @NotNull
  public MarkdownPreviewSettingsForm getForm() {
    if (myForm == null) {
      myForm = new MarkdownPreviewSettingsForm();
    }
    return myForm;
  }
}
