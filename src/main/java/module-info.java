/**
 * @author VISTALL
 * @since 14/01/2023
 */
open module consulo.markdown {
  requires consulo.ide.api;

  // TODO remove this in future
  requires consulo.ide.impl;
  requires java.desktop;
  requires forms.rt;

  requires loboevolution;

  requires markdown;
}