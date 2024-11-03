package org.intellij.plugins.markdown.structureView;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.fileEditor.structureView.StructureViewBuilder;
import consulo.fileEditor.structureView.StructureViewModel;
import consulo.fileEditor.structureView.TreeBasedStructureViewBuilder;
import consulo.language.Language;
import consulo.language.editor.structureView.PsiStructureViewFactory;
import consulo.language.editor.structureView.StructureViewModelBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import jakarta.annotation.Nullable;
import org.intellij.plugins.markdown.lang.MarkdownLanguage;
import jakarta.annotation.Nonnull;

@ExtensionImpl
public class MarkdownStructureViewFactory implements PsiStructureViewFactory {
  @Nullable
  @Override
  public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
    return new TreeBasedStructureViewBuilder() {
      @Nonnull
      @Override
      public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
        return new MarkdownStructureViewModel(psiFile, editor);
      }
    };
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return MarkdownLanguage.INSTANCE;
  }

  private static class MarkdownStructureViewModel extends StructureViewModelBase {
    public MarkdownStructureViewModel(@Nonnull PsiFile psiFile, @Nullable Editor editor) {
      super(psiFile, new MarkdownStructureElement(psiFile));
    }

    @Override
    protected boolean isSuitable(PsiElement element) {
      if (element == null) {
        return false;
      }
      if (!MarkdownStructureElement.PRESENTABLE_TYPES.contains(element.getNode().getElementType())) {
        return false;
      }
      final PsiElement parent = element.getParent();
      if (MarkdownStructureElement.hasTrivialChild(parent)) {
        return false;
      }
      return true;
    }
  }
}
