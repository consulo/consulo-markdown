package consulo.markdown.lang.psi;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.ast.IElementType;
import consulo.language.impl.ast.ASTLeafFactory;
import consulo.language.impl.psi.LeafPsiElement;
import consulo.language.version.LanguageVersion;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceContentImpl;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 03-Jul-16
 */
@ExtensionImpl
public class MarkdownASTLeafFactory implements ASTLeafFactory {
  @Nonnull
  @Override
  public LeafPsiElement createLeaf(@Nonnull IElementType type, @Nonnull LanguageVersion languageVersion, @Nonnull CharSequence text) {
    return new MarkdownCodeFenceContentImpl(type, text);
  }

  @Override
  public boolean test(@Nullable IElementType type) {
    return type == MarkdownTokenTypes.CODE_FENCE_CONTENT;
  }
}
