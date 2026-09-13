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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public interface SequentialParser {

    
    ParsingResult parse(TokensCache tokens, List<IntRange> rangesToGlue);

    final class Node {
        private final int myStartOffset;
        private final int myEndOffset;
        private final IElementType myType;

        public Node(int startOffset, int endOffset, IElementType type) {
            myStartOffset = startOffset;
            myEndOffset = endOffset;
            myType = type;
        }

        public int getStartOffset() {
            return myStartOffset;
        }

        public int getEndOffset() {
            return myEndOffset;
        }

        
        public IElementType getType() {
            return myType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return myStartOffset == node.myStartOffset
                    && myEndOffset == node.myEndOffset
                    && Objects.equals(myType, node.myType);
        }

        @Override
        public int hashCode() {
            int result = myStartOffset;
            result = 31 * result + myEndOffset;
            result = 31 * result + (myType != null ? myType.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Node(startOffset=" + myStartOffset
                    + ", endOffset=" + myEndOffset
                    + ", type=" + myType + ")";
        }
    }

    interface ParsingResult {
        
        Collection<Node> getParsedNodes();

        
        Collection<List<IntRange>> getRangesToProcessFurther();
    }

    class ParsingResultBuilder implements ParsingResult {
        private final Collection<Node> myParsedNodes = new ArrayList<>();
        private final Collection<List<IntRange>> myRangesToProcessFurther = new ArrayList<>();

        @Override
        
        public Collection<Node> getParsedNodes() {
            return myParsedNodes;
        }

        @Override
        
        public Collection<List<IntRange>> getRangesToProcessFurther() {
            return myRangesToProcessFurther;
        }

        
        public ParsingResultBuilder withNode(Node result) {
            myParsedNodes.add(result);
            return this;
        }

        
        public ParsingResultBuilder withNodes(Collection<Node> parsedNodes) {
            myParsedNodes.addAll(parsedNodes);
            return this;
        }

        
        public ParsingResultBuilder withFurtherProcessing(List<IntRange> ranges) {
            myRangesToProcessFurther.add(ranges);
            return this;
        }

        
        public ParsingResultBuilder withOtherParsingResult(ParsingResult parsingResult) {
            myParsedNodes.addAll(parsingResult.getParsedNodes());
            myRangesToProcessFurther.addAll(parsingResult.getRangesToProcessFurther());
            return this;
        }
    }

    final class LocalParsingResult implements ParsingResult {
        private final TokensCache.Iterator myIteratorPosition;
        private final Collection<Node> myParsedNodes;
        private final Collection<List<IntRange>> myRangesToProcessFurther;

        public LocalParsingResult(TokensCache.Iterator iteratorPosition,
                                  Collection<Node> parsedNodes,
                                  Collection<List<IntRange>> rangesToProcessFurther) {
            myIteratorPosition = iteratorPosition;
            myParsedNodes = parsedNodes;
            myRangesToProcessFurther = rangesToProcessFurther;
        }

        public LocalParsingResult(TokensCache.Iterator iteratorPosition,
                                  Collection<Node> parsedNodes,
                                  List<IntRange> delegateRanges) {
            this(iteratorPosition, parsedNodes, Collections.singletonList(delegateRanges));
        }

        public LocalParsingResult(TokensCache.Iterator iteratorPosition,
                                  Collection<Node> parsedNodes) {
            this(iteratorPosition, parsedNodes, Collections.<List<IntRange>>emptyList());
        }

        
        public TokensCache.Iterator getIteratorPosition() {
            return myIteratorPosition;
        }

        @Override
        
        public Collection<Node> getParsedNodes() {
            return myParsedNodes;
        }

        @Override
        
        public Collection<List<IntRange>> getRangesToProcessFurther() {
            return myRangesToProcessFurther;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LocalParsingResult that = (LocalParsingResult) o;
            return Objects.equals(myIteratorPosition, that.myIteratorPosition)
                    && Objects.equals(myParsedNodes, that.myParsedNodes)
                    && Objects.equals(myRangesToProcessFurther, that.myRangesToProcessFurther);
        }

        @Override
        public int hashCode() {
            int result = myIteratorPosition != null ? myIteratorPosition.hashCode() : 0;
            result = 31 * result + (myParsedNodes != null ? myParsedNodes.hashCode() : 0);
            result = 31 * result + (myRangesToProcessFurther != null ? myRangesToProcessFurther.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "LocalParsingResult(iteratorPosition=" + myIteratorPosition
                    + ", parsedNodes=" + myParsedNodes
                    + ", rangesToProcessFurther=" + myRangesToProcessFurther + ")";
        }
    }
}
