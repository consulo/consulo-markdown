package org.intellij.plugins.markdown.ui.preview;

import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProviderImpl;
import consulo.fileEditor.impl.text.TextEditorComponentContainerFactory;
import org.intellij.plugins.markdown.ui.split.SplitTextEditorProvider;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;

import javax.annotation.Nonnull;

public class MarkdownSplitEditorProvider extends SplitTextEditorProvider
{
	public MarkdownSplitEditorProvider(@Nonnull TextEditorComponentContainerFactory factory)
	{
		super(new PsiAwareTextEditorProviderImpl(factory), new MarkdownPreviewFileEditorProvider());
	}

	@Override
	protected FileEditor createSplitEditor(@NotNull final FileEditor firstEditor, @NotNull FileEditor secondEditor)
	{
		if(!(firstEditor instanceof TextEditor) || !(secondEditor instanceof MarkdownPreviewFileEditor))
		{
			throw new IllegalArgumentException("Main editor should be TextEditor");
		}
		return new MarkdownSplitEditor(((TextEditor) firstEditor), ((MarkdownPreviewFileEditor) secondEditor));
	}
}
