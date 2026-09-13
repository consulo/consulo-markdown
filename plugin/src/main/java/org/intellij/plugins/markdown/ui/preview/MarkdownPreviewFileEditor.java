package org.intellij.plugins.markdown.ui.preview;

import consulo.application.Application;
import consulo.component.messagebus.MessageBusConnection;
import consulo.disposer.Disposer;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.document.event.DocumentAdapter;
import consulo.document.event.DocumentEvent;
import consulo.fileEditor.FileEditor;
import consulo.fileEditor.FileEditorState;
import consulo.fileEditor.FileEditorStateLevel;
import consulo.ui.Component;
import consulo.ui.HtmlView;
import consulo.ui.ex.awt.JBUI;
import consulo.ui.ex.awt.util.Alarm;
import consulo.ui.ex.awtUnsafe.TargetAWT;
import consulo.util.dataholder.UserDataHolderBase;
import consulo.util.lang.StringUtil;
import consulo.project.Project;
import consulo.application.ReadAction;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import kava.beans.PropertyChangeListener;
import consulo.language.ast.IElementType;
import org.intellij.markdown.html.GeneratingProvider;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.markdown.parser.LinkMap;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.markdown.parser.MarkdownParserManager;
import org.intellij.plugins.markdown.settings.MarkdownApplicationSettings;
import org.intellij.plugins.markdown.settings.MarkdownCssSettings;
import org.intellij.plugins.markdown.settings.MarkdownSettingsChangedListener;

import javax.swing.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public class MarkdownPreviewFileEditor extends UserDataHolderBase implements FileEditor {
  private final static long PARSING_CALL_TIMEOUT_MS = 50L;

  private final static long RENDERING_DELAY_MS = 20L;
  @Nonnull
  private HtmlView myPanel;
  @Nonnull
  private final Project myProject;
  private final VirtualFile myFile;
  @Nullable
  private final Document myDocument;
  @Nonnull
  private final Alarm myPooledAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);
  @Nonnull
  private final Alarm mySwingAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, this);

  private final Object REQUESTS_LOCK = new Object();
  @Nullable
  private Runnable myLastScrollRequest = null;
  @Nullable
  private Runnable myLastHtmlOrRefreshRequest = null;

  private volatile int myLastScrollOffset;
  @Nonnull
  private String myLastRenderedHtml = "";

  public MarkdownPreviewFileEditor(@Nonnull Project project, @Nonnull VirtualFile file) {
    myProject = project;
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

    myPanel = HtmlView.create();

    MessageBusConnection settingsConnection = Application.get().getMessageBus().connect(this);
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

  @Override
  public @Nullable Component getUIComponent() {
    return myPanel;
  }

  @Nonnull
  @Override
  public String getName() {
    return "Markdown HTML Preview";
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
    myPooledAlarm.addRequest(() -> MarkdownPreviewFileEditor.this.updateHtml(true), 0);
  }

  /**
   * Is always run from pooled thread
   */
  private void updateHtml(final boolean preserveScrollOffset) {
    if (!myFile.isValid() || myDocument == null || Disposer.isDisposed(this)) {
      return;
    }

    final String html = generateMarkdownHtml();

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

            MarkdownApplicationSettings settings = MarkdownApplicationSettings.getInstance();
            MarkdownCssSettings cssSettings = settings.getMarkdownCssSettings();
            String inlineCss = new StringBuilder().append(cssSettings.isTextEnabled() ? cssSettings.getStylesheetText() + "\n" : "")
                                                  .toString();

            URL[] cssUrls = new URL[0];
            if (cssSettings.isUriEnabled()) {
              String stylesheetUri = cssSettings.getStylesheetUri();
              if (!StringUtil.isEmptyOrSpaces(stylesheetUri)) {
                try {
                  cssUrls = new URL[]{URI.create(stylesheetUri).toURL()};
                }
                catch (MalformedURLException ignored) {
                }
              }
            }

            myPanel.render(new HtmlView.RenderData(currentHtml, inlineCss, cssUrls)).whenComplete((o, throwable) -> {
                if (preserveScrollOffset) {
                    myPanel.scrollToMarkdownSrcOffset(myLastScrollOffset);
                }
            });
          }

          synchronized (REQUESTS_LOCK) {
            myLastHtmlOrRefreshRequest = null;
          }
        }
      };
      mySwingAlarm.addRequest(myLastHtmlOrRefreshRequest,
                              RENDERING_DELAY_MS,
                              Application.get().getModalityStateForComponent(getComponent()));
    }
  }

  @Override
  public void deselectNotify() {
  }

  @Override
  public void addPropertyChangeListener(@Nonnull PropertyChangeListener listener) {
  }

  @Override
  public void removePropertyChangeListener(@Nonnull PropertyChangeListener listener) {
  }

  @Nonnull
  @Override
  public VirtualFile getFile() {
    return myFile;
  }

  @Override
  public void dispose() {
  }

  @Nonnull
  private String generateMarkdownHtml() {
    // Renders from the file's own PSI tree rather than reparsing the text, so the preview and the
    // editor always agree. Needs a read action: this runs on a pooled thread from the alarm.
    return ReadAction.compute(() -> {
      PsiFile psiFile = PsiManager.getInstance(myProject).findFile(myFile);
      if (psiFile == null) {
        return "";
      }
      return org.intellij.plugins.markdown.html.MarkdownHtmlRenderer.renderTree(psiFile.getNode(),
                                             psiFile.getText(),
                                             org.intellij.plugins.markdown.html.MarkdownHtmlRenderer.baseUriFor(myFile));
    });
  }


  private class MyUpdatePanelOnSettingsChangedListener implements MarkdownSettingsChangedListener {
    @Override
    public void onSettingsChange(@Nonnull final MarkdownApplicationSettings settings) {
      mySwingAlarm.addRequest(new Runnable() {
        @Override
        public void run() {
          myLastRenderedHtml = null;
          updateHtml(false);
        }
      }, 0, Application.get().getModalityStateForComponent(getComponent()));
    }
  }
}
