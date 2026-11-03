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
package org.intellij.plugins.markdown.spellchecking;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.spellcheker.text.TextContent;
import consulo.language.spellcheker.text.TextContentBuilder;
import consulo.language.spellcheker.text.TextExtractor;
import consulo.language.spellcheker.text.util.HtmlUtils;
import org.intellij.markdown.MarkdownLanguage;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;

import java.util.List;
import java.util.Set;

@ExtensionImpl
public class MarkdownTextExtractor extends TextExtractor {
    private final Set<IElementType> markup = Set.of(MarkdownTokenTypes.EMPH, MarkdownTokenTypes.TILDE);

    @Override
    public List<TextContent> buildTextContents(PsiElement root, Set<TextContent.TextDomain> allowedDomains) {
        if (allowedDomains.contains(TextContent.TextDomain.PLAIN_TEXT) &&
            (MarkdownPsiUtils.isHeaderContent(root) || MarkdownPsiUtils.isParagraph(root))) {
            TextContent content = TextContentBuilder.FromPsi
                .withUnknown(it -> MarkdownPsiUtils.isMarkdownCodeType(it.getNode()))
                .withMarkup(e ->
                    markup.contains(PsiUtilCore.getElementType(e)) ||
                        e.getFirstChild() == null && MarkdownPsiUtils.isMarkdownLinkType(e.getParent().getNode()) &&
                            !isLinkText(e) && !isShortRefLinkLabel(e)
                )
                .excluding(it -> PsiUtilCore.getElementType(it) == MarkdownElementTypes.IMAGE)
                .removingIndents(" \t").removingLineSuffixes(" \t")
                .build(root, TextContent.TextDomain.PLAIN_TEXT);
            return HtmlUtils.excludeHtml(content);
        }
        return List.of();
    }

    private static boolean isLinkText(PsiElement e) {
        return (PsiUtilCore.getElementType(e) == MarkdownTokenTypes.TEXT ||
            PsiUtilCore.getElementType(e) == MarkdownTokenTypes.GFM_AUTOLINK ||
            e instanceof PsiWhiteSpace) &&
            PsiUtilCore.getElementType(e.getParent()) == MarkdownElementTypes.LINK_TEXT;
    }

    private static boolean isShortRefLinkLabel(PsiElement e) {
        return PsiUtilCore.getElementType(e.getParent().getParent()) == MarkdownElementTypes.SHORT_REFERENCE_LINK &&
            PsiUtilCore.getElementType(e.getParent()) == MarkdownElementTypes.LINK_LABEL &&
            (PsiUtilCore.getElementType(e) == MarkdownTokenTypes.TEXT || e instanceof PsiWhiteSpace);
    }

    @Override
    public Language getLanguage() {
        return MarkdownLanguage.INSTANCE;
    }
}
