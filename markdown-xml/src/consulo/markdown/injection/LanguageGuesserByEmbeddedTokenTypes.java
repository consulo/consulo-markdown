package consulo.markdown.injection;

import java.util.Arrays;
import java.util.List;

import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.Language;
import com.intellij.lexer.HtmlEmbeddedTokenTypesProvider;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.util.NotNullLazyValue;

/**
 * @author VISTALL
 * @since 03-Jul-16
 */
public class LanguageGuesserByEmbeddedTokenTypes implements LanguageGuesser
{
	private NotNullLazyValue<List<HtmlEmbeddedTokenTypesProvider>> embeddedTokenTypeProviders = new NotNullLazyValue<List<HtmlEmbeddedTokenTypesProvider>>()
	{
		@NotNull
		@Override
		protected List<HtmlEmbeddedTokenTypesProvider> compute()
		{
			return Arrays.asList(Extensions.getExtensions(HtmlEmbeddedTokenTypesProvider.EXTENSION_POINT_NAME));
		}
	};

	@Nullable
	@Override
	public Language guessLanguage(@NotNull MarkdownCodeFenceImpl markdownCodeFence, @NotNull String fenceLanguage)
	{
		for(HtmlEmbeddedTokenTypesProvider provider : embeddedTokenTypeProviders.getValue())
		{
			if(provider.getName().equalsIgnoreCase(fenceLanguage))
			{
				return provider.getElementType().getLanguage();
			}
		}
		return null;
	}
}
