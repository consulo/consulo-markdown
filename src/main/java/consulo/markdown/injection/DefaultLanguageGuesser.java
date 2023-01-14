package consulo.markdown.injection;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.NotNullLazyValue;
import consulo.language.Language;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @NotNull
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
  public Language guessLanguage(@NotNull MarkdownCodeFenceImpl markdownCodeFence, @NotNull String fenceLanguage) {
    return langIdToLanguage.getValue().get(fenceLanguage.toLowerCase(Locale.US));
  }
}
