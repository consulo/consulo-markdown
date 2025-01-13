/**
 * @author VISTALL
 * @since 14/01/2023
 */
open module consulo.markdown {
  requires consulo.ide.api;

  // TODO remove in future
  requires java.desktop;
  requires forms.rt;

  requires markdown;
}