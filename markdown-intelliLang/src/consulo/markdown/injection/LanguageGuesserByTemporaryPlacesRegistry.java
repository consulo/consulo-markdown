package consulo.markdown.injection;

import org.intellij.plugins.intelliLang.inject.InjectedLanguage;
import org.intellij.plugins.intelliLang.inject.TemporaryPlacesRegistry;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.Language;

/**
 * @author VISTALL
 * @since 03-Jul-16
 */
public class LanguageGuesserByTemporaryPlacesRegistry implements LanguageGuesser
{
	@Nullable
	@Override
	public Language guessLanguage(@NotNull MarkdownCodeFenceImpl markdownCodeFence, @NotNull String fenceLanguage)
	{
		final TemporaryPlacesRegistry registry = TemporaryPlacesRegistry.getInstance(markdownCodeFence.getProject());
		final InjectedLanguage language = registry.getLanguageFor(markdownCodeFence, markdownCodeFence.getContainingFile());
		if(language != null)
		{
			return language.getLanguage();
		}
		return null;
	}
}
