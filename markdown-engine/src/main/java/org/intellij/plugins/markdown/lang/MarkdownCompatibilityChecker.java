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
package org.intellij.plugins.markdown.lang;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.component.extension.ExtensionPointName;
import consulo.dataContext.DataContext;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import org.intellij.markdown.MarkdownLanguage;
import org.jspecify.annotations.Nullable;

/**
 * Provides support for languages that either are Markdown or are compatible with Markdown-like features.
 * This interface is used to determine if a language should be treated as Markdown-supported.
 * <p>
 * Example:
 * - MarkdownLanguage is the default Markdown implementation.
 * - MDX files contain Markdown outside of their JSX and ESM parts.
 * <p>
 * Ported from the JetBrains Markdown plugin. Use {@link #supportsMarkdown(Language)} and friends to ask all checkers.
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface MarkdownCompatibilityChecker {
    ExtensionPointName<MarkdownCompatibilityChecker> EP_NAME = ExtensionPointName.create(MarkdownCompatibilityChecker.class);

    boolean isSupportedLanguage(Language language);

    default boolean isSupportedElement(PsiElement element) {
        return isSupportedLanguage(element.getContainingFile().getLanguage());
    }

    default boolean isSupportedRange(PsiFile file, TextRange range) {
        return isSupportedLanguage(file.getLanguage());
    }

    default boolean isSupportedContext(Language language, @Nullable DataContext dataContext) {
        return isSupportedLanguage(language);
    }

    /**
     * JetBrains: {@code MarkdownLanguageUtils.supportsMarkdown}. Markdown itself is always supported.
     */
    static boolean supportsMarkdown(Language language) {
        return language == MarkdownLanguage.INSTANCE || EP_NAME.findFirstSafe(it -> it.isSupportedLanguage(language)) != null;
    }

    static boolean supportsMarkdown(PsiElement element) {
        PsiFile file = element.getContainingFile();
        if (file != null && file.getLanguage() == MarkdownLanguage.INSTANCE) {
            return true;
        }
        return EP_NAME.findFirstSafe(it -> it.isSupportedElement(element)) != null;
    }

    static boolean supportsMarkdown(PsiFile file, TextRange range) {
        return file.getLanguage() == MarkdownLanguage.INSTANCE || EP_NAME.findFirstSafe(it -> it.isSupportedRange(file, range)) != null;
    }

    static boolean supportsMarkdown(Language language, @Nullable DataContext dataContext) {
        return language == MarkdownLanguage.INSTANCE || EP_NAME.findFirstSafe(it -> it.isSupportedContext(language, dataContext)) != null;
    }
}
