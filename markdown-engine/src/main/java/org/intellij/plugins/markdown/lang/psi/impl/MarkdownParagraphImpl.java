package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;

public class MarkdownParagraphImpl extends MarkdownCompositePsiElementBase {
    public MarkdownParagraphImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Override
    protected LocalizeValue getPresentableTagName() {
        return LocalizeValue.localizeTODO("p");
    }
}
