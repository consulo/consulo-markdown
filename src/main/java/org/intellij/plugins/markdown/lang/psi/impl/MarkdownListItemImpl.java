package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.colorScheme.TextAttributesKey;
import consulo.execution.process.ConsoleHighlighter;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.navigation.ItemPresentation;
import consulo.ui.ex.ColoredItemPresentation;
import consulo.ui.image.Image;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;
import org.intellij.plugins.markdown.structureView.MarkdownBasePresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MarkdownListItemImpl extends MarkdownCompositePsiElementBase {
  public MarkdownListItemImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Nullable
  public PsiElement getMarkerElement() {
    final PsiElement child = getFirstChild();
    if (child != null && MarkdownTokenTypeSets.LIST_MARKERS.contains(child.getNode().getElementType())) {
      return child;
    }
    else {
      return null;
    }
  }

  @Nullable
  public PsiElement getCheckBox() {
    final PsiElement markerElement = getMarkerElement();
    if (markerElement == null) {
      return null;
    }
    final PsiElement candidate = markerElement.getNextSibling();
    if (candidate != null && candidate.getNode().getElementType() == MarkdownTokenTypes.CHECK_BOX) {
      return candidate;
    }
    else {
      return null;
    }
  }

  @Override
  public ItemPresentation getPresentation() {
    return new MyItemPresentation();
  }

  @Override
  protected String getPresentableTagName() {
    return "li";
  }

  private class MyItemPresentation extends MarkdownBasePresentation implements ColoredItemPresentation {
    @Nullable
    @Override
    public String getPresentableText() {
      if (!isValid()) {
        return null;
      }
      final PsiElement markerElement = getMarkerElement();
      if (markerElement == null) {
        return null;
      }
      return markerElement.getText();
    }

    @Nullable
    @Override
    public String getLocationString() {
      if (!isValid()) {
        return null;
      }

      if (hasTrivialChildren()) {
        final MarkdownCompositePsiElementBase element = findChildByClass(MarkdownCompositePsiElementBase.class);
        assert element != null;
        return element.shrinkTextTo(PRESENTABLE_TEXT_LENGTH);
      }
      else {
        return null;
      }
    }

    @Nullable
    @Override
    public Image getIcon() {
      return null;
    }

    @Nullable
    @Override
    public TextAttributesKey getTextAttributesKey() {
      final PsiElement checkBox = getCheckBox();
      if (checkBox == null) {
        return null;
      }
      if (checkBox.textContains('x') || checkBox.textContains('X')) {
        return ConsoleHighlighter.GREEN;
      }
      else {
        return ConsoleHighlighter.RED;
      }
    }
  }
}
