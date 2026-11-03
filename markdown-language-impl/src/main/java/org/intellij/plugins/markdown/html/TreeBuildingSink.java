/*
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
package org.intellij.plugins.markdown.html;

import org.intellij.markdown.parser.MarkdownProductionSink;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.ILazyParseableElementType;
import consulo.language.impl.ast.ASTFactory;
import consulo.language.impl.ast.CompositeElement;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Sink that builds a standalone tree, detached from any file - the counterpart of upstream's
 * {@code ASTNodeBuilder}.
 * <p>
 * Lazy parseable types are created already parsed (no chameleon text), so their children are the
 * ones produced here with the caller's flavour, instead of being reparsed later with the flavour of
 * a containing file, which a detached tree does not have.
 */
public final class TreeBuildingSink implements MarkdownProductionSink {
    private final CharSequence myText;
    private final Deque<CompositeElement> myStack = new ArrayDeque<>();
    private @Nullable CompositeElement myRoot;

    public TreeBuildingSink(CharSequence text) {
        myText = text;
    }

    public ASTNode getRoot() {
        if (myRoot == null || !myStack.isEmpty()) {
            throw new IllegalStateException("tree is not complete");
        }
        return myRoot;
    }

    @Override
    public void open(IElementType type, int startOffset) {
        CompositeElement element = type instanceof ILazyParseableElementType lazyType
            ? ASTFactory.lazy(lazyType, null)
            : ASTFactory.composite(type);
        CompositeElement parent = myStack.peek();
        if (parent != null) {
            parent.rawAddChildrenWithoutNotifications(element);
        }
        else {
            myRoot = element;
        }
        myStack.push(element);
    }

    @Override
    public void close(IElementType type, int endOffset) {
        myStack.pop();
    }

    @Override
    public void leaf(IElementType type, int startOffset, int endOffset) {
        if (startOffset == endOffset) {
            return;
        }
        CompositeElement parent = myStack.peek();
        if (parent == null) {
            throw new IllegalStateException("leaf outside of the root");
        }
        parent.rawAddChildrenWithoutNotifications(ASTFactory.leaf(type, myText.subSequence(startOffset, endOffset)));
    }
}
