package org.intellij.plugins.markdown.ui.actions.styling;

import consulo.application.dumb.DumbAware;
import consulo.codeEditor.Caret;
import consulo.codeEditor.Editor;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.ast.IElementType;
import consulo.language.editor.CommonDataKeys;
import consulo.language.editor.WriteCommandAction;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.logging.Logger;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.ToggleAction;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.Couple;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.ui.actions.MarkdownActionUtil;
import jakarta.annotation.Nullable;

public abstract class BaseToggleStateAction extends ToggleAction implements DumbAware {
  private static final Logger LOG = Logger.getInstance(BaseToggleStateAction.class);

  @Nonnull
  protected abstract String getBoundString(@Nonnull CharSequence text, int selectionStart, int selectionEnd);

  @Nullable
  protected String getExistingBoundString(@Nonnull CharSequence text, int startOffset) {
    return String.valueOf(text.charAt(startOffset));
  }

  protected abstract boolean shouldMoveToWordBounds();

  @Nonnull
  protected abstract IElementType getTargetNodeType();

  @Nonnull
  protected SelectionState getCommonState(@Nonnull PsiElement element1, @Nonnull PsiElement element2) {
    return MarkdownActionUtil.getCommonParentOfType(element1,
                                                    element2,
                                                    getTargetNodeType()) == null ? SelectionState.NO : SelectionState.YES;
  }

  @Override
  public void update(@Nonnull AnActionEvent e) {
    e.getPresentation().setEnabled(MarkdownActionUtil.findMarkdownTextEditor(e) != null);
    super.update(e);
  }

  @Override
  public boolean isSelected(AnActionEvent e) {
    final Editor editor = MarkdownActionUtil.findMarkdownTextEditor(e);
    final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
    if (editor == null || psiFile == null) {
      return false;
    }

    SelectionState lastState = null;
    for (Caret caret : editor.getCaretModel().getAllCarets()) {
      final Couple<PsiElement> elements = MarkdownActionUtil.getElementsUnderCaretOrSelection(psiFile, caret);
      if (elements == null) {
        continue;
      }

      final SelectionState state = getCommonState(elements.getFirst(), elements.getSecond());
      if (lastState == null) {
        lastState = state;
      }
      else if (lastState != state) {
        lastState = SelectionState.INCONSISTENT;
        break;
      }
    }

    if (lastState == SelectionState.INCONSISTENT) {
      e.getPresentation().setEnabled(false);
      return false;
    }
    else {
      e.getPresentation().setEnabled(true);
      return lastState == SelectionState.YES;
    }
  }

  @Override
  public void setSelected(AnActionEvent e, final boolean state) {
    final Editor editor = MarkdownActionUtil.findMarkdownTextEditor(e);
    final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
    if (editor == null || psiFile == null) {
      return;
    }


    WriteCommandAction.runWriteCommandAction(psiFile.getProject(), new Runnable() {
      @Override
      public void run() {
        if (!psiFile.isValid()) {
          return;
        }

        final Document document = editor.getDocument();
        for (Caret caret : ContainerUtil.reverse(editor.getCaretModel().getAllCarets())) {
          if (!state) {
            final Couple<PsiElement> elements = MarkdownActionUtil.getElementsUnderCaretOrSelection(psiFile, caret);
            if (elements == null) {
              continue;
            }

            final PsiElement closestEmph = MarkdownActionUtil.getCommonParentOfType(elements.getFirst(),
                                                                                    elements.getSecond(),
                                                                                    BaseToggleStateAction.this.getTargetNodeType());
            if (closestEmph == null) {
              LOG.warn("Could not find enclosing element on its destruction");
              continue;
            }

            final TextRange range = closestEmph.getTextRange();
            BaseToggleStateAction.this.removeEmphFromSelection(document, caret, range);
          }
          else {
            BaseToggleStateAction.this.addEmphToSelection(document, caret);
          }
        }

        PsiDocumentManager.getInstance(psiFile.getProject()).commitDocument(document);
      }
    });
  }

  public void removeEmphFromSelection(@Nonnull Document document, @Nonnull Caret caret, @Nonnull TextRange nodeRange) {
    final CharSequence text = document.getCharsSequence();
    final String boundString = getExistingBoundString(text, nodeRange.getStartOffset());
    if (boundString == null) {
      LOG.warn("Could not fetch bound string from found node");
      return;
    }
    final int boundLength = boundString.length();

    // Easy case --- selection corresponds to some emph
    if (nodeRange.getStartOffset() + boundLength == caret.getSelectionStart() && nodeRange.getEndOffset() - boundLength == caret.getSelectionEnd()) {
      document.deleteString(nodeRange.getEndOffset() - boundLength, nodeRange.getEndOffset());
      document.deleteString(nodeRange.getStartOffset(), nodeRange.getStartOffset() + boundLength);
      return;
    }


    int from = caret.getSelectionStart();
    int to = caret.getSelectionEnd();

    if (shouldMoveToWordBounds()) {
      while (from - boundLength > nodeRange.getStartOffset() && Character.isWhitespace(text.charAt(from - 1))) {
        from--;
      }
      while (to + boundLength < nodeRange.getEndOffset() && Character.isWhitespace(text.charAt(to))) {
        to++;
      }
    }

    if (to + boundLength == nodeRange.getEndOffset()) {
      document.deleteString(nodeRange.getEndOffset() - boundLength, nodeRange.getEndOffset());
    }
    else {
      document.insertString(to, boundString);
    }

    if (from - boundLength == nodeRange.getStartOffset()) {
      document.deleteString(nodeRange.getStartOffset(), nodeRange.getStartOffset() + boundLength);
    }
    else {
      document.insertString(from, boundString);
    }
  }

  public void addEmphToSelection(@Nonnull Document document, @Nonnull Caret caret) {
    int from = caret.getSelectionStart();
    int to = caret.getSelectionEnd();

    final CharSequence text = document.getCharsSequence();

    if (shouldMoveToWordBounds()) {
      while (from < to && Character.isWhitespace(text.charAt(from))) {
        from++;
      }
      while (to > from && Character.isWhitespace(text.charAt(to - 1))) {
        to--;
      }

      if (from == to) {
        from = caret.getSelectionStart();
        to = caret.getSelectionEnd();
      }
    }

    final String boundString = getBoundString(text, from, to);
    document.insertString(to, boundString);
    document.insertString(from, boundString);

    if (caret.getSelectionStart() == caret.getSelectionEnd()) {
      caret.moveCaretRelatively(boundString.length(), 0, false, false);
    }
  }

  protected enum SelectionState {
    YES,
    NO,
    INCONSISTENT
  }
}
