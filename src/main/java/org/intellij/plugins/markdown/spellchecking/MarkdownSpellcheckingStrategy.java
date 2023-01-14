package org.intellij.plugins.markdown.spellchecking;

import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.TokenSet;
import consulo.language.impl.ast.TreeUtil;
import consulo.language.psi.PsiElement;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.MarkdownLanguage;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class MarkdownSpellcheckingStrategy extends SpellcheckingStrategy {

  public static final TokenSet NO_SPELLCHECKING_TYPES = TokenSet.create(MarkdownElementTypes.CODE_BLOCK,
                                                                        MarkdownElementTypes.CODE_FENCE,
                                                                        MarkdownElementTypes.CODE_SPAN,
                                                                        MarkdownElementTypes.LINK_DESTINATION);

  @NotNull
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
