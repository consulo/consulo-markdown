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
package org.intellij.markdown;

import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helpers for reading a Consulo AST the way the markdown generators expect.
 */
public final class MarkdownAstUtil {
    private MarkdownAstUtil() {
    }

    /** All children in order, including whitespace - markdown registers none as platform whitespace. */
    public static List<ASTNode> children(ASTNode node) {
        ASTNode child = node.getFirstChildNode();
        if (child == null) {
            return Collections.emptyList();
        }
        List<ASTNode> result = new ArrayList<>();
        while (child != null) {
            result.add(child);
            child = child.getTreeNext();
        }
        return result;
    }

    public static int endOffset(ASTNode node) {
        return node.getStartOffset() + node.getTextLength();
    }

    /** A node with no children is a terminal, the Consulo equivalent of the old {@code LeafASTNode}. */
    public static boolean isLeaf(ASTNode node) {
        return node.getFirstChildNode() == null;
    }

    public static ASTNode findChildOfType(ASTNode node, IElementType type) {
        for (ASTNode child : children(node)) {
            if (child.getElementType() == type) {
                return child;
            }
        }
        return null;
    }

    public static ASTNode getParentOfType(ASTNode node, IElementType... types) {
        ASTNode parent = node.getTreeParent();
        while (parent != null) {
            for (IElementType type : types) {
                if (parent.getElementType() == type) {
                    return parent;
                }
            }
            parent = parent.getTreeParent();
        }
        return null;
    }

    public static CharSequence getTextInNode(ASTNode node, CharSequence allFileText) {
        return allFileText.subSequence(node.getStartOffset(), endOffset(node));
    }

    public static void accept(ASTNode node, MarkdownVisitor visitor) {
        visitor.visitNode(node);
    }

    public static void acceptChildren(ASTNode node, MarkdownVisitor visitor) {
        for (ASTNode child : children(node)) {
            visitor.visitNode(child);
        }
    }

    /**
     * Whether a list is "loose" - rendered with its items wrapped in paragraphs.
     * <p>
     * Was {@code ListCompositeNode.isLoose()}; the node subclass existed only to hang this on, and
     * to memoise it. It is a pure function of the subtree, so it is a static helper now.
     */
    public static boolean isLooseList(ASTNode list) {
        if (hasLooseContent(list)) {
            return true;
        }
        for (ASTNode child : children(list)) {
            if (child.getElementType() == MarkdownElementTypes.LIST_ITEM && hasLooseContent(child)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasLooseContent(ASTNode node) {
        int newlines = 0;
        boolean seenNonWhitespace = false;
        for (ASTNode child : children(node)) {
            IElementType type = child.getElementType();
            if (type == MarkdownTokenTypes.EOL) {
                ++newlines;
            }
            else if (type == MarkdownTokenTypes.LIST_BULLET
                     || type == MarkdownTokenTypes.LIST_NUMBER
                     || type == MarkdownTokenTypes.WHITE_SPACE) {
                // structural, not content
            }
            else {
                if (seenNonWhitespace && newlines > 1) {
                    return true;
                }
                seenNonWhitespace = true;
                newlines = 0;
            }
        }
        return false;
    }
}
