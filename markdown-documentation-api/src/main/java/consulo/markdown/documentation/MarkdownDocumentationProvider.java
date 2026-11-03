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
package consulo.markdown.documentation;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.application.Application;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiLanguageInjectionHost;
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor;
import org.intellij.markdown.parser.MarkdownParserManager;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Describes the Markdown documentation comments of a language, for instance the {@code ///} comments of Java (JEP 467) or of Rust.
 * <p>
 * The comment keeps the PSI of its language; the Markdown implementation highlights its Markdown, injects the languages of its
 * code fences and renders it, through the shared classes of this module.
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface MarkdownDocumentationProvider {
    /**
     * @return The class of the documentation comment, for instance {@code PsiDocComment}
     */
    Class<? extends PsiElement> getCommentClass();

    /**
     * @return Whether the comment is written in Markdown - a language may have other documentation comments, as Java {@code /** *}{@code /}
     */
    default boolean isMarkdown(PsiElement comment) {
        return true;
    }

    /**
     * @return The prefixes of the lines of the comment, which are not part of the Markdown, for instance {@code ///}
     */
    List<String> getLinePrefixes();

    /**
     * @return The flavour the Markdown of the comment is parsed and rendered with
     */
    default MarkdownFlavourDescriptor getFlavour() {
        return MarkdownParserManager.FLAVOUR;
    }

    /**
     * @return The class of the code fences of the comment, which the language of the fence is injected into, or {@code null} when
     * the language has no such element
     */
    default @Nullable Class<? extends PsiLanguageInjectionHost> getCodeFenceClass() {
        return null;
    }

    /**
     * @return The info string of a code fence of {@link #getCodeFenceClass()}, for instance {@code java}, or {@code null} if it has none
     */
    default @Nullable String getCodeFenceInfo(PsiLanguageInjectionHost codeFence) {
        return null;
    }

    static @Nullable MarkdownDocumentationProvider findByComment(PsiElement comment) {
        return Application.get().getExtensionPoint(MarkdownDocumentationProvider.class)
            .findFirstSafe(provider -> provider.getCommentClass().isInstance(comment) && provider.isMarkdown(comment));
    }

    static @Nullable MarkdownDocumentationProvider findByCodeFence(PsiElement codeFence) {
        return Application.get().getExtensionPoint(MarkdownDocumentationProvider.class)
            .findFirstSafe(provider -> {
                Class<? extends PsiLanguageInjectionHost> codeFenceClass = provider.getCodeFenceClass();
                return codeFenceClass != null && codeFenceClass.isInstance(codeFence);
            });
    }
}
