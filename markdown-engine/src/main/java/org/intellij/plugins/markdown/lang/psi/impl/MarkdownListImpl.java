package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;

public class MarkdownListImpl extends MarkdownCompositePsiElementBase {
    public MarkdownListImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Override
    @RequiredReadAction
    protected LocalizeValue getPresentableTagName() {
        return getNode().getElementType() == MarkdownElementTypes.ORDERED_LIST
            ? LocalizeValue.localizeTODO("ol")
            : LocalizeValue.localizeTODO("ul");
    }
}
