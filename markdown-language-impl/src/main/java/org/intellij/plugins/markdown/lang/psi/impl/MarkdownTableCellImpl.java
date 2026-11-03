package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;

public class MarkdownTableCellImpl extends MarkdownCompositePsiElementBase {
    public MarkdownTableCellImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Override
    public LocalizeValue getPresentableTagName() {
        return LocalizeValue.localizeTODO("td");
    }
}
