/*
 * Copyright 2016 JetBrains s.r.o.
 * Copyright 2013-2026 consulo.io
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
package org.intellij.markdown.parser.sequentialparsers.impl;

import consulo.language.ast.IElementType;
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.MarkdownTokenTypes;
import org.intellij.markdown.parser.sequentialparsers.IntRange;
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil;
import org.intellij.markdown.parser.sequentialparsers.TokensCache;

import org.jspecify.annotations.Nullable;

import java.util.Collections;

public final class LinkParserUtil {
    private LinkParserUtil() {
    }

    public static SequentialParser.@Nullable LocalParsingResult parseLinkDestination(TokensCache.Iterator iterator) {
        TokensCache.Iterator it = iterator;
        if (it.getType() == MarkdownTokenTypes.EOL || it.getType() == MarkdownTokenTypes.RPAREN) {
            return null;
        }

        int startIndex = it.getIndex();
        boolean withBraces = it.getType() == MarkdownTokenTypes.LT;
        if (withBraces) {
            it = it.advance();
        }

        boolean hasOpenedParentheses = false;
        while (it.getType() != null) {
            if (withBraces && it.getType() == MarkdownTokenTypes.GT) {
                break;
            } else if (!withBraces) {
                if (it.getType() == MarkdownTokenTypes.LPAREN) {
                    if (hasOpenedParentheses) {
                        break;
                    }
                    hasOpenedParentheses = true;
                }

                IElementType next = it.rawLookup(1);
                if (SequentialParserUtil.isWhitespace(it, 1) || next == null) {
                    break;
                } else if (next == MarkdownTokenTypes.RPAREN) {
                    if (!hasOpenedParentheses) {
                        break;
                    }
                    hasOpenedParentheses = false;
                }
            }

            it = it.advance();
        }

        if (it.getType() != null && !hasOpenedParentheses) {
            return new SequentialParser.LocalParsingResult(
                    it,
                    Collections.singletonList(
                            new SequentialParser.Node(startIndex, it.getIndex() + 1, MarkdownElementTypes.LINK_DESTINATION)
                    )
            );
        }
        return null;
    }

    public static SequentialParser.@Nullable LocalParsingResult parseLinkLabel(TokensCache.Iterator iterator) {
        TokensCache.Iterator it = iterator;

        if (it.getType() != MarkdownTokenTypes.LBRACKET) {
            return null;
        }

        int startIndex = it.getIndex();

        RangesListBuilder delegate = new RangesListBuilder();

        it = it.advance();
        while (it.getType() != MarkdownTokenTypes.RBRACKET && it.getType() != null) {
            delegate.put(it.getIndex());
            if (it.getType() == MarkdownTokenTypes.LBRACKET) {
                break;
            }
            it = it.advance();
        }

        if (it.getType() == MarkdownTokenTypes.RBRACKET) {
            int endIndex = it.getIndex();
            if (endIndex == startIndex + 1) {
                return null;
            }

            return new SequentialParser.LocalParsingResult(
                    it,
                    Collections.singletonList(
                            new SequentialParser.Node(startIndex, endIndex + 1, MarkdownElementTypes.LINK_LABEL)
                    ),
                    delegate.get()
            );
        }
        return null;
    }

    public static SequentialParser.@Nullable LocalParsingResult parseLinkText(TokensCache.Iterator iterator) {
        TokensCache.Iterator it = iterator;

        if (it.getType() != MarkdownTokenTypes.LBRACKET) {
            return null;
        }

        int startIndex = it.getIndex();
        RangesListBuilder delegate = new RangesListBuilder();

        int bracketDepth = 1;

        it = it.advance();
        while (it.getType() != null) {
            if (it.getType() == MarkdownTokenTypes.RBRACKET) {
                if (--bracketDepth == 0) {
                    break;
                }
            }

            delegate.put(it.getIndex());
            if (it.getType() == MarkdownTokenTypes.LBRACKET) {
                bracketDepth++;
            }
            it = it.advance();
        }

        if (it.getType() == MarkdownTokenTypes.RBRACKET) {
            return new SequentialParser.LocalParsingResult(
                    it,
                    Collections.singletonList(
                            new SequentialParser.Node(startIndex, it.getIndex() + 1, MarkdownElementTypes.LINK_TEXT)
                    ),
                    delegate.get()
            );
        }
        return null;
    }

    public static SequentialParser.@Nullable LocalParsingResult parseLinkTitle(TokensCache.Iterator iterator) {
        TokensCache.Iterator it = iterator;
        if (it.getType() == MarkdownTokenTypes.EOL) {
            return null;
        }

        int startIndex = it.getIndex();
        IElementType closingType;

        if (it.getType() == MarkdownTokenTypes.SINGLE_QUOTE || it.getType() == MarkdownTokenTypes.DOUBLE_QUOTE) {
            closingType = it.getType();
        } else if (it.getType() == MarkdownTokenTypes.LPAREN) {
            closingType = MarkdownTokenTypes.RPAREN;
        } else {
            return null;
        }

        it = it.advance();
        while (it.getType() != null && it.getType() != closingType) {
            it = it.advance();
        }

        if (it.getType() != null) {
            return new SequentialParser.LocalParsingResult(
                    it,
                    Collections.singletonList(
                            new SequentialParser.Node(startIndex, it.getIndex() + 1, MarkdownElementTypes.LINK_TITLE)
                    )
            );
        }
        return null;
    }
}
