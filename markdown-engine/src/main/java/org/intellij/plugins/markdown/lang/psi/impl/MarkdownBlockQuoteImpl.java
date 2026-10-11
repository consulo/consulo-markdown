package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.ui.ex.DelegatingItemPresentation;
import jakarta.annotation.Nonnull;
import org.jspecify.annotations.Nullable;

public class MarkdownBlockQuoteImpl extends MarkdownCompositePsiElementBase {
    public MarkdownBlockQuoteImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Override
    protected LocalizeValue getPresentableTagName() {
        return LocalizeValue.localizeTODO("blockquote");
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
                return hasTrivialChildren() ? super.getLocationString() : null;
            }
        };
    }
}
