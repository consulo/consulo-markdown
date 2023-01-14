package org.intellij.plugins.markdown.ui.preview;

import consulo.codeEditor.Editor;
import consulo.codeEditor.event.CaretAdapter;
import consulo.codeEditor.event.CaretEvent;
import consulo.fileEditor.TextEditor;
import consulo.navigation.Navigatable;
import org.intellij.plugins.markdown.ui.split.SplitFileEditor;
import org.jetbrains.annotations.NotNull;

public class MarkdownSplitEditor extends SplitFileEditor<TextEditor, MarkdownPreviewFileEditor> implements TextEditor {
  public MarkdownSplitEditor(@NotNull TextEditor mainEditor, @NotNull MarkdownPreviewFileEditor secondEditor) {
    super(mainEditor, secondEditor);

    mainEditor.getEditor().getCaretModel().addCaretListener(new MyCaretListener(secondEditor));
  }

  @NotNull
  @Override
  public String getName() {
    return "Markdown split editor";
  }

  @NotNull
  @Override
  public Editor getEditor() {
    return getMainEditor().getEditor();
  }

  @Override
  public boolean canNavigateTo(@NotNull Navigatable navigatable) {
    return getMainEditor().canNavigateTo(navigatable);
  }

  @Override
  public void navigateTo(@NotNull Navigatable navigatable) {
    getMainEditor().navigateTo(navigatable);
  }

  private static class MyCaretListener extends CaretAdapter {
    @NotNull
    private final MarkdownPreviewFileEditor myPreviewFileEditor;

    public MyCaretListener(@NotNull MarkdownPreviewFileEditor previewFileEditor) {
      myPreviewFileEditor = previewFileEditor;
    }

    @Override
    public void caretPositionChanged(CaretEvent e) {
      final Editor editor = e.getEditor();
      if (editor.getCaretModel().getCaretCount() != 1) {
        return;
      }

      final int offset = editor.logicalPositionToOffset(e.getNewPosition());
      myPreviewFileEditor.scrollToSrcOffset(offset);
    }
  }
}
