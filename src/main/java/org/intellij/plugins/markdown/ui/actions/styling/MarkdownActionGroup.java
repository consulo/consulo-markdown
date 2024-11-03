package org.intellij.plugins.markdown.ui.actions.styling;

import consulo.annotation.component.ActionImpl;
import consulo.annotation.component.ActionRef;
import consulo.ui.ex.action.DefaultActionGroup;

/**
 * @author VISTALL
 * @since 2024-11-03
 */
@ActionImpl(id = MarkdownActionGroup.ID, children = {
  @ActionRef(type = ToggleBoldAction.class),
  @ActionRef(type = ToggleItalicAction.class),
  @ActionRef(type = ToggleCodeSpanAction.class),
})
public class MarkdownActionGroup extends DefaultActionGroup {
  public static final String ID = "Markdown.Toolbar.Left";
}
