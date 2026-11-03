// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package consulo.markdown.documentation.flavour;

import consulo.language.ast.IElementType;
import consulo.util.lang.StringUtil;
import org.intellij.markdown.MarkdownElementType;
import org.intellij.markdown.MarkdownTokenTypes;
import org.intellij.markdown.parser.sequentialparsers.IntRange;
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;
import org.intellij.markdown.parser.sequentialparsers.TokensCache;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Inline parser dedicated to finding {@code <code>} tags.
 * <p>
 * Only purpose is to exclude the range from further markdown processing.
 * A flavour using it renders {@link #RAW_TYPE} as is, for instance with a {@code TransparentInlineHolderProvider}.
 */
public class CodeTagParser implements SequentialParser {
  /**
   * The type of a {@code <code>} range, which is not processed as markdown
   */
  public static final IElementType RAW_TYPE = new MarkdownElementType("RAW_TYPE");

  @Override
  public ParsingResult parse(TokensCache tokens, List<IntRange> rangesToGlue) {
    var result = new SequentialParser.ParsingResultBuilder();
    var delegateIndices = new RangesListBuilder();
    TokensCache.Iterator iterator = tokens.new RangesListIterator(rangesToGlue);

    while (iterator.getType() != null) {
      if (iterator.getType() == MarkdownTokenTypes.HTML_TAG && hasOpeningTag(getText(tokens, iterator), "code")) {
        int startIndex = iterator.getIndex();
        var endIterator = findEnd(tokens, iterator.advance());

        if (endIterator != null) {
          result.withNode(new SequentialParser.Node(startIndex, endIterator.getIndex() + 1, RAW_TYPE));
          iterator = endIterator.advance();
          continue;
        }
      }

      delegateIndices.put(iterator.getIndex());
      iterator = iterator.advance();
    }

    return result.withFurtherProcessing(delegateIndices.get());
  }

  /**
   * @return The iterator located at the end tag, or {@code null} if the end isn't found
   */
  private static TokensCache.@Nullable Iterator findEnd(TokensCache tokens, TokensCache.Iterator iterator) {
    while (iterator.getType() != null) {
      if (iterator.getType() == MarkdownTokenTypes.HTML_TAG) {
        if (hasClosingTag(getText(tokens, iterator), "code")) {
          return iterator;
        }
      }

      iterator = iterator.advance();
    }
    return null;
  }

  private static String getText(TokensCache tokens, TokensCache.Iterator iterator) {
    return tokens.getOriginalText().subSequence(iterator.getStart(), iterator.getEnd()).toString();
  }

  private static boolean hasOpeningTag(String tag, String tagName) {
    return StringUtil.trimEnd(StringUtil.trimStart(tag, "<"), ">").equals(tagName);
  }

  private static boolean hasClosingTag(String tag, String tagName) {
    return StringUtil.trimEnd(StringUtil.trimStart(tag, "</"), ">").equals(tagName);
  }
}
