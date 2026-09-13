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
package org.intellij.markdown.parser.sequentialparsers;

import consulo.language.ast.IElementType;
import org.intellij.markdown.MarkdownTokenTypes;
import org.intellij.markdown.lexer.MarkdownLexer;
import org.intellij.markdown.lexer.TokenInfo;

import java.util.ArrayList;
import java.util.List;

public final class LexerBasedTokensCache extends TokensCache {
    private final List<TokenInfo> myCachedTokens;
    private final List<TokenInfo> myFilteredTokens;
    private final CharSequence myOriginalText;
    private final int myOriginalTextStart;
    private final int myOriginalTextEnd;

    public LexerBasedTokensCache(MarkdownLexer lexer) {
        ResultOfCaching result = cacheTokens(lexer);
        myCachedTokens = result.getCachedTokens();
        myFilteredTokens = result.getFilteredTokens();
        myOriginalText = lexer.getOriginalText();
        myOriginalTextStart = lexer.getBufferStart();
        // Kotlin uses `until` which creates IntRange(start, end - 1), so last = bufferEnd - 1
        myOriginalTextEnd = lexer.getBufferEnd() - 1;

        verify();
    }

    @Override
    
    public List<TokenInfo> getCachedTokens() {
        return myCachedTokens;
    }

    @Override
    
    public List<TokenInfo> getFilteredTokens() {
        return myFilteredTokens;
    }

    @Override
    
    public CharSequence getOriginalText() {
        return myOriginalText;
    }

    @Override
    public int getOriginalTextStart() {
        return myOriginalTextStart;
    }

    @Override
    public int getOriginalTextEnd() {
        return myOriginalTextEnd;
    }

    private static boolean isWhitespace(IElementType elementType) {
        return elementType == MarkdownTokenTypes.WHITE_SPACE;
    }

    
    private static ResultOfCaching cacheTokens(MarkdownLexer lexer) {
        List<TokenInfo> cachedTokens = new ArrayList<>();
        List<TokenInfo> filteredTokens = new ArrayList<>();

        while (lexer.getType() != null) {
            boolean ws = isWhitespace(lexer.getType());
            TokenInfo info = new TokenInfo(
                    lexer.getType(),
                    lexer.getTokenStart(),
                    lexer.getTokenEnd(),
                    cachedTokens.size(),
                    ws ? -1 : filteredTokens.size()
            );

            cachedTokens.add(info);
            if (!ws) {
                filteredTokens.add(info);
            }

            lexer.advance();
        }

        return new ResultOfCaching(cachedTokens, filteredTokens);
    }

    static final class ResultOfCaching {
        private final List<TokenInfo> myCachedTokens;
        private final List<TokenInfo> myFilteredTokens;

        ResultOfCaching(List<TokenInfo> cachedTokens, List<TokenInfo> filteredTokens) {
            myCachedTokens = cachedTokens;
            myFilteredTokens = filteredTokens;
        }

        
        public List<TokenInfo> getCachedTokens() {
            return myCachedTokens;
        }

        
        public List<TokenInfo> getFilteredTokens() {
            return myFilteredTokens;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ResultOfCaching that = (ResultOfCaching) o;
            return myCachedTokens.equals(that.myCachedTokens)
                    && myFilteredTokens.equals(that.myFilteredTokens);
        }

        @Override
        public int hashCode() {
            int result = myCachedTokens.hashCode();
            result = 31 * result + myFilteredTokens.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "ResultOfCaching(cachedTokens=" + myCachedTokens
                    + ", filteredTokens=" + myFilteredTokens + ")";
        }
    }
}
