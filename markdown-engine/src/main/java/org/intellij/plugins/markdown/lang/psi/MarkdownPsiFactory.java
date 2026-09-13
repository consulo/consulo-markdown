package org.intellij.plugins.markdown.lang.psi;

import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets;
import org.intellij.plugins.markdown.lang.psi.impl.*;

public class MarkdownPsiFactory {
  public static final MarkdownPsiFactory INSTANCE = new MarkdownPsiFactory();

  public PsiElement createElement(@Nonnull ASTNode node) {
    final IElementType elementType = node.getElementType();

    if (elementType == MarkdownElementTypes.PARAGRAPH) {
      return new MarkdownParagraphImpl(node);
    }
    if (MarkdownTokenTypeSets.HEADERS.contains(elementType)) {
      return new MarkdownHeaderImpl(node);
    }
    if (elementType == MarkdownElementTypes.CODE_FENCE) {
      // MarkdownCodeFenceImpl is a CompositePsiElement, i.e. it is the node itself - so this only
      // works when something created the node as one. Nothing registers an ASTFactory that does,
      // so in practice the fence falls through to the platform default, which is what the parsing
      // expectations record. Guarded rather than removed to keep the manipulator and the
      // injection host reachable if a factory is ever registered.
      if (node instanceof MarkdownCodeFenceImpl) {
        return (MarkdownCodeFenceImpl)node;
      }
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
