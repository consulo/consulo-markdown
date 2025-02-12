package org.intellij.plugins.markdown.lang.lexer;

import consulo.language.ast.IElementType;
import consulo.language.lexer.LexerBase;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.markdown.ast.ASTNode;
import org.intellij.markdown.ast.ASTNodeKt;
import org.intellij.markdown.ast.visitors.RecursiveVisitor;
import org.intellij.plugins.markdown.lang.MarkdownElementType;
import org.intellij.plugins.markdown.lang.parser.MarkdownParserManager;

import java.util.ArrayList;
import java.util.List;

public class MarkdownToplevelLexer extends LexerBase {
  private CharSequence myBuffer;
  private int myBufferStart;
  private int myBufferEnd;

  private List<IElementType> myLexemes;
  private List<Integer> myStartOffsets;
  private List<Integer> myEndOffsets;

  private int myLexemeIndex;

  @Override
  public void start(@Nonnull CharSequence buffer, int startOffset, int endOffset, int initialState) {
    myBuffer = buffer;
    myBufferStart = startOffset;
    myBufferEnd = endOffset;

    final ASTNode parsedTree = MarkdownParserManager.parseContent(buffer.subSequence(startOffset, endOffset));
    myLexemes = new ArrayList<IElementType>();
    myStartOffsets = new ArrayList<Integer>();
    myEndOffsets = new ArrayList<Integer>();
    ASTNodeKt.accept(parsedTree, new LexerBuildingVisitor());
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

  private class LexerBuildingVisitor extends RecursiveVisitor {

    @Override
    public void visitNode(@Nonnull ASTNode node) {
      if (node.getStartOffset() == node.getEndOffset()) {
        return;
      }
      final List<ASTNode> children = node.getChildren();
      if (children.isEmpty()) {
        myLexemes.add(MarkdownElementType.platformType(node.getType()));
        myStartOffsets.add(node.getStartOffset());
        myEndOffsets.add(node.getEndOffset());
      }
      else {
        super.visitNode(node);
      }
    }
  }
}
