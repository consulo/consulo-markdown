package org.intellij.plugins.markdown.ui.actions;

import consulo.codeEditor.Caret;
import consulo.codeEditor.Editor;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.editor.CommonDataKeys;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.ui.ex.action.AnActionEvent;
import consulo.util.lang.Couple;
import consulo.util.lang.function.Condition;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.plugins.markdown.lang.MarkdownLanguage;

public class MarkdownActionUtil {
  @Nullable
  public static Editor findMarkdownTextEditor(AnActionEvent e) {
    final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
    if (psiFile != null && psiFile.getLanguage() == MarkdownLanguage.INSTANCE) {
      return e.getData(CommonDataKeys.EDITOR);
    }
    return null;
  }

  @Nullable
  public static Couple<PsiElement> getElementsUnderCaretOrSelection(@Nonnull PsiFile file, @Nonnull Caret caret) {
    if (caret.getSelectionStart() == caret.getSelectionEnd()) {
      final PsiElement element = file.findElementAt(caret.getSelectionStart());
      if (element == null) {
        return null;
      }
      return Couple.of(element, element);
    }
    else {
      final PsiElement startElement = file.findElementAt(caret.getSelectionStart());
      final PsiElement endElement = file.findElementAt(caret.getSelectionEnd());
      if (startElement == null || endElement == null) {
        return null;
      }
      return Couple.of(startElement, endElement);
    }
  }

  @Nullable
  public static PsiElement getCommonParentOfType(@Nonnull PsiElement element1,
                                                 @Nonnull PsiElement element2,
                                                 @Nonnull final IElementType elementType) {
    final PsiElement base = PsiTreeUtil.findCommonParent(element1, element2);
    return PsiTreeUtil.findFirstParent(base, false, new Condition<PsiElement>() {
      @Override
      public boolean value(PsiElement element) {
        final ASTNode node = element.getNode();
        return node != null && node.getElementType() == elementType;
      }
    });
  }
}
