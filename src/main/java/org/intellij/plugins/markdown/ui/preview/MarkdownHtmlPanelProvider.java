package org.intellij.plugins.markdown.ui.preview;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.application.CommonBundle;
import consulo.component.extension.ExtensionPointName;
import consulo.logging.Logger;
import consulo.ui.ex.awt.Messages;
import consulo.util.xml.serializer.annotation.Attribute;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

@ExtensionAPI(ComponentScope.APPLICATION)
public abstract class MarkdownHtmlPanelProvider {
  public static final ExtensionPointName<MarkdownHtmlPanelProvider> EP_NAME = ExtensionPointName.create(MarkdownHtmlPanelProvider.class);

  private static MarkdownHtmlPanelProvider[] ourProviders = null;

  @NotNull
  public abstract MarkdownHtmlPanel createHtmlPanel();

  @NotNull
  public abstract AvailabilityInfo isAvailable();

  @NotNull
  public abstract ProviderInfo getProviderInfo();

  @NotNull
  public static MarkdownHtmlPanelProvider[] getProviders() {
    if (ourProviders == null) {
      ourProviders = EP_NAME.getExtensions();
    }
    return ourProviders;
  }

  @NotNull
  public static MarkdownHtmlPanelProvider createFromInfo(@NotNull ProviderInfo providerInfo) {
    try {
      return ((MarkdownHtmlPanelProvider)Class.forName(providerInfo.getClassName()).newInstance());
    }
    catch (Exception e) {
      Messages.showMessageDialog("Cannot set preview panel provider (" + providerInfo.getName() + "):\n" + e.getMessage(),
                                 CommonBundle.getErrorTitle(),
                                 Messages.getErrorIcon());
      Logger.getInstance(MarkdownHtmlPanelProvider.class).error(e);
      return getProviders()[0];
    }
  }

  public static class ProviderInfo {
    @NotNull
    @Attribute("name")
    private String myName;
    @NotNull
    @Attribute("className")
    private String className;

    @SuppressWarnings("unused")
    private ProviderInfo() {
      myName = "";
      className = "";
    }

    public ProviderInfo(@NotNull String name, @NotNull String className) {
      myName = name;
      this.className = className;
    }

    @NotNull
    public String getName() {
      return myName;
    }

    @NotNull
    public String getClassName() {
      return className;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      ProviderInfo info = (ProviderInfo)o;

      if (!myName.equals(info.myName)) {
        return false;
      }
      if (!className.equals(info.className)) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = myName.hashCode();
      result = 31 * result + className.hashCode();
      return result;
    }

    @Override
    public String toString() {
      return myName;
    }
  }

  public static abstract class AvailabilityInfo {
    public static final AvailabilityInfo AVAILABLE = new AvailabilityInfo() {
      @Override
      public boolean checkAvailability(@NotNull JComponent parentComponent) {
        return true;
      }
    };

    public static final AvailabilityInfo UNAVAILABLE = new AvailabilityInfo() {
      @Override
      public boolean checkAvailability(@NotNull JComponent parentComponent) {
        return false;
      }
    };

    public abstract boolean checkAvailability(@NotNull JComponent parentComponent);
  }
}
