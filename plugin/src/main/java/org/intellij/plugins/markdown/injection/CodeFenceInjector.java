package org.intellij.plugins.markdown.injection;

import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.inject.MultiHostInjector;
import consulo.language.inject.MultiHostRegistrar;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.markdown.injection.LanguageGuesser;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceContentImpl;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceImpl;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

@ExtensionImpl
public class CodeFenceInjector implements MultiHostInjector {
  @Nullable
  private Language findLangForInjection(@Nonnull MarkdownCodeFenceImpl element) {
    final String fenceLanguage = element.getFenceLanguage();
    if (fenceLanguage == null) {
      return null;
    }
    return LanguageGuesser.EP_NAME.computeSafeIfAny(it -> it.guessLanguage(element, fenceLanguage));
  }

  @Nonnull
  @Override
  public Class<? extends PsiElement> getElementClass() {
    return MarkdownCodeFenceImpl.class;
  }

  @Override
  public void injectLanguages(@Nonnull MultiHostRegistrar registrar, @Nonnull PsiElement context) {
    if (!(context instanceof MarkdownCodeFenceImpl)) {
      return;
    }
    if (PsiTreeUtil.findChildOfType(context, MarkdownCodeFenceContentImpl.class) == null) {
      return;
    }

    final Language language = findLangForInjection(((MarkdownCodeFenceImpl)context));
    if (language == null) {
      return;
    }

    registrar.startInjecting(language);
    final List<MarkdownCodeFenceContentImpl> list = PsiTreeUtil.getChildrenOfTypeAsList(context, MarkdownCodeFenceContentImpl.class);
    for (int i = 0; i < list.size(); i++) {
      final MarkdownCodeFenceContentImpl content = list.get(i);
      final boolean includeEol = (i + 1 < list.size());
      final TextRange rangeInHost = TextRange.from(content.getStartOffsetInParent(), content.getTextLength() + (includeEol ? 1 : 0));
      registrar.addPlace(null, null, ((MarkdownCodeFenceImpl)context), rangeInHost);
    }
    registrar.doneInjecting();
  }
}
