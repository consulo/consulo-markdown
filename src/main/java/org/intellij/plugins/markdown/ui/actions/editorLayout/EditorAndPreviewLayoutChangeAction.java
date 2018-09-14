package org.intellij.plugins.markdown.ui.actions.editorLayout;

import org.intellij.plugins.markdown.ui.split.SplitFileEditor;

public class EditorAndPreviewLayoutChangeAction extends BaseChangeSplitLayoutAction
{
	public EditorAndPreviewLayoutChangeAction()
	{
		super(SplitFileEditor.SplitEditorLayout.SPLIT);
	}
}
