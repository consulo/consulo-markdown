package org.intellij.plugins.markdown.ui.preview;

import org.intellij.plugins.markdown.lang.MarkdownFileType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.WeighedFileEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class MarkdownPreviewFileEditorProvider extends WeighedFileEditorProvider
{
	@Override
	public boolean accept(@NotNull Project project, @NotNull VirtualFile file)
	{
		return file.getFileType() == MarkdownFileType.INSTANCE;
	}

	@NotNull
	@Override
	public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file)
	{
		return new MarkdownPreviewFileEditor(file);
	}

	@Override
	public void disposeEditor(@NotNull FileEditor fileEditor)
	{

	}

	@NotNull
	@Override
	public FileEditorState readState(@NotNull Element element, @NotNull Project project, @NotNull VirtualFile virtualFile)
	{
		return FileEditorState.INSTANCE;
	}

	@Override
	public void writeState(@NotNull FileEditorState fileEditorState, @NotNull Project project, @NotNull Element element)
	{

	}

	@NotNull
	@Override
	public String getEditorTypeId()
	{
		return "markdown-preview-editor";
	}

	@NotNull
	@Override
	public FileEditorPolicy getPolicy()
	{
		return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
	}
}
