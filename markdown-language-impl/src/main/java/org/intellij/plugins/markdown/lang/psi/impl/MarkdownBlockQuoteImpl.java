package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;
import org.jspecify.annotations.Nullable;

public class MarkdownBlockQuoteImpl extends MarkdownCompositePsiElementBase {
    public MarkdownBlockQuoteImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Override
    public LocalizeValue getPresentableTagName() {
        return LocalizeValue.localizeTODO("blockquote");
    }
}
