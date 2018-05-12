package org.intellij.plugins.markdown.ui.preview;

import org.intellij.plugins.markdown.ui.split.SplitTextEditorProvider;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.DesktopPsiAwareTextEditorProvider;

public class MarkdownSplitEditorProvider extends SplitTextEditorProvider
{
	public MarkdownSplitEditorProvider()
	{
		super(new DesktopPsiAwareTextEditorProvider(), new MarkdownPreviewFileEditorProvider());
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

	@Override
	public void disposeEditor(@NotNull FileEditor fileEditor)
	{

	}
}
