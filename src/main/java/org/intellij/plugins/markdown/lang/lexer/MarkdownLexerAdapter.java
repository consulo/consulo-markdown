/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.plugins.markdown.lang.lexer;

import consulo.language.ast.IElementType;
import consulo.language.lexer.LexerBase;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.markdown.lexer.MarkdownLexer;
import org.intellij.plugins.markdown.lang.MarkdownElementType;
import org.intellij.plugins.markdown.lang.parser.MarkdownParserManager;

public class MarkdownLexerAdapter extends LexerBase {
  @Nonnull
  private final MarkdownLexer delegateLexer = MarkdownParserManager.FLAVOUR.createInlinesLexer();

  private int endOffset;

  @Override
  public void start(@Nonnull CharSequence buffer, int startOffset, int endOffset, int initialState) {
    this.endOffset = endOffset;
    delegateLexer.start(buffer, startOffset, endOffset);
  }

  @Override
  public int getState() {
    return 1;
  }

  @Nullable
  @Override
  public IElementType getTokenType() {
    return MarkdownElementType.platformType(delegateLexer.getType());
  }

  @Override
  public int getTokenStart() {
    return delegateLexer.getTokenStart();
  }

  @Override
  public int getTokenEnd() {
    return delegateLexer.getTokenEnd();
  }

  @Override
  public void advance() {
    delegateLexer.advance();
  }

  @Nonnull
  @Override
  public CharSequence getBufferSequence() {
    return delegateLexer.getOriginalText();
  }

  @Override
  public int getBufferEnd() {
    return endOffset;
  }
}
