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

/**
 * A simple inclusive integer range, equivalent to Kotlin's IntRange.
 * Both {@code first} and {@code last} are inclusive endpoints.
 */
public final class IntRange {
    private final int myFirst;
    private final int myLast;

    public IntRange(int first, int last) {
        myFirst = first;
        myLast = last;
    }

    public int getFirst() {
        return myFirst;
    }

    public int getLast() {
        return myLast;
    }

    /**
     * Alias for {@link #getFirst()} to match Kotlin's {@code start} property.
     */
    public int getStart() {
        return myFirst;
    }

    /**
     * Alias for {@link #getLast()} to match Kotlin's {@code endInclusive} property.
     */
    public int getEndInclusive() {
        return myLast;
    }

    public boolean contains(int value) {
        return value >= myFirst && value <= myLast;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntRange intRange = (IntRange) o;
        return myFirst == intRange.myFirst && myLast == intRange.myLast;
    }

    @Override
    public int hashCode() {
        return 31 * myFirst + myLast;
    }

    @Override
    
    public String toString() {
        return myFirst + ".." + myLast;
    }
}
