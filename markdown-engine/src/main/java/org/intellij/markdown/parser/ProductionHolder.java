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
package org.intellij.markdown.parser;

import consulo.language.ast.IElementType;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ProductionHolder {
    private int myCurrentPosition = 0;
    private final List<SequentialParser.Node> myProduction = new ArrayList<>();

    public int getCurrentPosition() {
        return myCurrentPosition;
    }

    
    public List<SequentialParser.Node> getProduction() {
        return myProduction;
    }

    public void updatePosition(int position) {
        myCurrentPosition = position;
    }

    public void addProduction(Collection<SequentialParser.Node> nodes) {
        myProduction.addAll(nodes);
    }

    
    public Marker mark() {
        return new Marker();
    }

    public final class Marker {
        private final int myStartPos;

        Marker() {
            myStartPos = myCurrentPosition;
        }

        public void done(IElementType type) {
            myProduction.add(new SequentialParser.Node(myStartPos, myCurrentPosition, type));
        }
    }
}
