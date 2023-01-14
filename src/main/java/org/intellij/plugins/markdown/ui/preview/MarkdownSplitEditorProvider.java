package org.intellij.plugins.markdown.ui.preview;

import consulo.annotation.component.ExtensionImpl;
import consulo.fileEditor.FileEditor;
import consulo.fileEditor.TextEditor;
import consulo.ide.impl.fileEditor.text.TextEditorComponentContainerFactory;
import consulo.ide.impl.idea.openapi.fileEditor.impl.text.PsiAwareTextEditorProviderImpl;
import jakarta.inject.Inject;
import org.intellij.plugins.markdown.ui.split.SplitTextEditorProvider;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

@ExtensionImpl
public class MarkdownSplitEditorProvider extends SplitTextEditorProvider {
  @Inject
  public MarkdownSplitEditorProvider(@Nonnull TextEditorComponentContainerFactory factory) {
    super(new PsiAwareTextEditorProviderImpl(factory), new MarkdownPreviewFileEditorProvider());
  }

  @Override
  protected FileEditor createSplitEditor(@NotNull final FileEditor firstEditor, @NotNull FileEditor secondEditor) {
    if (!(firstEditor instanceof TextEditor) || !(secondEditor instanceof MarkdownPreviewFileEditor)) {
      throw new IllegalArgumentException("Main editor should be TextEditor");
    }
    return new MarkdownSplitEditor(((TextEditor)firstEditor), ((MarkdownPreviewFileEditor)secondEditor));
  }
}
