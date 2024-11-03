package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.navigation.ItemPresentation;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElement;
import org.intellij.plugins.markdown.structureView.MarkdownBasePresentation;
import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class MarkdownCompositePsiElementBase extends ASTWrapperPsiElement implements MarkdownPsiElement {
  public static final int PRESENTABLE_TEXT_LENGTH = 50;

  public MarkdownCompositePsiElementBase(@Nonnull ASTNode node) {
    super(node);
  }

  protected abstract String getPresentableTagName();

  @Nonnull
  protected CharSequence getChars() {
    return getTextRange().subSequence(getContainingFile().getViewProvider().getContents());
  }

  @Nonnull
  protected String shrinkTextTo(int length) {
    final CharSequence chars = getChars();
    return chars.subSequence(0, Math.min(length, chars.length())).toString();
  }

  @Nonnull
  @Override
  public List<MarkdownPsiElement> getCompositeChildren() {
    return Arrays.asList(findChildrenByClass(MarkdownPsiElement.class));
  }

  /**
   * @return <code>true</code> if there is more than one composite child
   * OR there is one child which is not a paragraph, <code>false</code> otherwise.
   */
  public boolean hasTrivialChildren() {
    final Collection<MarkdownPsiElement> children = getCompositeChildren();
    if (children.size() != 1) {
      return false;
    }
    return children.iterator().next() instanceof MarkdownParagraphImpl;
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
        return getPresentableTagName();
      }

      @Nullable
      @Override
      public String getLocationString() {
        if (!isValid()) {
          return null;
        }
        if (getCompositeChildren().size() == 0) {
          return shrinkTextTo(PRESENTABLE_TEXT_LENGTH);
        }
        else {
          return null;
        }
      }
    };
  }
}
