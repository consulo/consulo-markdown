package consulo.markdown.injection;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.NotNullLazyValue;
import consulo.language.Language;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.file.LanguageFileType;
import consulo.language.util.LanguageUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceImpl;

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
  public Language guessLanguage(@Nonnull MarkdownCodeFenceImpl markdownCodeFence, @Nonnull String fenceLanguage) {
    return langIdToLanguage.getValue().get(LanguageGuesser.getLanguageName(fenceLanguage).toLowerCase(Locale.US));
  }

  @Nonnull
  @Override
  public Collection<LookupElement> getCompletionVariants(@Nonnull CompletionParameters parameters) {
    List<LookupElement> result = new ArrayList<>();
    for (Map.Entry<String, Language> entry : langIdToLanguage.getValue().entrySet()) {
      Language language = entry.getValue();
      LanguageFileType fileType = language.getAssociatedFileType();
      result.add(LookupElementBuilder.create(entry.getKey())
        .withIcon(fileType == null ? null : fileType.getIcon())
        .withTypeText(language.getDisplayName().get(), true));
    }
    return result;
  }
}
