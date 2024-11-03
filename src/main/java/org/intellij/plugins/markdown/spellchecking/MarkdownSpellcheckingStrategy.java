package org.intellij.plugins.markdown.spellchecking;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.TokenSet;
import consulo.language.impl.ast.TreeUtil;
import consulo.language.psi.PsiElement;
import consulo.language.spellcheker.SpellcheckingStrategy;
import consulo.language.spellcheker.tokenizer.Tokenizer;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.MarkdownLanguage;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;
import jakarta.annotation.Nonnull;

@ExtensionImpl
public class MarkdownSpellcheckingStrategy extends SpellcheckingStrategy {

  public static final TokenSet NO_SPELLCHECKING_TYPES = TokenSet.create(MarkdownElementTypes.CODE_BLOCK,
                                                                        MarkdownElementTypes.CODE_FENCE,
                                                                        MarkdownElementTypes.CODE_SPAN,
                                                                        MarkdownElementTypes.LINK_DESTINATION);

  @Nonnull
  @Override
  public Tokenizer getTokenizer(PsiElement element) {
    final ASTNode node = element.getNode();
    if (node == null || node.getElementType() != MarkdownTokenTypes.TEXT) {
      return EMPTY_TOKENIZER;
    }
    if (TreeUtil.findParent(node, NO_SPELLCHECKING_TYPES) != null) {
      return EMPTY_TOKENIZER;
    }

    return TEXT_TOKENIZER;
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return MarkdownLanguage.INSTANCE;
  }
}
