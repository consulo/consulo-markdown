package consulo.markdown.lang.psi;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.ast.IElementType;
import consulo.language.impl.ast.ASTCompositeFactory;
import consulo.language.impl.ast.CompositeElement;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceImpl;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 03-Jul-16
 */
@ExtensionImpl
public class MarkdownASTCompositeFactory implements ASTCompositeFactory {
  @Override
  public CompositeElement createComposite(IElementType type) {
    return new MarkdownCodeFenceImpl(type);
  }

  @Override
  public boolean test(@Nullable IElementType type) {
    return type == MarkdownElementTypes.CODE_FENCE;
  }
}
