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
import org.jspecify.annotations.Nullable;

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

    /**
     * Drives {@code production} into {@code sink}.
     * <p>
     * Malformed input must never propagate out of here: this runs inside the lexer, so an exception
     * would break every caller that merely touches the file - find-in-files, indexing, the
     * structure view. If the productions do not nest, the document is emitted as flat text
     * instead, which keeps the token stream gap-free and the file merely unstructured.
     * <p>
     * Nesting is therefore checked <em>before</em> anything is emitted. It cannot be checked while
     * emitting: by the time a mismatch is found the sink has already been given markers, and a
     * {@link consulo.language.parser.PsiBuilder} cannot be rewound from here.
     */
    public void walk(List<SequentialParser.Node> production, MarkdownProductionSink sink) {
        List<MyEvent> events = constructEvents(production);
        if (events.isEmpty()
            || events.get(0).myInfo != events.get(events.size() - 1).myInfo
            || !nestsProperly(events)) {
            emitFallback(events.isEmpty() ? null : events.get(0).myInfo, sink);
            return;
        }

        // A production that is itself a token becomes one leaf covering its whole range; anything
        // the parser found inside it is part of that token's text, not a child.
        //
        // Every event is still pushed and popped while inside such a node - only the output is
        // suppressed. Skipping the events instead unbalances the stack, because a node's open and
        // close sit at opposite ends of its span and a positional test can accept one and reject
        // the other.
        Stack<SequentialParser.Node> open = new Stack<>();
        int suppressDepth = 0;

        for (int i = 0; i < events.size(); i++) {
            myCancellationToken.checkCancelled();
            MyEvent event = events.get(i);
            IElementType type = event.myInfo.getType();

            if (event.isStart()) {
                if (suppressDepth > 0) {
                    open.push(event.myInfo);
                    suppressDepth++;
                    continue;
                }
                flushBefore(startOffsetOf(event.myInfo), sink);
                if (isLeafType(type)) {
                    emitLeaf(event.myInfo, sink);
                    open.push(event.myInfo);
                    suppressDepth = 1;
                    continue;
                }
                open.push(event.myInfo);
                sink.open(type, startOffsetOf(event.myInfo));
            }
            else if (event.isEmpty()) {
                if (suppressDepth > 0) {
                    continue;
                }
                flushBefore(startOffsetOf(event.myInfo), sink);
                if (isLeafType(type)) {
                    emitLeaf(event.myInfo, sink);
                    continue;
                }
                sink.open(type, startOffsetOf(event.myInfo));
                // A production can be empty in its own coordinates yet still span text - an
                // inline root over a region with no non-whitespace tokens, say. Without this
                // flush that text is lost, and only the parse expectations would notice.
                flushBefore(endOffsetOf(event.myInfo), sink);
                sink.close(type, endOffsetOf(event.myInfo));
            }
            else {
                open.pop();
                if (suppressDepth > 0) {
                    suppressDepth--;
                    continue;
                }
                flushBefore(endOffsetOf(event.myInfo), sink);
                sink.close(type, endOffsetOf(event.myInfo));
            }
        }
    }

    /**
     * Whether the events open and close in a well-formed order. Pure: no sink calls and no cursor
     * state is touched, so it can run before anything is emitted.
     */
    private boolean nestsProperly(List<MyEvent> events) {
        Stack<SequentialParser.Node> open = new Stack<>();
        int suppressDepth = 0;

        for (MyEvent event : events) {
            myCancellationToken.checkCancelled();
            if (event.isStart()) {
                open.push(event.myInfo);
                if (suppressDepth > 0) {
                    suppressDepth++;
                }
                else if (isLeafType(event.myInfo.getType())) {
                    suppressDepth = 1;
                }
            }
            else if (!event.isEmpty()) {
                if (open.isEmpty() || open.pop() != event.myInfo) {
                    return false;
                }
                if (suppressDepth > 0) {
                    suppressDepth--;
                }
            }
        }
        return open.isEmpty();
    }

    /**
     * Emits the whole region as flat text under {@code root}, for input whose productions do not
     * nest. {@code root} is null only when there were no productions at all.
     */
    protected abstract void emitFallback(SequentialParser.@Nullable Node root, MarkdownProductionSink sink);

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
