package org.intellij.plugins.markdown.settings;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.ApplicationManager;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.ide.ServiceManager;
import consulo.ui.style.StyleManager;
import consulo.util.xml.serializer.XmlSerializerUtil;
import consulo.util.xml.serializer.annotation.Property;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@State(name = "MarkdownApplicationSettings", storages = @Storage("markdown.xml"))
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
public class MarkdownApplicationSettings implements PersistentStateComponent<MarkdownApplicationSettings.State>, MarkdownCssSettings.Holder {
  private State myState = new State();

  @Inject
  public MarkdownApplicationSettings() {
    final MarkdownLAFListener listener = new MarkdownLAFListener();
    StyleManager.get().addChangeListener(listener);
    
    // Let's init proper CSS scheme
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        listener.updateCssSettingsForced(StyleManager.get().getCurrentStyle().isDark());
      }
    });
  }

  @Nonnull
  public static MarkdownApplicationSettings getInstance() {
    return ServiceManager.getService(MarkdownApplicationSettings.class);
  }

  @Nullable
  @Override
  public State getState() {
    return myState;
  }

  @Override
  public void loadState(State state) {
    XmlSerializerUtil.copyBean(state, myState);
  }

  @Override
  public void setMarkdownCssSettings(@Nonnull MarkdownCssSettings settings) {
    myState.myCssSettings = settings;

    ApplicationManager.getApplication().getMessageBus().syncPublisher(MarkdownSettingsChangedListener.class).onSettingsChange(this);
  }

  @Nonnull
  @Override
  public MarkdownCssSettings getMarkdownCssSettings() {
    return myState.myCssSettings;
  }

  public static class State {
    @Property(surroundWithTag = false)
    @Nonnull
    private MarkdownCssSettings myCssSettings = MarkdownCssSettings.DEFAULT;
  }

}
