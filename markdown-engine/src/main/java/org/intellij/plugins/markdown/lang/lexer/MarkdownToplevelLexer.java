package org.intellij.plugins.markdown.lang.lexer;

import org.intellij.markdown.flavours.MarkdownFlavourDescriptor;
import org.intellij.markdown.parser.LexemeSink;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.markdown.parser.MarkdownParserManager;
import consulo.language.ast.IElementType;
import consulo.language.lexer.LexerBase;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.markdown.parser.LexemeSink;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.markdown.parser.MarkdownParserManager;

import java.util.ArrayList;
import java.util.List;

public class MarkdownToplevelLexer extends LexerBase {
  private final MarkdownFlavourDescriptor myFlavour;
  private CharSequence myBuffer;
  private int myBufferStart;
  private int myBufferEnd;

  private List<IElementType> myLexemes;
  private List<Integer> myStartOffsets;
  private List<Integer> myEndOffsets;

  private int myLexemeIndex;

  public MarkdownToplevelLexer() {
    this(MarkdownParserManager.FLAVOUR);
  }

  /**
   * The lexer of a Markdown dialect with its own block parser (MDX).
   */
  public MarkdownToplevelLexer(@Nonnull MarkdownFlavourDescriptor flavour) {
    myFlavour = flavour;
  }

  @Override
  public void start(@Nonnull CharSequence buffer, int startOffset, int endOffset, int initialState) {
    myBuffer = buffer;
    myBufferStart = startOffset;
    myBufferEnd = endOffset;

    final CharSequence text = buffer.subSequence(startOffset, endOffset);
    final LexemeSink sink = new LexemeSink();
    new MarkdownParser(myFlavour)
      .walkBlocks(MarkdownParserManager.parseBlocks(text, myFlavour), text, false, sink);

    myLexemes = sink.getTypes();
    myStartOffsets = sink.getStarts();
    myEndOffsets = sink.getEnds();
    myLexemeIndex = 0;
  }

  @Override
  public int getState() {
    return myLexemeIndex;
  }

  @Nullable
  @Override
  public IElementType getTokenType() {
    if (myLexemeIndex >= myLexemes.size()) {
      return null;
    }
    return myLexemes.get(myLexemeIndex);
  }

  @Override
  public int getTokenStart() {
    if (myLexemeIndex >= myLexemes.size()) {
      return myBufferEnd;
    }
    return myBufferStart + myStartOffsets.get(myLexemeIndex);
  }

  @Override
  public int getTokenEnd() {
    if (myLexemeIndex >= myLexemes.size()) {
      return myBufferEnd;
    }
    return myBufferStart + myEndOffsets.get(myLexemeIndex);
  }

  @Override
  public void advance() {
    myLexemeIndex++;
  }

  @Nonnull
  @Override
  public CharSequence getBufferSequence() {
    return myBuffer;
  }

  @Override
  public int getBufferEnd() {
    return myBufferEnd;
  }

}
