package org.intellij.plugins.markdown.lang.parser;

import consulo.language.parser.PsiBuilder;
import jakarta.annotation.Nonnull;
import org.intellij.markdown.ast.ASTNode;
import org.intellij.markdown.ast.LeafASTNode;
import org.intellij.markdown.ast.visitors.RecursiveVisitor;
import org.intellij.plugins.markdown.lang.MarkdownElementType;

public class PsiBuilderFillingVisitor extends RecursiveVisitor {
  @Nonnull
  private final PsiBuilder builder;

  public PsiBuilderFillingVisitor(@Nonnull PsiBuilder builder) {
    this.builder = builder;
  }

  @Override
  public void visitNode(@Nonnull ASTNode node) {
    if (node instanceof LeafASTNode) {
      return;
    }

    ensureBuilderInPosition(node.getStartOffset());
    final PsiBuilder.Marker marker = builder.mark();

    super.visitNode(node);

    ensureBuilderInPosition(node.getEndOffset());
    marker.done(MarkdownElementType.platformType(node.getType()));
  }

  private void ensureBuilderInPosition(int position) {
    while (builder.getCurrentOffset() < position) {
      builder.advanceLexer();
    }

    if (builder.getCurrentOffset() != position) {
      throw new AssertionError("parsed tree and lexer are unsynchronized");
    }
  }
}
