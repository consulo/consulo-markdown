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

import org.intellij.markdown.MarkdownLanguage;
import consulo.language.ast.IElementType;
import consulo.language.ast.IFileElementType;
import org.intellij.markdown.flavours.gfm.GFMElementTypes;
import org.intellij.markdown.flavours.gfm.GFMTokenTypes;


public interface MarkdownElementTypes {
  IFileElementType MARKDOWN_FILE_ELEMENT_TYPE = new IFileElementType(MarkdownLanguage.INSTANCE);

  IElementType MARKDOWN_FILE = org.intellij.markdown.MarkdownElementTypes.MARKDOWN_FILE;

  IElementType UNORDERED_LIST = org.intellij.markdown.MarkdownElementTypes.UNORDERED_LIST;

  IElementType ORDERED_LIST = org.intellij.markdown.MarkdownElementTypes.ORDERED_LIST;

  IElementType LIST_ITEM = org.intellij.markdown.MarkdownElementTypes.LIST_ITEM;

  IElementType BLOCK_QUOTE = org.intellij.markdown.MarkdownElementTypes.BLOCK_QUOTE;

  IElementType CODE_FENCE = org.intellij.markdown.MarkdownElementTypes.CODE_FENCE;

  IElementType CODE_BLOCK = org.intellij.markdown.MarkdownElementTypes.CODE_BLOCK;

  IElementType CODE_SPAN = org.intellij.markdown.MarkdownElementTypes.CODE_SPAN;

  IElementType PARAGRAPH = org.intellij.markdown.MarkdownElementTypes.PARAGRAPH;

  IElementType EMPH = org.intellij.markdown.MarkdownElementTypes.EMPH;

  IElementType STRONG = org.intellij.markdown.MarkdownElementTypes.STRONG;

  IElementType STRIKETHROUGH = GFMElementTypes.STRIKETHROUGH;

  IElementType LINK_DEFINITION = org.intellij.markdown.MarkdownElementTypes.LINK_DEFINITION;
  IElementType LINK_LABEL = org.intellij.markdown.MarkdownElementTypes.LINK_LABEL;
  IElementType LINK_DESTINATION = org.intellij.markdown.MarkdownElementTypes.LINK_DESTINATION;
  IElementType LINK_TITLE = org.intellij.markdown.MarkdownElementTypes.LINK_TITLE;
  IElementType LINK_TEXT = org.intellij.markdown.MarkdownElementTypes.LINK_TEXT;
  IElementType INLINE_LINK = org.intellij.markdown.MarkdownElementTypes.INLINE_LINK;
  IElementType FULL_REFERENCE_LINK = org.intellij.markdown.MarkdownElementTypes.FULL_REFERENCE_LINK;
  IElementType SHORT_REFERENCE_LINK = org.intellij.markdown.MarkdownElementTypes.SHORT_REFERENCE_LINK;
  IElementType IMAGE = org.intellij.markdown.MarkdownElementTypes.IMAGE;

  IElementType HTML_BLOCK = org.intellij.markdown.MarkdownElementTypes.HTML_BLOCK;

  IElementType AUTOLINK = org.intellij.markdown.MarkdownElementTypes.AUTOLINK;

  IElementType TABLE = GFMElementTypes.TABLE;
  IElementType TABLE_ROW = GFMElementTypes.ROW;
  IElementType TABLE_HEADER = GFMElementTypes.HEADER;
  IElementType TABLE_CELL = GFMTokenTypes.CELL;

  IElementType SETEXT_1 = org.intellij.markdown.MarkdownElementTypes.SETEXT_1;
  IElementType SETEXT_2 = org.intellij.markdown.MarkdownElementTypes.SETEXT_2;

  IElementType ATX_1 = org.intellij.markdown.MarkdownElementTypes.ATX_1;
  IElementType ATX_2 = org.intellij.markdown.MarkdownElementTypes.ATX_2;
  IElementType ATX_3 = org.intellij.markdown.MarkdownElementTypes.ATX_3;
  IElementType ATX_4 = org.intellij.markdown.MarkdownElementTypes.ATX_4;
  IElementType ATX_5 = org.intellij.markdown.MarkdownElementTypes.ATX_5;
  IElementType ATX_6 = org.intellij.markdown.MarkdownElementTypes.ATX_6;
}
