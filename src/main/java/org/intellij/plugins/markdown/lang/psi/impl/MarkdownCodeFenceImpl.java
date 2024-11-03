package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.document.util.TextRange;
import consulo.language.ast.IElementType;
import consulo.language.impl.psi.CompositePsiElement;
import consulo.language.psi.ElementManipulators;
import consulo.language.psi.LiteralTextEscaper;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.navigation.ItemPresentation;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElement;
import org.intellij.plugins.markdown.structureView.MarkdownBasePresentation;

import java.util.Collections;
import java.util.List;

public class MarkdownCodeFenceImpl extends CompositePsiElement implements PsiLanguageInjectionHost, MarkdownPsiElement {
  public MarkdownCodeFenceImpl(IElementType type) {
    super(type);
  }

  @Nullable
  public String getFenceLanguage() {
    final PsiElement element = findPsiChildByType(MarkdownTokenTypes.FENCE_LANG);
    if (element == null) {
      return null;
    }
    return element.getText();
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
        return "Code fence";
      }

      @Nullable
      @Override
      public String getLocationString() {
        if (!isValid()) {
          return null;
        }

        final StringBuilder sb = new StringBuilder();
        for (PsiElement child = getFirstChild(); child != null; child = child.getNextSibling()) {
          if (!(child instanceof MarkdownCodeFenceContentImpl)) {
            continue;
          }
          if (sb.length() > 0) {
            sb.append("\\n");
          }
          sb.append(child.getText());

          if (sb.length() >= MarkdownCompositePsiElementBase.PRESENTABLE_TEXT_LENGTH) {
            break;
          }
        }

        return sb.toString();
      }
    };
  }

  @Override
  public boolean isValidHost() {
    return true;
  }

  @Override
  public PsiLanguageInjectionHost updateText(@Nonnull String text) {
    return ElementManipulators.handleContentChange(this, text);
  }

  @Nonnull
  @Override
  public LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
    return new LiteralTextEscaper<PsiLanguageInjectionHost>(this) {
      @Override
      public boolean decode(@Nonnull TextRange rangeInsideHost, @Nonnull StringBuilder outChars) {
        outChars.append(rangeInsideHost.substring(myHost.getText()));
        return true;
      }

      @Override
      public int getOffsetInHost(int offsetInDecoded, @Nonnull TextRange rangeInsideHost) {
        return rangeInsideHost.getStartOffset() + offsetInDecoded;
      }

      @Nonnull
      @Override
      public TextRange getRelevantTextRange() {
        return getContentTextRange();
      }

      public TextRange getContentTextRange() {
        final MarkdownCodeFenceContentImpl first = PsiTreeUtil.findChildOfType(myHost, MarkdownCodeFenceContentImpl.class);
        if (first == null) {
          return TextRange.EMPTY_RANGE;
        }

        MarkdownCodeFenceContentImpl last = null;
        for (PsiElement child = myHost.getLastChild(); child != null; child = child.getPrevSibling()) {
          if (child instanceof MarkdownCodeFenceContentImpl) {
            last = ((MarkdownCodeFenceContentImpl)child);
            break;
          }
        }
        assert last != null;

        return TextRange.create(first.getStartOffsetInParent(), last.getStartOffsetInParent() + last.getTextLength());
      }

      @Override
      public boolean isOneLine() {
        return false;
      }
    };
  }

  @Nonnull
  @Override
  public List<MarkdownPsiElement> getCompositeChildren() {
    return Collections.emptyList();
  }
}
