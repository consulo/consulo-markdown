package org.intellij.plugins.markdown.settings;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.ApplicationConfigurable;
import consulo.configurable.ConfigurationException;
import consulo.configurable.SearchableConfigurable;
import consulo.configurable.StandardConfigurableIds;
import consulo.disposer.Disposer;
import consulo.localize.LocalizeValue;
import consulo.markdown.localize.MarkdownLocalize;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;

import javax.swing.*;

@ExtensionImpl
public class MarkdownCssConfigurable implements SearchableConfigurable, ApplicationConfigurable {
  public static final String ID = "Settings.Markdown.Css";

  @Nullable
  private MarkdownCssSettingsForm myForm = null;
  @Nonnull
  private MarkdownApplicationSettings myMarkdownApplicationSettings;

  @Inject
  public MarkdownCssConfigurable(@Nonnull MarkdownApplicationSettings markdownApplicationSettings) {
    myMarkdownApplicationSettings = markdownApplicationSettings;
  }

  @Nonnull
  @Override
  public String getId() {
    return "Settings.Markdown.Css";
  }

  @Nullable
  @Override
  public String getParentId() {
    return StandardConfigurableIds.EDITOR_GROUP;
  }

  @Nullable
  @Override
  public Runnable enableSearch(String option) {
    return null;
  }

  @Nonnull
  @Override
  public LocalizeValue getDisplayName() {
    return MarkdownLocalize.settingsMarkdownCssName();
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }

  @Nonnull
  @Override
  public JComponent createComponent() {
    return getForm().getComponent();
  }

  @Nonnull
  public MarkdownCssSettingsForm getForm() {
    if (myForm == null) {
      myForm = new MarkdownCssSettingsForm();
    }
    return myForm;
  }

  @Override
  public boolean isModified() {
    return !getForm().getMarkdownCssSettings().equals(myMarkdownApplicationSettings.getMarkdownCssSettings());
  }

  @Override
  public void apply() throws ConfigurationException {
    myMarkdownApplicationSettings.setMarkdownCssSettings(getForm().getMarkdownCssSettings());
  }

  @Override
  public void reset() {
    getForm().setMarkdownCssSettings(myMarkdownApplicationSettings.getMarkdownCssSettings());
  }

  @Override
  public void disposeUIResources() {
    if (myForm != null) {
      Disposer.dispose(myForm);
    }
    myForm = null;
  }
}
