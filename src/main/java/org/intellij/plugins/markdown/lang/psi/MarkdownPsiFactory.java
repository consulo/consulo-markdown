package org.intellij.plugins.markdown.lang.psi;

import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiElement;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets;
import org.intellij.plugins.markdown.lang.psi.impl.*;
import org.jetbrains.annotations.NotNull;

public class MarkdownPsiFactory {
  public static final MarkdownPsiFactory INSTANCE = new MarkdownPsiFactory();

  public PsiElement createElement(@NotNull ASTNode node) {
    final IElementType elementType = node.getElementType();

    if (elementType == MarkdownElementTypes.PARAGRAPH) {
      return new MarkdownParagraphImpl(node);
    }
    if (MarkdownTokenTypeSets.HEADERS.contains(elementType)) {
      return new MarkdownHeaderImpl(node);
    }
    if (elementType == MarkdownElementTypes.CODE_FENCE) {
      return ((MarkdownCodeFenceImpl)node);
    }
    if (MarkdownTokenTypeSets.LISTS.contains(elementType)) {
      return new MarkdownListImpl(node);
    }
    if (elementType == MarkdownElementTypes.LIST_ITEM) {
      return new MarkdownListItemImpl(node);
    }
    if (elementType == MarkdownElementTypes.BLOCK_QUOTE) {
      return new MarkdownBlockQuoteImpl(node);
    }
    if (elementType == MarkdownElementTypes.LINK_DEFINITION) {
      return new MarkdownLinkDefinitionImpl(node);
    }
    if (elementType == MarkdownElementTypes.CODE_BLOCK) {
      return new MarkdownCodeBlockImpl(node);
    }
    if (elementType == MarkdownElementTypes.HTML_BLOCK) {
      return new MarkdownHtmlBlockImpl(node);
    }
    if (elementType == MarkdownElementTypes.TABLE) {
      return new MarkdownTableImpl(node);
    }
    if (elementType == MarkdownElementTypes.TABLE_ROW || elementType == MarkdownElementTypes.TABLE_HEADER) {
      return new MarkdownTableRowImpl(node);
    }
    if (elementType == MarkdownElementTypes.TABLE_CELL) {
      return new MarkdownTableCellImpl(node);
    }

    return new ASTWrapperPsiElement(node);
  }
}
