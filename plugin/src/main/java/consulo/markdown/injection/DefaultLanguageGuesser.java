package consulo.markdown.injection;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.NotNullLazyValue;
import consulo.language.Language;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceImpl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author VISTALL
 * @since 03-Jul-16
 */
@ExtensionImpl
public class DefaultLanguageGuesser implements LanguageGuesser {
  private NotNullLazyValue<Map<String, Language>> langIdToLanguage = new NotNullLazyValue<Map<String, Language>>() {
    @Nonnull
    @Override
    protected Map<String, Language> compute() {
      final HashMap<String, Language> result = new HashMap<String, Language>();
      for (Language language : Language.getRegisteredLanguages()) {
        result.put(language.getID().toLowerCase(Locale.US), language);
      }

      result.put("js", result.get("javascript"));
      result.put("csharp", result.get("c#"));
      return result;
    }
  };

  @Nullable
  @Override
  public Language guessLanguage(@Nonnull MarkdownCodeFenceImpl markdownCodeFence, @Nonnull String fenceLanguage) {
    return langIdToLanguage.getValue().get(fenceLanguage.toLowerCase(Locale.US));
  }
}
