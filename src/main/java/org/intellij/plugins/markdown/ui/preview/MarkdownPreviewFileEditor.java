package org.intellij.plugins.markdown.ui.preview;

import consulo.application.Application;
import consulo.application.ApplicationManager;
import consulo.application.CommonBundle;
import consulo.component.messagebus.MessageBusConnection;
import consulo.disposer.Disposer;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.document.event.DocumentAdapter;
import consulo.document.event.DocumentEvent;
import consulo.fileEditor.FileEditor;
import consulo.fileEditor.FileEditorLocation;
import consulo.fileEditor.FileEditorState;
import consulo.fileEditor.FileEditorStateLevel;
import consulo.fileEditor.highlight.BackgroundEditorHighlighter;
import consulo.fileEditor.structureView.StructureViewBuilder;
import consulo.ui.ex.awt.JBUI;
import consulo.ui.ex.awt.Messages;
import consulo.ui.ex.awt.util.Alarm;
import consulo.util.dataholder.UserDataHolderBase;
import consulo.virtualFileSystem.VirtualFile;
import kava.beans.PropertyChangeListener;
import org.intellij.markdown.IElementType;
import org.intellij.markdown.ast.ASTNode;
import org.intellij.markdown.html.GeneratingProvider;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.markdown.parser.LinkMap;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.plugins.markdown.lang.parser.MarkdownParserManager;
import org.intellij.plugins.markdown.settings.MarkdownApplicationSettings;
import org.intellij.plugins.markdown.settings.MarkdownCssSettings;
import org.intellij.plugins.markdown.settings.MarkdownPreviewSettings;
import org.intellij.plugins.markdown.settings.MarkdownSettingsChangedListener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.Map;

public class MarkdownPreviewFileEditor extends UserDataHolderBase implements FileEditor {
  private final static long PARSING_CALL_TIMEOUT_MS = 50L;

