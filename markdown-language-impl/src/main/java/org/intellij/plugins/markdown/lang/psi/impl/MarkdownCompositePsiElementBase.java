package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.navigation.ItemPresentationProvider;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElement;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class MarkdownCompositePsiElementBase extends ASTWrapperPsiElement implements MarkdownPsiElement {
    public static final int PRESENTABLE_TEXT_LENGTH = 50;

    public MarkdownCompositePsiElementBase(@Nonnull ASTNode node) {
        super(node);
    }

    public abstract LocalizeValue getPresentableTagName();

    @Nonnull
    @RequiredReadAction
    protected CharSequence getChars() {
        return getTextRange().subSequence(getContainingFile().getViewProvider().getContents());
    }

    @Nonnull
    @RequiredReadAction
    public String shrinkTextTo(int length) {
        final CharSequence chars = getChars();
        return chars.subSequence(0, Math.min(length, chars.length())).toString();
    }

    @Nonnull
    @Override
    @RequiredReadAction
    public List<MarkdownPsiElement> getCompositeChildren() {
        return Arrays.asList(findChildrenByClass(MarkdownPsiElement.class));
    }

    /**
     * @return <code>true</code> if there is more than one composite child
     * OR there is one child which is not a paragraph, <code>false</code> otherwise.
     */
    @RequiredReadAction
    public boolean hasTrivialChildren() {
        Collection<MarkdownPsiElement> children = getCompositeChildren();
        return children.size() == 1 && children.iterator().next() instanceof MarkdownParagraphImpl;
    }

    @Override
    public ItemPresentation getPresentation() {
        return ItemPresentationProvider.getItemPresentation(this);
    }
}
