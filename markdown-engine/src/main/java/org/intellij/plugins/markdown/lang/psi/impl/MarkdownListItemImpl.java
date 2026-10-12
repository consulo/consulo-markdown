package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.colorScheme.TextAttributesKey;
import consulo.execution.process.ConsoleHighlighter;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.ui.ex.ColoredItemPresentation;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;
import org.intellij.plugins.markdown.lang.psi.MarkdownBasePresentation;
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
    public ItemPresentation getPresentation() {
        return new MyItemPresentation();
    }

    @Override
    protected LocalizeValue getPresentableTagName() {
        return LocalizeValue.localizeTODO("li");
    }

    private class MyItemPresentation extends MarkdownBasePresentation implements ColoredItemPresentation {
        @Override
        @RequiredReadAction
        public LocalizeValue getPresentableText() {
            if (!isValid()) {
                return LocalizeValue.empty();
            }
            PsiElement markerElement = getMarkerElement();
            if (markerElement == null) {
                return LocalizeValue.empty();
            }
            return LocalizeValue.of(markerElement.getText());
        }

        @Override
        @RequiredReadAction
        public @Nullable String getLocationString() {
            if (!isValid()) {
                return null;
            }

            if (hasTrivialChildren()) {
                MarkdownCompositePsiElementBase element = findChildByClass(MarkdownCompositePsiElementBase.class);
                assert element != null;
                return element.shrinkTextTo(PRESENTABLE_TEXT_LENGTH);
            }
            else {
                return null;
            }
        }

        @Override
        public @Nullable Image getIcon() {
            return null;
        }

        @Override
        @RequiredReadAction
        public @Nullable TextAttributesKey getTextAttributesKey() {
            PsiElement checkBox = getCheckBox();
            if (checkBox == null) {
                return null;
            }
            return checkBox.textContains('x') || checkBox.textContains('X') ? ConsoleHighlighter.GREEN : ConsoleHighlighter.RED;
        }
    }
}
