/**
 * @author VISTALL
 * @since 14/01/2023
 */
open module consulo.markdown {
  requires transitive consulo.language.editor.api;
  requires consulo.language.impl;
  requires consulo.configurable.api;
  requires consulo.file.editor.api;
  requires consulo.ui.ex.api;
  requires consulo.language.spellchecker.api;
  requires consulo.language.inject.advanced.api;
  requires consulo.execution.api;

  requires transitive consulo.markdown.engine;
  requires transitive consulo.markdown.language.api;
  requires transitive consulo.markdown.language.impl;
  requires transitive consulo.markdown.language.editor.api;


  // for Markdown dialects in other plugins (MDX)
  exports org.intellij.plugins.markdown.braces;
  exports org.intellij.plugins.markdown.highlighting.impl;
  exports org.intellij.plugins.markdown.injection;
  exports org.intellij.plugins.markdown.lang.formatter.settings;

  // TODO remove in future
  requires java.desktop;
  requires forms.rt;

}