package consulo.markdown.lang.psi;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.ast.IElementType;
import consulo.language.impl.ast.ASTLeafFactory;
import consulo.language.impl.psi.LeafPsiElement;
import consulo.language.version.LanguageVersion;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceContentImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 03-Jul-16
 */
@ExtensionImpl
public class MarkdownASTLeafFactory implements ASTLeafFactory {
  @NotNull
  @Override
  public LeafPsiElement createLeaf(@NotNull IElementType type, @NotNull LanguageVersion languageVersion, @NotNull CharSequence text) {
    return new MarkdownCodeFenceContentImpl(type, text);
  }

  @Override
  public boolean test(@Nullable IElementType type) {
    return type == MarkdownTokenTypes.CODE_FENCE_CONTENT;
  }
}
