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
import org.intellij.markdown.MarkdownTokenMarker;
import org.intellij.markdown.lexer.Compat;
import org.intellij.markdown.lexer.Stack;
import org.intellij.markdown.parser.sequentialparsers.SequentialParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Walks a production list in document order, driving a {@link MarkdownProductionSink}.
 * <p>
 * This is the event algorithm that used to live in {@code TreeBuilder}: productions become paired
 * open/close events, sorted so that nesting is well formed, and a stack checks that they nest
 * rather than intersect. What changed is that it no longer builds anything - the sink decides.
 */
public abstract class ProductionWalker {
    protected final CancellationToken myCancellationToken;

    protected ProductionWalker(CancellationToken cancellationToken) {
        myCancellationToken = cancellationToken;
    }

    public void walk(List<SequentialParser.Node> production, MarkdownProductionSink sink) {
        List<MyEvent> events = constructEvents(production);
        Compat.assertCondition(!events.isEmpty(), "nonsense");
        Compat.assertCondition(events.get(0).myInfo == events.get(events.size() - 1).myInfo,
                "more than one root?\nfirst: " + events.get(0).myInfo + "\nlast: " + events.get(events.size() - 1).myInfo);

        Stack<SequentialParser.Node> open = new Stack<>();
        // A production that is itself a token becomes one leaf covering its whole range; anything
        // the parser found inside it is part of that token's text, not a child. Events for nodes
        // contained in that range are therefore dropped rather than emitted alongside it.
        int suppressStart = -1;
        int suppressEnd = -1;

        for (int i = 0; i < events.size(); i++) {
            myCancellationToken.checkCancelled();
            MyEvent event = events.get(i);
            IElementType type = event.myInfo.getType();

            if (suppressEnd >= 0) {
                if (event.myPosition >= suppressEnd) {
                    suppressStart = -1;
                    suppressEnd = -1;
                }
                else if (startOffsetOf(event.myInfo) >= suppressStart
                         && endOffsetOf(event.myInfo) <= suppressEnd) {
                    continue;
                }
            }

            if (event.isStart()) {
                flushBefore(startOffsetOf(event.myInfo), sink);
                if (isLeafType(type)) {
                    emitLeaf(event.myInfo, sink);
                    suppressStart = startOffsetOf(event.myInfo);
                    suppressEnd = endOffsetOf(event.myInfo);
                    continue;
                }
                open.push(event.myInfo);
                sink.open(type, startOffsetOf(event.myInfo));
            }
            else {
                if (event.isEmpty()) {
                    flushBefore(startOffsetOf(event.myInfo), sink);
                    if (isLeafType(type)) {
                        emitLeaf(event.myInfo, sink);
                        suppressStart = startOffsetOf(event.myInfo);
                        suppressEnd = endOffsetOf(event.myInfo);
                        continue;
                    }
                    sink.open(type, startOffsetOf(event.myInfo));
                    // A production can be empty in its own coordinates yet still span text - an
                    // inline root over a region with no non-whitespace tokens, say. Without this
                    // flush that text is lost, and only the engine tree goldens would notice.
                    flushBefore(endOffsetOf(event.myInfo), sink);
                    sink.close(type, endOffsetOf(event.myInfo));
                    continue;
                }
                if (isLeafType(type)) {
                    continue;
                }
                SequentialParser.Node started = open.pop();
                Compat.assertCondition(started == event.myInfo,
                        "Intersecting parsed nodes detected: " + started + " vs " + event.myInfo);
                flushBefore(endOffsetOf(event.myInfo), sink);
                sink.close(type, endOffsetOf(event.myInfo));
            }
        }
        Compat.assertCondition(open.isEmpty(), "unclosed nodes remain: " + open);
    }

    /** Types the parser reports as a span but that are terminals in the tree. */
    protected boolean isLeafType(IElementType type) {
        return type instanceof MarkdownTokenMarker && ((MarkdownTokenMarker) type).isToken();
    }

    /** Document character offset where this production starts. */
    protected abstract int startOffsetOf(SequentialParser.Node node);

    /** Document character offset where this production ends. */
    protected abstract int endOffsetOf(SequentialParser.Node node);

    /** Emit everything occupying the document up to {@code offset} that no production covers. */
    protected abstract void flushBefore(int offset, MarkdownProductionSink sink);

    protected abstract void emitLeaf(SequentialParser.Node node, MarkdownProductionSink sink);

    private List<MyEvent> constructEvents(List<SequentialParser.Node> production) {
        ArrayList<MyEvent> events = new ArrayList<>();
        for (int index = 0; index < production.size(); index++) {
            myCancellationToken.checkCancelled();
            SequentialParser.Node result = production.get(index);
            int start = result.getStartOffset();
            int end = result.getEndOffset();

            events.add(new MyEvent(start, index, result));
            if (end != start) {
                events.add(new MyEvent(end, index, result));
            }
        }
        Collections.sort(events);
        return events;
    }

    protected static class MyEvent implements Comparable<MyEvent> {
        final int myPosition;
        final int myTimeClosed;
        final SequentialParser.Node myInfo;

        MyEvent(int position, int timeClosed, SequentialParser.Node info) {
            myPosition = position;
            myTimeClosed = timeClosed;
            myInfo = info;
        }

        boolean isStart() {
            return myInfo.getEndOffset() != myPosition;
        }

        boolean isEmpty() {
            return myInfo.getStartOffset() == myInfo.getEndOffset();
        }

        @Override
        public int compareTo(MyEvent other) {
            if (myPosition != other.myPosition) {
                return myPosition - other.myPosition;
            }
            if (isStart() == other.isStart()) {
                int positionDiff = myInfo.getStartOffset() + myInfo.getEndOffset()
                        - (other.myInfo.getStartOffset() + other.myInfo.getEndOffset());
                if (positionDiff != 0) {
                    if (isEmpty() || other.isEmpty()) {
                        return positionDiff;
                    }
                    return -positionDiff;
                }
                int timeDiff = myTimeClosed - other.myTimeClosed;
                return isStart() ? -timeDiff : timeDiff;
            }
            return isStart() ? 1 : -1;
        }

        @Override
        public String toString() {
            return (isStart() ? "Open" : "Close") + ": " + myPosition + " (" + myInfo + ")";
        }
    }
}
