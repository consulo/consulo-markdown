package consulo.markdown.injection.impl;

import consulo.markdown.injection.LanguageGuesser;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.inject.advanced.InjectedLanguage;
import consulo.language.inject.advanced.TemporaryPlacesRegistry;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.plugins.markdown.lang.psi.MarkdownCodeFence;

/**
 * @author VISTALL
 * @since 03-Jul-16
 */
@ExtensionImpl
public class LanguageGuesserByTemporaryPlacesRegistry implements LanguageGuesser {
  @Nullable
  @Override
  public Language guessLanguage(@Nonnull MarkdownCodeFence markdownCodeFence, @Nonnull String fenceLanguage) {
    final TemporaryPlacesRegistry registry = TemporaryPlacesRegistry.getInstance(markdownCodeFence.getProject());
    final InjectedLanguage language = registry.getLanguageFor(markdownCodeFence, markdownCodeFence.getContainingFile());
    if (language != null) {
      return language.getLanguage();
    }
    return null;
  }
}
