package consulo.markdown;

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.AttributesFlyweightBuilder;
import consulo.colorScheme.EditorColorSchemeExtender;
import consulo.colorScheme.EditorColorsScheme;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.highlighting.MarkdownHighlighterColors;

/**
 * @author VISTALL
 * @since 28/04/2023
 */
@ExtensionImpl
public class LightAdditionalTextAttributesProvider implements EditorColorSchemeExtender {
    @Override
    public void extend(Builder builder) {
        builder.add(MarkdownHighlighterColors.BOLD_ATTR_KEY, MarkdownHighlighterColors.TEXT_ATTR_KEY, AttributesFlyweightBuilder
            .create()
            .withBoldFont()
            .build());

      builder.add(MarkdownHighlighterColors.ITALIC_ATTR_KEY, MarkdownHighlighterColors.TEXT_ATTR_KEY, AttributesFlyweightBuilder
          .create()
          .withItalicFont()
          .build());
    }

    @Nonnull
    @Override
    public String getColorSchemeId() {
        return EditorColorsScheme.DEFAULT_SCHEME_NAME;
    }
}
