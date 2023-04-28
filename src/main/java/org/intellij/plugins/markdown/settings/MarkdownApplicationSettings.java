package org.intellij.plugins.markdown.settings;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.ApplicationManager;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.ide.ServiceManager;
import consulo.ide.impl.idea.ide.ui.LafManager;
import consulo.util.xml.serializer.XmlSerializerUtil;
import consulo.util.xml.serializer.annotation.Property;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
@State(name = "MarkdownApplicationSettings", storages = @Storage("markdown.xml"))
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
public class MarkdownApplicationSettings implements PersistentStateComponent<MarkdownApplicationSettings.State>, MarkdownCssSettings.Holder, MarkdownPreviewSettings.Holder {
  private State myState = new State();

  @Inject
  public MarkdownApplicationSettings() {
    final MarkdownLAFListener lafListener = new MarkdownLAFListener();
    LafManager.getInstance().addLafManagerListener(lafListener);
    // Let's init proper CSS scheme
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        lafListener.updateCssSettingsForced(MarkdownLAFListener.isDarcula(LafManager.getInstance().getCurrentLookAndFeel()));
      }
    });
  }

  @NotNull
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
  public void setMarkdownCssSettings(@NotNull MarkdownCssSettings settings) {
    myState.myCssSettings = settings;

    ApplicationManager.getApplication().getMessageBus().syncPublisher(MarkdownSettingsChangedListener.class).onSettingsChange(this);
  }

  @NotNull
  @Override
  public MarkdownCssSettings getMarkdownCssSettings() {
    return myState.myCssSettings;
  }

  @Override
  public void setMarkdownPreviewSettings(@NotNull MarkdownPreviewSettings settings) {
    myState.myPreviewSettings = settings;

    ApplicationManager.getApplication().getMessageBus().syncPublisher(MarkdownSettingsChangedListener.class).onSettingsChange(this);
  }

  @NotNull
  @Override
  public MarkdownPreviewSettings getMarkdownPreviewSettings() {
    return myState.myPreviewSettings;
  }


  public static class State {
    @Property(surroundWithTag = false)
    @NotNull
    private MarkdownCssSettings myCssSettings = MarkdownCssSettings.DEFAULT;

    @Property(surroundWithTag = false)
    @NotNull
    private MarkdownPreviewSettings myPreviewSettings = MarkdownPreviewSettings.DEFAULT;
  }

}