  private final static long RENDERING_DELAY_MS = 20L;
  @NotNull
  private final JPanel myHtmlPanelWrapper;
  @NotNull
  private MarkdownHtmlPanel myPanel;
  @Nullable
  private MarkdownHtmlPanelProvider.ProviderInfo myLastPanelProviderInfo = null;
  @NotNull
  private final VirtualFile myFile;
  @Nullable
  private final Document myDocument;
  @NotNull
  private final Alarm myPooledAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);
  @NotNull
  private final Alarm mySwingAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, this);

  private final Object REQUESTS_LOCK = new Object();
  @Nullable
  private Runnable myLastScrollRequest = null;
  @Nullable
  private Runnable myLastHtmlOrRefreshRequest = null;

  private volatile int myLastScrollOffset;
  @NotNull
  private String myLastRenderedHtml = "";

  public MarkdownPreviewFileEditor(@NotNull VirtualFile file) {
    myFile = file;
    myDocument = FileDocumentManager.getInstance().getDocument(myFile);

    if (myDocument != null) {
      myDocument.addDocumentListener(new DocumentAdapter() {

        @Override
        public void beforeDocumentChange(DocumentEvent e) {
          myPooledAlarm.cancelAllRequests();
        }

        @Override
        public void documentChanged(final DocumentEvent e) {
          myPooledAlarm.addRequest(new Runnable() {
            @Override
            public void run() {
              //myLastScrollOffset = e.getOffset();
              updateHtml(true);
            }
          }, PARSING_CALL_TIMEOUT_MS);
        }
      }, this);
    }

    myHtmlPanelWrapper = new JPanel(new BorderLayout());

    final MarkdownApplicationSettings settings = MarkdownApplicationSettings.getInstance();
    myPanel = detachOldPanelAndCreateAndAttachNewOne(myHtmlPanelWrapper, null, retrievePanelProvider(settings));
    updatePanelCssSettings(myPanel, settings.getMarkdownCssSettings());

    MessageBusConnection settingsConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
    MarkdownSettingsChangedListener settingsChangedListener = new MyUpdatePanelOnSettingsChangedListener();
    settingsConnection.subscribe(MarkdownSettingsChangedListener.class, settingsChangedListener);
  }

  public void scrollToSrcOffset(final int offset) {
    // Do not scroll if html update request is online
    // This will restrain preview from glitches on editing
    if (!myPooledAlarm.isEmpty()) {
      myLastScrollOffset = offset;
      return;
    }

    synchronized (REQUESTS_LOCK) {
      if (myLastScrollRequest != null) {
        mySwingAlarm.cancelRequest(myLastScrollRequest);
      }
      myLastScrollRequest = new Runnable() {
        @Override
        public void run() {
          myLastScrollOffset = offset;
          myPanel.scrollToMarkdownSrcOffset(myLastScrollOffset);
          synchronized (REQUESTS_LOCK) {
            myLastScrollRequest = null;
          }
        }
      };
      mySwingAlarm.addRequest(myLastScrollRequest, RENDERING_DELAY_MS, Application.get().getModalityStateForComponent(getComponent()));
    }
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return myHtmlPanelWrapper;
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return myPanel.getComponent();
  }

  @NotNull
  @Override
  public String getName() {
    return "Markdown HTML Preview";
  }

  @NotNull
  @Override
  public FileEditorState getState(@NotNull FileEditorStateLevel fileEditorStateLevel) {
    return FileEditorState.INSTANCE;
  }

  @Override
  public void setState(@NotNull FileEditorState state) {
  }

  @Override
  public boolean isModified() {
    return false;
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public void selectNotify() {
    myPooledAlarm.cancelAllRequests();
    myPooledAlarm.addRequest(new Runnable() {
      @Override
      public void run() {
        MarkdownPreviewFileEditor.this.updateHtml(true);
      }
    }, 0);
  }

  @Nullable("Null means leave current panel")
  private MarkdownHtmlPanelProvider retrievePanelProvider(@NotNull MarkdownApplicationSettings settings) {
    final MarkdownHtmlPanelProvider.ProviderInfo providerInfo = settings.getMarkdownPreviewSettings().getHtmlPanelProviderInfo();
    if (providerInfo.equals(myLastPanelProviderInfo)) {
      return null;
    }

    MarkdownHtmlPanelProvider provider = MarkdownHtmlPanelProvider.createFromInfo(providerInfo);

    if (provider.isAvailable() != MarkdownHtmlPanelProvider.AvailabilityInfo.AVAILABLE) {
      settings.setMarkdownPreviewSettings(new MarkdownPreviewSettings(settings.getMarkdownPreviewSettings().getSplitEditorLayout(),
                                                                      MarkdownPreviewSettings.DEFAULT.getHtmlPanelProviderInfo()
      ));

      Messages.showMessageDialog(myHtmlPanelWrapper,
                                 "Tried to use preview panel provider (" + providerInfo.getName() + "), but it is unavailable. Reverting to default.",
                                 CommonBundle.getErrorTitle(),
                                 Messages.getErrorIcon());

      provider = MarkdownHtmlPanelProvider.getProviders()[0];
    }

    myLastPanelProviderInfo = settings.getMarkdownPreviewSettings().getHtmlPanelProviderInfo();
    return provider;
  }

  /**
   * Is always run from pooled thread
   */
  private void updateHtml(final boolean preserveScrollOffset) {
    if (!myFile.isValid() || myDocument == null || Disposer.isDisposed(this)) {
      return;
    }

    final String html = generateMarkdownHtml(myFile, myDocument.getText());

    // EA-75860: The lines to the top may be processed slowly; Since we're in pooled thread, we can be disposed already.
    if (!myFile.isValid() || Disposer.isDisposed(this)) {
      return;
    }

    synchronized (REQUESTS_LOCK) {
      if (myLastHtmlOrRefreshRequest != null) {
        mySwingAlarm.cancelRequest(myLastHtmlOrRefreshRequest);
      }
      myLastHtmlOrRefreshRequest = new Runnable() {
        @Override
        public void run() {
          final String currentHtml = "<html><head></head>" + html + "</html>";
          if (!currentHtml.equals(myLastRenderedHtml)) {
            myLastRenderedHtml = currentHtml;
            myPanel.setHtml(myLastRenderedHtml);

            if (preserveScrollOffset) {
              myPanel.scrollToMarkdownSrcOffset(myLastScrollOffset);
            }
          }

          myPanel.render();
          synchronized (REQUESTS_LOCK) {
            myLastHtmlOrRefreshRequest = null;
          }
        }
      };
      mySwingAlarm.addRequest(myLastHtmlOrRefreshRequest, RENDERING_DELAY_MS, Application.get().getModalityStateForComponent(getComponent()));
    }
  }

  @Override
  public void deselectNotify() {
  }

  @Override
  public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
  }

  @Override
  public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
  }

  @Nullable
  @Override
  public BackgroundEditorHighlighter getBackgroundHighlighter() {
    return null;
  }

  @Nullable
  @Override
  public FileEditorLocation getCurrentLocation() {
    return null;
  }

  @Nullable
  @Override
  public StructureViewBuilder getStructureViewBuilder() {
    return null;
  }

  @NotNull
  @Override
  public VirtualFile getFile() {
    return myFile;
  }

  @Override
  public void dispose() {
    Disposer.dispose(myPanel);
  }

  @NotNull
  private static String generateMarkdownHtml(@NotNull VirtualFile file, @NotNull String text) {
    final VirtualFile parent = file.getParent();
    final URI baseUri = parent != null ? new File(parent.getPath()).toURI() : null;

    final ASTNode parsedTree = new MarkdownParser(MarkdownParserManager.FLAVOUR).buildMarkdownTreeFromString(text);
    final Map<IElementType, GeneratingProvider> htmlGeneratingProviders =
      MarkdownParserManager.FLAVOUR.createHtmlGeneratingProviders(LinkMap.Builder.buildLinkMap(parsedTree, text), baseUri);

    return new HtmlGenerator(text, parsedTree, htmlGeneratingProviders, true).generateHtml();
  }

  @Contract("_, null, null -> fail")
  @NotNull
  private static MarkdownHtmlPanel detachOldPanelAndCreateAndAttachNewOne(@NotNull JPanel panelWrapper,
                                                                          @Nullable MarkdownHtmlPanel oldPanel,
                                                                          @Nullable MarkdownHtmlPanelProvider newPanelProvider) {
    ApplicationManager.getApplication().assertIsDispatchThread();
    if (oldPanel == null && newPanelProvider == null) {
      throw new IllegalArgumentException("Either create new one or leave the old");
    }
    if (newPanelProvider == null) {
      return oldPanel;
    }
    if (oldPanel != null) {
      panelWrapper.remove(oldPanel.getComponent());
      Disposer.dispose(oldPanel);
    }

    final MarkdownHtmlPanel newPanel = newPanelProvider.createHtmlPanel();
    panelWrapper.add(newPanel.getComponent(), BorderLayout.CENTER);
    panelWrapper.repaint();

    return newPanel;
  }

  private static void updatePanelCssSettings(@NotNull MarkdownHtmlPanel panel, @NotNull final MarkdownCssSettings cssSettings) {
    ApplicationManager.getApplication().assertIsDispatchThread();
    //noinspection StringBufferReplaceableByString
    final String inlineCss = new StringBuilder().append(cssSettings.isTextEnabled() ? cssSettings.getStylesheetText() + "\n" : "")
                                                .append("body {\n  font-size: ")
                                                .append(JBUI
                                                          .scale(100))
                                                .append
                                                  ("%;\n}")
                                                .toString();

    panel.setCSS(inlineCss, cssSettings.isUriEnabled() ? cssSettings.getStylesheetUri() : null);
    panel.render();
  }


  private class MyUpdatePanelOnSettingsChangedListener implements MarkdownSettingsChangedListener {
    @Override
    public void onSettingsChange(@NotNull final MarkdownApplicationSettings settings) {
      final MarkdownHtmlPanelProvider newPanelProvider = retrievePanelProvider(settings);

      mySwingAlarm.addRequest(new Runnable() {
        @Override
        public void run() {
          myPanel = detachOldPanelAndCreateAndAttachNewOne(myHtmlPanelWrapper, myPanel, newPanelProvider);
          myPanel.setHtml(myLastRenderedHtml);
          updatePanelCssSettings(myPanel, settings.getMarkdownCssSettings());
        }
      }, 0, Application.get().getModalityStateForComponent(getComponent()));
    }
  }
}
