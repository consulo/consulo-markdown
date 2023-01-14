package org.intellij.plugins.markdown.ui.preview;

import consulo.fileEditor.FileEditor;
import consulo.fileEditor.FileEditorPolicy;
import consulo.fileEditor.FileEditorState;
import consulo.fileEditor.WeighedFileEditorProvider;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.intellij.plugins.markdown.lang.MarkdownFileType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class MarkdownPreviewFileEditorProvider extends WeighedFileEditorProvider {
  @Override
  public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
    return file.getFileType() == MarkdownFileType.INSTANCE;
  }

  @NotNull
  @Override
  public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
    return new MarkdownPreviewFileEditor(file);
  }

  @Override
  public void disposeEditor(@NotNull FileEditor fileEditor) {

  }

  @NotNull
  @Override
  public FileEditorState readState(@NotNull Element element, @NotNull Project project, @NotNull VirtualFile virtualFile) {
    return FileEditorState.INSTANCE;
  }

  @Override
  public void writeState(@NotNull FileEditorState fileEditorState, @NotNull Project project, @NotNull Element element) {

  }

  @NotNull
  @Override
  public String getEditorTypeId() {
    return "markdown-preview-editor";
  }

  @NotNull
  @Override
  public FileEditorPolicy getPolicy() {
    return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
  }
}
