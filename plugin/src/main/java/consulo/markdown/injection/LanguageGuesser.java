package consulo.markdown.injection;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.component.extension.ExtensionPointName;
import consulo.language.Language;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceImpl;

/**
 * @author VISTALL
 * @since 03-Jul-16
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface LanguageGuesser {
  ExtensionPointName<LanguageGuesser> EP_NAME = ExtensionPointName.create(LanguageGuesser.class);

  @Nullable
  Language guessLanguage(@Nonnull MarkdownCodeFenceImpl markdownCodeFence, @Nonnull String fenceLanguage);
}
