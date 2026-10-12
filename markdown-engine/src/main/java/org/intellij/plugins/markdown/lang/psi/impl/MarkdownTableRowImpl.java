package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.localize.LocalizeValue;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import jakarta.annotation.Nonnull;

public class MarkdownTableRowImpl extends MarkdownCompositePsiElementBase {
    public MarkdownTableRowImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Override
    @RequiredReadAction
    protected LocalizeValue getPresentableTagName() {
        return getNode().getElementType() == MarkdownElementTypes.TABLE_HEADER
            ? LocalizeValue.localizeTODO("th")
            : LocalizeValue.localizeTODO("tr");
    }
}
