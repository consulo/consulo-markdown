package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.ui.ex.DelegatingItemPresentation;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets;
import org.jspecify.annotations.Nullable;

public class MarkdownHeaderImpl extends MarkdownCompositePsiElementBase {
    public MarkdownHeaderImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Override
    @RequiredReadAction
    protected LocalizeValue getPresentableTagName() {
        return LocalizeValue.localizeTODO("h" + getHeaderNumber());
    }

    @Override
    public ItemPresentation getPresentation() {
        return new DelegatingItemPresentation(super.getPresentation()) {
            @Override
            @RequiredReadAction
            public @Nullable String getLocationString() {
                if (!isValid()) {
                    return null;
                }
                PsiElement contentHolder = findChildByType(MarkdownTokenTypeSets.INLINE_HOLDING_ELEMENT_TYPES);
                return contentHolder == null ? null : contentHolder.getText();
            }
        };
    }

    @RequiredReadAction
    private int getHeaderNumber() {
        IElementType type = getNode().getElementType();
        if (MarkdownTokenTypeSets.HEADER_LEVEL_1_SET.contains(type)) {
            return 1;
        }
        if (MarkdownTokenTypeSets.HEADER_LEVEL_2_SET.contains(type)) {
            return 2;
        }
        if (type == MarkdownElementTypes.ATX_3) {
            return 3;
        }
        if (type == MarkdownElementTypes.ATX_4) {
            return 4;
        }
        if (type == MarkdownElementTypes.ATX_5) {
            return 5;
        }
        if (type == MarkdownElementTypes.ATX_6) {
            return 6;
        }
        throw new IllegalStateException("Type should be one of header types");
    }
}
