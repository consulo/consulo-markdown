package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElement;
import org.intellij.plugins.markdown.lang.psi.MarkdownBasePresentation;
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

    @Override
    public ItemPresentation getPresentation() {
        return new MarkdownBasePresentation() {
            @Override
            @RequiredReadAction
            public LocalizeValue getPresentableText() {
                if (!isValid()) {
                    return LocalizeValue.empty();
                }

                return LocalizeValue.localizeTODO("Def: " + getLinkLabel().getText() + " → " + getLinkDestination().getText());
            }

            @Override
            @RequiredReadAction
            public @Nullable String getLocationString() {
                if (!isValid()) {
                    return null;
                }

                PsiElement linkTitle = getLinkTitle();
                return linkTitle == null ? null : linkTitle.getText();
            }
        };
    }

    @Nonnull
    @Override
    public List<MarkdownPsiElement> getCompositeChildren() {
        return Collections.emptyList();
    }
}
