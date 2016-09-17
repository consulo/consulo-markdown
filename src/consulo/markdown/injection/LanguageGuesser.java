package consulo.markdown.injection;

import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.Language;
import consulo.extensions.CompositeExtensionPointName;

/**
 * @author VISTALL
 * @since 03-Jul-16
 */
public interface LanguageGuesser
{
	CompositeExtensionPointName<LanguageGuesser> EP_NAME = CompositeExtensionPointName.applicationPoint("consulo.markdown.langGuesser", LanguageGuesser.class);

	@Nullable
	Language guessLanguage(@NotNull MarkdownCodeFenceImpl markdownCodeFence, @NotNull String fenceLanguage);
}
