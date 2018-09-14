package consulo.markdown.lang.psi;

import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceContentImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import consulo.lang.LanguageVersion;
import consulo.psi.tree.ASTLeafFactory;

/**
 * @author VISTALL
 * @since 03-Jul-16
 */
public class MarkdownASTLeafFactory implements ASTLeafFactory
{
	@NotNull
	@Override
	public LeafElement createLeaf(@NotNull IElementType type, @NotNull LanguageVersion languageVersion, @NotNull CharSequence text)
	{
		return new MarkdownCodeFenceContentImpl(type, text);
	}

	@Override
	public boolean apply(@Nullable IElementType type)
	{
		return type == MarkdownTokenTypes.CODE_FENCE_CONTENT;
	}
}
