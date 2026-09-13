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

import java.util.ArrayList;
import java.util.List;

public final class RangesListBuilder {
    private final List<IntRange> myList = new ArrayList<>();
    private int myLastStart = -239;
    private int myLastEnd = -239;

    public void put(int index) {
        if (myLastEnd + 1 == index) {
            myLastEnd = index;
            return;
        }
        if (myLastStart != -239) {
            myList.add(new IntRange(myLastStart, myLastEnd));
        }
        myLastStart = index;
        myLastEnd = index;
    }

    
    public List<IntRange> get() {
        if (myLastStart != -239) {
            myList.add(new IntRange(myLastStart, myLastEnd));
        }
        myLastStart = -239;
        myLastEnd = -239;
        return myList;
    }
}
