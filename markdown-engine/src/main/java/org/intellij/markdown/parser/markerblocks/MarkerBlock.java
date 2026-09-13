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
package org.intellij.markdown.parser.markerblocks;

import consulo.language.ast.IElementType;
import org.intellij.markdown.parser.LookaheadText;
import org.intellij.markdown.parser.ProductionHolder;
import org.intellij.markdown.parser.constraints.MarkdownConstraints;

public interface MarkerBlock {

    int getNextInterestingOffset(LookaheadText.Position pos);

    boolean isInterestingOffset(LookaheadText.Position pos);

    boolean allowsSubBlocks();

    
    ProcessingResult processToken(LookaheadText.Position pos,
                                  MarkdownConstraints currentConstraints);

    
    MarkdownConstraints getBlockConstraints();

    /**
     * @param action to accept
     * @return true if this block is to be deleted after this action, false otherwise
     */
    boolean acceptAction(ClosingAction action);

    enum ClosingAction {
        DONE {
            @Override
            public void doAction(ProductionHolder.Marker marker, IElementType type) {
                marker.done(type);
            }
        },
        DROP {
            @Override
            public void doAction(ProductionHolder.Marker marker, IElementType type) {
            }
        },
        DEFAULT {
            @Override
            public void doAction(ProductionHolder.Marker marker, IElementType type) {
                throw new UnsupportedOperationException("Should not be invoked");
            }
        },
        NOTHING {
            @Override
            public void doAction(ProductionHolder.Marker marker, IElementType type) {
            }
        };

        public abstract void doAction(ProductionHolder.Marker marker, IElementType type);
    }

    enum EventAction {
        PROPAGATE,
        CANCEL
    }

    final class ProcessingResult {
        public static final ProcessingResult PASS = new ProcessingResult(ClosingAction.NOTHING, ClosingAction.NOTHING, EventAction.PROPAGATE);
        public static final ProcessingResult CANCEL = new ProcessingResult(ClosingAction.NOTHING, ClosingAction.NOTHING, EventAction.CANCEL);
        public static final ProcessingResult DEFAULT = new ProcessingResult(ClosingAction.DEFAULT, ClosingAction.DONE, EventAction.PROPAGATE);

        private final ClosingAction myChildrenAction;
        private final ClosingAction mySelfAction;
        private final EventAction myEventAction;

        public ProcessingResult(ClosingAction childrenAction,
                         ClosingAction selfAction,
                         EventAction eventAction) {
            myChildrenAction = childrenAction;
            mySelfAction = selfAction;
            myEventAction = eventAction;
        }

        
        public ClosingAction getChildrenAction() {
            return myChildrenAction;
        }

        
        public ClosingAction getSelfAction() {
            return mySelfAction;
        }

        
        public EventAction getEventAction() {
            return myEventAction;
        }
    }
}
