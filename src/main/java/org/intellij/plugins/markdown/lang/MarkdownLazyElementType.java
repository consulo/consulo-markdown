package org.intellij.plugins.markdown.lang;

import consulo.language.ast.ASTNode;
import consulo.language.ast.ILazyParseableElementType;
import consulo.language.lexer.Lexer;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiBuilderFactory;
import consulo.language.psi.PsiElement;
import consulo.language.version.LanguageVersionUtil;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.plugins.markdown.lang.lexer.MarkdownLexerAdapter;
import org.intellij.plugins.markdown.lang.parser.MarkdownParserManager;
import org.intellij.plugins.markdown.lang.parser.PsiBuilderFillingVisitor;
import org.jetbrains.annotations.NonNls;

public class MarkdownLazyElementType extends ILazyParseableElementType {
  public MarkdownLazyElementType(@Nonnull @NonNls String debugName) {
    super(debugName, MarkdownLanguage.INSTANCE);
  }

  @Override
  protected ASTNode doParseContents(@Nonnull ASTNode chameleon, @Nonnull PsiElement psi) {
    final Project project = psi.getProject();
    final Lexer lexer = new MarkdownLexerAdapter();
    final CharSequence chars = chameleon.getChars();

    final org.intellij.markdown.ast.ASTNode node =
      new MarkdownParser(MarkdownParserManager.FLAVOUR).parseInline(MarkdownElementType.markdownType(chameleon.getElementType()), chars, 0,
                                                                    chars.length());

    final PsiBuilder builder = PsiBuilderFactory.getInstance()
                                                .createBuilder(project,
                                                               chameleon,
                                                               lexer,
                                                               getLanguage(),
                                                               LanguageVersionUtil.findDefaultVersion(getLanguage()),
                                                               chars);
    assert builder.getCurrentOffset() == 0;
    new PsiBuilderFillingVisitor(builder).visitNode(node);
    assert builder.eof();

    return builder.getTreeBuilt().getFirstChildNode();
  }
}
