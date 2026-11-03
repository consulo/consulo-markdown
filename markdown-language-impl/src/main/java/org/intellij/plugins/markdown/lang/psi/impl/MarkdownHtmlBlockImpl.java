package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.navigation.ItemPresentationProvider;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElement;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MarkdownHtmlBlockImpl extends ASTWrapperPsiElement implements MarkdownPsiElement {
    public MarkdownHtmlBlockImpl(@Nonnull ASTNode node) {
        super(node);
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
