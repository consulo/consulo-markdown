// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.intellij.plugins.markdown.spellchecking;

import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.impl.ast.TreeUtil;
import consulo.language.psi.PsiElement;
import consulo.language.spellcheker.text.util.PsiUtils;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;

final class MarkdownPsiUtils {
    private MarkdownPsiUtils() {
    }

    static boolean isMarkdownLinkType(ASTNode node) {
        IElementType elementType = node.getElementType();
        return elementType == MarkdownElementTypes.LINK_DEFINITION ||
            elementType == MarkdownElementTypes.LINK_LABEL ||
            elementType == MarkdownElementTypes.LINK_DESTINATION ||
            elementType == MarkdownElementTypes.LINK_TITLE ||
            elementType == MarkdownElementTypes.LINK_TEXT ||
            elementType == MarkdownElementTypes.FULL_REFERENCE_LINK ||
            elementType == MarkdownElementTypes.SHORT_REFERENCE_LINK ||
            elementType == MarkdownElementTypes.AUTOLINK ||
            elementType == MarkdownElementTypes.INLINE_LINK;
    }

    static boolean isMarkdownCodeType(ASTNode node) {
        IElementType elementType = node.getElementType();
        return elementType == MarkdownElementTypes.CODE_FENCE ||
            elementType == MarkdownElementTypes.CODE_BLOCK ||
            elementType == MarkdownElementTypes.CODE_SPAN ||
            elementType == MarkdownTokenTypes.CODE_FENCE_CONTENT ||
            elementType == MarkdownTokenTypes.CODE_LINE;
    }

    static boolean isParagraph(PsiElement element) {
        ASTNode node = element.getNode();
        return node != null && PsiUtils.hasType(node, MarkdownElementTypes.PARAGRAPH);
    }

    static boolean isHeaderContent(PsiElement element) {
        ASTNode node = element.getNode();
        return node != null && PsiUtils.hasType(node, MarkdownTokenTypes.ATX_CONTENT, MarkdownTokenTypes.SETEXT_CONTENT);
    }

    static boolean isOuterListItem(PsiElement element) {
        ASTNode node = element.getNode();
        return node != null && PsiUtils.hasType(node, MarkdownElementTypes.LIST_ITEM)
            && TreeUtil.findParent(node, TokenSet.create(MarkdownElementTypes.LIST_ITEM)) == null;
    }

    static boolean isInOuterListItem(PsiElement element) {
        return PsiUtils.parents(element).anyMatch(MarkdownPsiUtils::isOuterListItem);
    }
}
