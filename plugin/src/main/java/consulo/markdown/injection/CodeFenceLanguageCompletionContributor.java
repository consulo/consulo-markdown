package consulo.markdown.injection;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.completion.CompletionContributor;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionProvider;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.CompletionType;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.util.ProcessingContext;
import jakarta.annotation.Nonnull;
import org.intellij.markdown.MarkdownLanguage;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;

/**
 * Completes the language name of the info string after the opening backticks of a code fence with the variants of all
 * {@link LanguageGuesser}s.
 */
@ExtensionImpl
public class CodeFenceLanguageCompletionContributor extends CompletionContributor {
  public CodeFenceLanguageCompletionContributor() {
    extend(CompletionType.BASIC, PlatformPatterns.psiElement(MarkdownTokenTypes.FENCE_LANG), new CompletionProvider() {
      @Override
      public void addCompletions(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        int prefixLength = parameters.getOffset() - position.getTextRange().getStartOffset();
        if (prefixLength < 0 || prefixLength > position.getTextLength()) {
          return;
        }

        String prefix = position.getText().substring(0, prefixLength);
        // only the language name is completed, not the attributes after it
        if (!LanguageGuesser.getLanguageName(prefix).equals(prefix.trim())) {
          return;
        }

        CompletionResultSet resultSet = result.withPrefixMatcher(prefix.trim());
        position.getProject().getApplication().getExtensionPoint(LanguageGuesser.class)
          .forEachExtensionSafe(guesser -> resultSet.addAllElements(guesser.getCompletionVariants(parameters)));
        resultSet.stopHere();
      }
    });
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return MarkdownLanguage.INSTANCE;
  }
}
