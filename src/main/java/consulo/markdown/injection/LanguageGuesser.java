package consulo.markdown.injection;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.component.extension.ExtensionPointName;
import consulo.language.Language;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 03-Jul-16
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface LanguageGuesser {
  ExtensionPointName<LanguageGuesser> EP_NAME = ExtensionPointName.create(LanguageGuesser.class);

  @Nullable
  Language guessLanguage(@NotNull MarkdownCodeFenceImpl markdownCodeFence, @NotNull String fenceLanguage);
}
