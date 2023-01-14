package org.intellij.plugins.markdown.ui.actions;

import consulo.application.ApplicationManager;
import consulo.codeEditor.Caret;
import consulo.codeEditor.Editor;
import consulo.fileEditor.FileEditor;
import consulo.fileEditor.TextEditor;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.editor.CommonDataKeys;
import consulo.language.editor.PlatformDataKeys;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.ui.ex.action.AnActionEvent;
import consulo.util.lang.Couple;
import consulo.util.lang.function.Condition;
import org.intellij.plugins.markdown.lang.MarkdownLanguage;
import org.intellij.plugins.markdown.ui.split.SplitFileEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MarkdownActionUtil {
  @Nullable
  public static SplitFileEditor findSplitEditor(AnActionEvent e) {
    final FileEditor editor = e.getData(PlatformDataKeys.FILE_EDITOR);
    if (editor instanceof SplitFileEditor) {
      return (SplitFileEditor)editor;
    }
    else {
      return SplitFileEditor.PARENT_SPLIT_KEY.get(editor);
    }
  }

  @Nullable
  public static Editor findMarkdownTextEditor(AnActionEvent e) {
    final SplitFileEditor splitEditor = findSplitEditor(e);
    if (splitEditor == null) {
      // This fallback is used primarily for testing

      final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
      if (psiFile != null && psiFile.getLanguage() == MarkdownLanguage.INSTANCE && ApplicationManager.getApplication().isUnitTestMode()) {
        return e.getData(CommonDataKeys.EDITOR);
      }
      else {
        return null;
      }
    }

    if (!(splitEditor.getMainEditor() instanceof TextEditor)) {
      return null;
    }
    final TextEditor mainEditor = (TextEditor)splitEditor.getMainEditor();
    if (!mainEditor.getComponent().isVisible()) {
      return null;
    }

    return mainEditor.getEditor();
  }

  @Nullable
  public static Couple<PsiElement> getElementsUnderCaretOrSelection(@NotNull PsiFile file, @NotNull Caret caret) {
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
  public static PsiElement getCommonParentOfType(@NotNull PsiElement element1,
                                                 @NotNull PsiElement element2,
                                                 @NotNull final IElementType elementType) {
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
