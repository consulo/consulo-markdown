package consulo.markdown;

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.AdditionalTextAttributesProvider;
import consulo.colorScheme.EditorColorsScheme;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 28/04/2023
 */
@ExtensionImpl
public class LightAdditionalTextAttributesProvider implements AdditionalTextAttributesProvider {
  @Nonnull
  @Override
  public String getColorSchemeName() {
    return EditorColorsScheme.DEFAULT_SCHEME_NAME;
  }

  @Nonnull
  @Override
  public String getColorSchemeFile() {
    return "/colorSchemes/MarkdownDefault.xml";
  }
}
