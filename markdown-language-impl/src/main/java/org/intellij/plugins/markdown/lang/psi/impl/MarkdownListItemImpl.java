package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;
import org.jspecify.annotations.Nullable;

public class MarkdownListItemImpl extends MarkdownCompositePsiElementBase {
    public MarkdownListItemImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @RequiredReadAction
    public @Nullable PsiElement getMarkerElement() {
        PsiElement child = getFirstChild();
        return child != null && MarkdownTokenTypeSets.LIST_MARKERS.contains(child.getNode().getElementType()) ? child : null;
    }

    @RequiredReadAction
    public @Nullable PsiElement getCheckBox() {
        PsiElement markerElement = getMarkerElement();
        if (markerElement == null) {
            return null;
        }
        PsiElement candidate = markerElement.getNextSibling();
        return candidate != null && candidate.getNode().getElementType() == MarkdownTokenTypes.CHECK_BOX ? candidate : null;
    }

    @Override
    public LocalizeValue getPresentableTagName() {
        return LocalizeValue.localizeTODO("li");
    }
}
