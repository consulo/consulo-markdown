package org.intellij.plugins.markdown.ui.preview;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.fileEditor.FileEditor;
import consulo.fileEditor.FileEditorProvider;
import consulo.fileEditor.TextEditor;
import consulo.fileEditor.TextEditorWithPreviewFactory;
import consulo.fileEditor.text.TextEditorProvider;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.ActionGroup;
import consulo.ui.ex.action.ActionManager;
import consulo.ui.ex.action.ActionToolbar;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import org.intellij.plugins.markdown.lang.MarkdownFileType;

@ExtensionImpl
public class MarkdownSplitEditorProvider implements FileEditorProvider, DumbAware {
  private final TextEditorWithPreviewFactory myTextEditorWithPreviewFactory;

  @Inject
  public MarkdownSplitEditorProvider(TextEditorWithPreviewFactory textEditorWithPreviewFactory) {
    myTextEditorWithPreviewFactory = textEditorWithPreviewFactory;
  }

  @Override
  public boolean accept(@Nonnull Project project, @Nonnull VirtualFile virtualFile) {
    return virtualFile.getFileType() == MarkdownFileType.INSTANCE;
  }

  @RequiredUIAccess
  @Nonnull
  @Override
  public FileEditor createEditor(@Nonnull Project project, @Nonnull VirtualFile virtualFile) {
    ActionManager manager = ActionManager.getInstance();
    String id = "Markdown.Toolbar.Left";
    ActionGroup action = (ActionGroup)manager.getAction(id);
    ActionToolbar actionToolbar = manager.createActionToolbar(id, action, true);
    return myTextEditorWithPreviewFactory.create((TextEditor)TextEditorProvider.getInstance().createEditor(project, virtualFile),
                                                 new MarkdownPreviewFileEditor(virtualFile),
                                                 actionToolbar,
                                                 "MarkdownPreview");
  }

  @Nonnull
  @Override
  public String getEditorTypeId() {
    return "markdownPreview";
  }
}
