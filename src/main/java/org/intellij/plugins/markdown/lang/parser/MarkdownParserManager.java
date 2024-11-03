package org.intellij.plugins.markdown.lang.parser;

import jakarta.annotation.Nonnull;
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.ast.ASTNode;
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor;
import org.intellij.markdown.parser.MarkdownParser;

public class MarkdownParserManager {
  public static final GFMFlavourDescriptor FLAVOUR = new GFMFlavourDescriptor();

  private static final ThreadLocal<ParsingInfo> ourLastParsingResult = new ThreadLocal<ParsingInfo>();

  public static ASTNode parseContent(@Nonnull CharSequence buffer) {
    final ParsingInfo info = ourLastParsingResult.get();
    if (info != null && info.myBufferHash == buffer.hashCode() && info.myBuffer.equals(buffer)) {
      return info.myParseResult;
    }

    final ASTNode parseResult = new MarkdownParser(FLAVOUR)
      .parse(MarkdownElementTypes.MARKDOWN_FILE, buffer.toString(), false);
    ourLastParsingResult.set(new ParsingInfo(buffer, parseResult));
    return parseResult;
  }

  private static class ParsingInfo {
    @Nonnull
    final CharSequence myBuffer;
    final int myBufferHash;
    @Nonnull
    final ASTNode myParseResult;

    public ParsingInfo(@Nonnull CharSequence buffer, @Nonnull ASTNode parseResult) {
      myBuffer = buffer;
      myBufferHash = myBuffer.hashCode();
      myParseResult = parseResult;
    }
  }
}
