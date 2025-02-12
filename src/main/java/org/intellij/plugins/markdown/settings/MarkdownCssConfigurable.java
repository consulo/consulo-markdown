package org.intellij.plugins.markdown.settings;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.ApplicationConfigurable;
import consulo.configurable.ConfigurationException;
import consulo.configurable.SearchableConfigurable;
import consulo.configurable.StandardConfigurableIds;
import consulo.disposer.Disposer;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import org.intellij.plugins.markdown.MarkdownBundle;
import org.jetbrains.annotations.Nls;
import jakarta.annotation.Nullable;

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

  @Nls
  @Override
  public String getDisplayName() {
    return MarkdownBundle.message("settings.markdown.css.name");
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
