package consulo.markdown.injection;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.Language;
import com.intellij.openapi.util.NotNullLazyValue;

/**
 * @author VISTALL
 * @since 03-Jul-16
 */
public class DefaultLanguageGuesser implements LanguageGuesser
{
	private NotNullLazyValue<Map<String, Language>> langIdToLanguage = new NotNullLazyValue<Map<String, Language>>()
	{
		@NotNull
		@Override
		protected Map<String, Language> compute()
		{
			final HashMap<String, Language> result = new HashMap<String, Language>();
			for(Language language : Language.getRegisteredLanguages())
			{
				result.put(language.getID().toLowerCase(Locale.US), language);
			}

			result.put("js", result.get("javascript"));
			result.put("csharp", result.get("c#"));
			return result;
		}
	};

	@Nullable
	@Override
	public Language guessLanguage(@NotNull MarkdownCodeFenceImpl markdownCodeFence, @NotNull String fenceLanguage)
	{
		return langIdToLanguage.getValue().get(fenceLanguage.toLowerCase(Locale.US));
	}
}
