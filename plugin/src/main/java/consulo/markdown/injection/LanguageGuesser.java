package consulo.markdown.injection;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.component.extension.ExtensionPointName;
import consulo.language.Language;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.lookup.LookupElement;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceImpl;

import java.util.Collection;
import java.util.List;

/**
 * Picks the language injected into a code fence by the info string after its opening backticks.
 *
 * @author VISTALL
 * @since 03-Jul-16
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface LanguageGuesser {
  ExtensionPointName<LanguageGuesser> EP_NAME = ExtensionPointName.create(LanguageGuesser.class);

  /**
   * @param fenceLanguage the whole info string of the fence, for example {@code php} or {@code twig {.example}};
   *                      {@link #getLanguageName(String)} gives its language name
   */
  @Nullable
  Language guessLanguage(@Nonnull MarkdownCodeFenceImpl markdownCodeFence, @Nonnull String fenceLanguage);

  /**
   * @return the info strings offered by completion after the opening backticks of a fence
   */
  @Nonnull
  default Collection<LookupElement> getCompletionVariants(@Nonnull CompletionParameters parameters) {
    return List.of();
  }

  /**
   * @return the language name of an info string: its first word, without the attributes after it
   */
  @Nonnull
  static String getLanguageName(@Nonnull String infoString) {
    String trimmed = infoString.trim();
    for (int i = 0; i < trimmed.length(); i++) {
      char c = trimmed.charAt(i);
      if (Character.isWhitespace(c) || c == '{' || c == ',') {
        return trimmed.substring(0, i);
      }
    }
    return trimmed;
  }
}
