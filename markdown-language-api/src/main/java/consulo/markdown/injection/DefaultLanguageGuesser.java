package consulo.markdown.injection;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.NotNullLazyValue;
import consulo.language.Language;
import consulo.language.file.LanguageFileType;
import consulo.language.util.LanguageUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.plugins.markdown.lang.psi.MarkdownCodeFence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
        // a template language needs its own file to be injected, a plugin providing that registers its own guesser
        if (!LanguageUtil.isInjectableLanguage(language)) {
          continue;
        }
        String id = language.getID().toLowerCase(Locale.US);
        // an info string names its language by its first word
        if (LanguageGuesser.getLanguageName(id).equals(id)) {
          result.put(id, language);
        }
      }

      putAlias(result, "js", "javascript");
      putAlias(result, "csharp", "c#");
      return result;
    }
  };

  private static void putAlias(Map<String, Language> map, String alias, String id) {
    Language language = map.get(id);
    if (language != null) {
      map.put(alias, language);
    }
  }

  @Nullable
  @Override
  public Language guessLanguage(@Nonnull MarkdownCodeFence markdownCodeFence, @Nonnull String fenceLanguage) {
    return guessLanguage(fenceLanguage);
  }

  @Nullable
  @Override
  public Language guessLanguage(@Nonnull String fenceLanguage) {
    return langIdToLanguage.getValue().get(LanguageGuesser.getLanguageName(fenceLanguage).toLowerCase(Locale.US));
  }

  @Nonnull
  @Override
  public Collection<LanguageVariant> getVariants() {
    List<LanguageVariant> result = new ArrayList<>();
    for (Map.Entry<String, Language> entry : langIdToLanguage.getValue().entrySet()) {
      Language language = entry.getValue();
      LanguageFileType fileType = language.getAssociatedFileType();
      result.add(new LanguageVariant(entry.getKey(), fileType == null ? null : fileType.getIcon(), language.getDisplayName()));
    }
    return result;
  }
}
