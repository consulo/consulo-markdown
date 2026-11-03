package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.navigation.ItemPresentationProvider;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElement;
import jakarta.annotation.Nonnull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MarkdownLinkDefinitionImpl extends ASTWrapperPsiElement implements MarkdownPsiElement {
    public MarkdownLinkDefinitionImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Nonnull
    @RequiredReadAction
    public PsiElement getLinkLabel() {
        PsiElement label = findChildByType(MarkdownElementTypes.LINK_LABEL);
        if (label == null) {
            throw new IllegalStateException("Probably parsing failed. Should have a label");
        }
        return label;
    }

    @Nonnull
    @RequiredReadAction
    public PsiElement getLinkDestination() {
        PsiElement destination = findChildByType(MarkdownElementTypes.LINK_DESTINATION);
        if (destination == null) {
            throw new IllegalStateException("Probably parsing failed. Should have a destination");
        }
        return destination;
    }

    @RequiredReadAction
    public @Nullable PsiElement getLinkTitle() {
        return findChildByType(MarkdownElementTypes.LINK_TITLE);
    }

    @Nonnull
    @Override
    public List<MarkdownPsiElement> getCompositeChildren() {
        return Collections.emptyList();
    }

    @Override
    public ItemPresentation getPresentation() {
        return ItemPresentationProvider.getItemPresentation(this);
    }
}
