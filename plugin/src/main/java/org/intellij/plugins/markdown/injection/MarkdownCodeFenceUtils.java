/*
 * Copyright 2000-2026 JetBrains s.r.o. and contributors.
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
package org.intellij.plugins.markdown.injection;

import consulo.annotation.access.RequiredReadAction;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.language.psi.util.PsiTreeUtil;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceImpl;
import org.jspecify.annotations.Nullable;

/**
 * Utility functions used to work with Markdown Code Fences.
 * <p>
 * The code fence PSI is {@link MarkdownCodeFenceImpl}.
 */
public final class MarkdownCodeFenceUtils {
    private MarkdownCodeFenceUtils() {
    }

    /**
     * Check if {@code node} is of CODE_FENCE type.
     */
    public static boolean isCodeFence(ASTNode node) {
        return node.getElementType() == MarkdownElementTypes.CODE_FENCE;
    }

    /**
     * Check if {@code node} is inside CODE_FENCE.
     */
    public static boolean inCodeFence(ASTNode node) {
        for (ASTNode parent = node.getTreeParent(); parent != null; parent = parent.getTreeParent()) {
            if (isCodeFence(parent)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get code fence if {@code element} is part of it.
     * <p>
     * Would also work for injected elements.
     */
    @RequiredReadAction
    public static @Nullable MarkdownCodeFenceImpl getCodeFence(PsiElement element) {
        PsiLanguageInjectionHost host = InjectedLanguageManager.getInstance(element.getProject()).getInjectionHost(element);
        if (host instanceof MarkdownCodeFenceImpl fence) {
            return fence;
        }
        return PsiTreeUtil.getParentOfType(element, MarkdownCodeFenceImpl.class);
    }

    /**
     * Get indent for the line at {@code offset}, up to (not including) {@code offset} itself.
     * <p>
     * If blockquoted, the indent may include the {@code >} char.
     */
    public static String getIndent(Document document, int offset) {
        int lineStartOffset = document.getLineStartOffset(document.getLineNumber(offset));
        return document.getText(TextRange.create(lineStartOffset, offset)).replaceAll("[^>\\t ]", " ");
    }
}
