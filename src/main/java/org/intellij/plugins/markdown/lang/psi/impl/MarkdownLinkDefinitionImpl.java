package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiElement;
import consulo.navigation.ItemPresentation;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElement;
import org.intellij.plugins.markdown.structureView.MarkdownBasePresentation;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collections;
import java.util.List;

public class MarkdownLinkDefinitionImpl extends ASTWrapperPsiElement implements MarkdownPsiElement {
  public MarkdownLinkDefinitionImpl(@Nonnull ASTNode node) {
    super(node);
  }

  @Nonnull
  public PsiElement getLinkLabel() {
    final PsiElement label = findChildByType(MarkdownElementTypes.LINK_LABEL);
    if (label == null) {
      throw new IllegalStateException("Probably parsing failed. Should have a label");
    }
    return label;
  }

  @Nonnull
  public PsiElement getLinkDestination() {
    final PsiElement destination = findChildByType(MarkdownElementTypes.LINK_DESTINATION);
    if (destination == null) {
      throw new IllegalStateException("Probably parsing failed. Should have a destination");
    }
    return destination;
  }

  @Nullable
  public PsiElement getLinkTitle() {
    return findChildByType(MarkdownElementTypes.LINK_TITLE);
  }

  @Override
  public ItemPresentation getPresentation() {
    return new MarkdownBasePresentation() {
      @Nullable
      @Override
      public String getPresentableText() {
        if (!isValid()) {
          return null;
        }

        return "Def: " + getLinkLabel().getText() + " → " + getLinkDestination().getText();
      }

      @Nullable
      @Override
      public String getLocationString() {
        if (!isValid()) {
          return null;
        }

        final PsiElement linkTitle = getLinkTitle();
        if (linkTitle == null) {
          return null;
        }
        else {
          return linkTitle.getText();
        }
      }
    };
  }

  @Nonnull
  @Override
  public List<MarkdownPsiElement> getCompositeChildren() {
    return Collections.emptyList();
  }
}
