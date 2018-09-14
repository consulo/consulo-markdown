package consulo.markdown.lang.psi;

import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.tree.IElementType;
import consulo.psi.tree.ASTCompositeFactory;

/**
 * @author VISTALL
 * @since 03-Jul-16
 */
public class MarkdownASTCompositeFactory implements ASTCompositeFactory
{
	@NotNull
	@Override
	public CompositeElement createComposite(IElementType type)
	{
		return new MarkdownCodeFenceImpl(type);
	}

	@Override
	public boolean apply(@Nullable IElementType type)
	{
		return type == MarkdownElementTypes.CODE_FENCE;
	}
}
