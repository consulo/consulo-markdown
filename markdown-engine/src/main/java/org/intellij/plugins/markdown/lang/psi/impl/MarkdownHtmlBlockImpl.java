package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElement;
import org.intellij.plugins.markdown.lang.psi.MarkdownBasePresentation;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MarkdownHtmlBlockImpl extends ASTWrapperPsiElement implements MarkdownPsiElement {
    public MarkdownHtmlBlockImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new MarkdownBasePresentation() {
            @Override
            public LocalizeValue getPresentableText() {
                return LocalizeValue.localizeTODO("HTML block");
            }

            @Override
            public @Nullable String getLocationString() {
                return null;
            }
        };
    }

    @Nonnull
    @Override
    public List<MarkdownPsiElement> getCompositeChildren() {
        return Collections.emptyList();
    }
}
