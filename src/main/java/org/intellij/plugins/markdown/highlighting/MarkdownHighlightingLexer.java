package org.intellij.plugins.markdown.highlighting;

import consulo.language.ast.IElementType;
import consulo.language.lexer.LayeredLexer;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets;
import org.intellij.plugins.markdown.lang.lexer.MarkdownLexerAdapter;
import org.intellij.plugins.markdown.lang.lexer.MarkdownToplevelLexer;

public class MarkdownHighlightingLexer extends LayeredLexer {
  public MarkdownHighlightingLexer() {
    super(new MarkdownToplevelLexer());

    registerSelfStoppingLayer(new MarkdownLexerAdapter(), MarkdownTokenTypeSets.INLINE_HOLDING_ELEMENT_TYPES.getTypes(),
                              IElementType.EMPTY_ARRAY);
  }
}
