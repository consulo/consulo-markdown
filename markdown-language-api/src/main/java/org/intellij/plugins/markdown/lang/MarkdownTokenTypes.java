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
package org.intellij.plugins.markdown.lang;

import consulo.language.ast.IElementType;
import consulo.language.ast.StandardTokenTypes;
import consulo.language.ast.TokenType;
import org.intellij.markdown.flavours.gfm.GFMTokenTypes;


public interface MarkdownTokenTypes extends StandardTokenTypes {

  IElementType TEXT = org.intellij.markdown.MarkdownTokenTypes.TEXT;

  IElementType CODE_LINE = org.intellij.markdown.MarkdownTokenTypes.CODE_LINE;

  IElementType ATX_CONTENT = org.intellij.markdown.MarkdownTokenTypes.ATX_CONTENT;

  IElementType SETEXT_CONTENT = org.intellij.markdown.MarkdownTokenTypes.SETEXT_CONTENT;

  IElementType BLOCK_QUOTE = org.intellij.markdown.MarkdownTokenTypes.BLOCK_QUOTE;

  IElementType HTML_BLOCK_CONTENT = org.intellij.markdown.MarkdownTokenTypes.HTML_BLOCK_CONTENT;

  IElementType SINGLE_QUOTE = org.intellij.markdown.MarkdownTokenTypes.SINGLE_QUOTE;
  IElementType DOUBLE_QUOTE = org.intellij.markdown.MarkdownTokenTypes.DOUBLE_QUOTE;
  IElementType LPAREN = org.intellij.markdown.MarkdownTokenTypes.LPAREN;
  IElementType RPAREN = org.intellij.markdown.MarkdownTokenTypes.RPAREN;
  IElementType LBRACKET = org.intellij.markdown.MarkdownTokenTypes.LBRACKET;
  IElementType RBRACKET = org.intellij.markdown.MarkdownTokenTypes.RBRACKET;
  IElementType LT = org.intellij.markdown.MarkdownTokenTypes.LT;
  IElementType GT = org.intellij.markdown.MarkdownTokenTypes.GT;

  IElementType COLON = org.intellij.markdown.MarkdownTokenTypes.COLON;
  IElementType EXCLAMATION_MARK = org.intellij.markdown.MarkdownTokenTypes.EXCLAMATION_MARK;


  IElementType HARD_LINE_BREAK = org.intellij.markdown.MarkdownTokenTypes.HARD_LINE_BREAK;
  IElementType EOL = org.intellij.markdown.MarkdownTokenTypes.EOL;

  IElementType LINK_ID = org.intellij.markdown.MarkdownTokenTypes.LINK_ID;
  IElementType ATX_HEADER = org.intellij.markdown.MarkdownTokenTypes.ATX_HEADER;
  IElementType EMPH = org.intellij.markdown.MarkdownTokenTypes.EMPH;
  IElementType TILDE = GFMTokenTypes.TILDE;

  IElementType BACKTICK = org.intellij.markdown.MarkdownTokenTypes.BACKTICK;
  IElementType ESCAPED_BACKTICKS = org.intellij.markdown.MarkdownTokenTypes.ESCAPED_BACKTICKS;

  IElementType LIST_BULLET = org.intellij.markdown.MarkdownTokenTypes.LIST_BULLET;
  IElementType URL = org.intellij.markdown.MarkdownTokenTypes.URL;
  IElementType HORIZONTAL_RULE = org.intellij.markdown.MarkdownTokenTypes.HORIZONTAL_RULE;
  IElementType TABLE_SEPARATOR = GFMTokenTypes.TABLE_SEPARATOR;
  IElementType SETEXT_1 = org.intellij.markdown.MarkdownTokenTypes.SETEXT_1;
  IElementType SETEXT_2 = org.intellij.markdown.MarkdownTokenTypes.SETEXT_2;
  IElementType LIST_NUMBER = org.intellij.markdown.MarkdownTokenTypes.LIST_NUMBER;
  IElementType FENCE_LANG = org.intellij.markdown.MarkdownTokenTypes.FENCE_LANG;
  IElementType CODE_FENCE_START = org.intellij.markdown.MarkdownTokenTypes.CODE_FENCE_START;
  IElementType CODE_FENCE_END = org.intellij.markdown.MarkdownTokenTypes.CODE_FENCE_END;
  IElementType CODE_FENCE_CONTENT = org.intellij.markdown.MarkdownTokenTypes.CODE_FENCE_CONTENT;
  IElementType LINK_TITLE = org.intellij.markdown.MarkdownTokenTypes.LINK_TITLE;

  IElementType GFM_AUTOLINK = GFMTokenTypes.GFM_AUTOLINK;
  IElementType AUTOLINK = org.intellij.markdown.MarkdownTokenTypes.AUTOLINK;
  IElementType EMAIL_AUTOLINK = org.intellij.markdown.MarkdownTokenTypes.EMAIL_AUTOLINK;
  IElementType HTML_TAG = org.intellij.markdown.MarkdownTokenTypes.HTML_TAG;

  IElementType CHECK_BOX = GFMTokenTypes.CHECK_BOX;

  IElementType BAD_CHARACTER = org.intellij.markdown.MarkdownTokenTypes.BAD_CHARACTER;
  IElementType WHITE_SPACE = org.intellij.markdown.MarkdownTokenTypes.WHITE_SPACE;
}
