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
package org.intellij.markdown.lexer;

import consulo.language.ast.IElementType;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class TokenInfo {
    private final @Nullable IElementType myType;
    private final int myTokenStart;
    private final int myTokenEnd;
    private final int myRawIndex;
    private final int myNormIndex;

    public TokenInfo(@Nullable IElementType type, int tokenStart, int tokenEnd, int rawIndex, int normIndex) {
        myType = type;
        myTokenStart = tokenStart;
        myTokenEnd = tokenEnd;
        myRawIndex = rawIndex;
        myNormIndex = normIndex;
    }

    public @Nullable IElementType getType() {
        return myType;
    }

    public int getTokenStart() {
        return myTokenStart;
    }

    public int getTokenEnd() {
        return myTokenEnd;
    }

    public int getRawIndex() {
        return myRawIndex;
    }

    public int getNormIndex() {
        return myNormIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenInfo tokenInfo = (TokenInfo) o;
        return myTokenStart == tokenInfo.myTokenStart &&
                myTokenEnd == tokenInfo.myTokenEnd &&
                myRawIndex == tokenInfo.myRawIndex &&
                myNormIndex == tokenInfo.myNormIndex &&
                Objects.equals(myType, tokenInfo.myType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myType, myTokenStart, myTokenEnd, myRawIndex, myNormIndex);
    }

    @Override
    public String toString() {
        return "TokenInfo(" +
                "type=" + myType +
                ", tokenStart=" + myTokenStart +
                ", tokenEnd=" + myTokenEnd +
                ", rawIndex=" + myRawIndex +
                ", normIndex=" + myNormIndex +
                ')';
    }
}
