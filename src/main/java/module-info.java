/**
 * @author VISTALL
 * @since 14/01/2023
 */
open module consulo.markdown {
  requires consulo.language.editor.api;
  requires consulo.language.impl;
  requires consulo.configurable.api;
  requires consulo.file.editor.api;
  requires consulo.ui.ex.api;
  requires consulo.language.spellchecker.api;
  requires consulo.language.inject.advanced.api;
  requires consulo.execution.api;

  // TODO remove in future
  requires java.desktop;
  requires forms.rt;

  requires markdown;
}