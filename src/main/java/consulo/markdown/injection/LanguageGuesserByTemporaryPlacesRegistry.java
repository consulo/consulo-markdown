package consulo.markdown.injection;

import consulo.annotation.component.ExtensionImpl;
import consulo.ide.impl.intelliLang.inject.InjectedLanguage;
import consulo.ide.impl.intelliLang.inject.TemporaryPlacesRegistry;
import consulo.language.Language;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 03-Jul-16
 */
@ExtensionImpl
public class LanguageGuesserByTemporaryPlacesRegistry implements LanguageGuesser {
  @Nullable
  @Override
  public Language guessLanguage(@NotNull MarkdownCodeFenceImpl markdownCodeFence, @NotNull String fenceLanguage) {
    final TemporaryPlacesRegistry registry = TemporaryPlacesRegistry.getInstance(markdownCodeFence.getProject());
    final InjectedLanguage language = registry.getLanguageFor(markdownCodeFence, markdownCodeFence.getContainingFile());
    if (language != null) {
      return language.getLanguage();
    }
    return null;
  }
}
