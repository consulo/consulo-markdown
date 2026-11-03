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
package org.intellij.plugins.markdown.presentation;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.navigation.ItemPresentationProvider;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceContentImpl;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceImpl;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCompositePsiElementBase;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public class MarkdownCodeFencePresentationProvider implements ItemPresentationProvider<MarkdownCodeFenceImpl> {
    @Override
    public Class<MarkdownCodeFenceImpl> getItemClass() {
        return MarkdownCodeFenceImpl.class;
    }

    @Override
    public ItemPresentation getPresentation(MarkdownCodeFenceImpl item) {
        return new MarkdownBasePresentation() {
            @Override
            @RequiredReadAction
            public LocalizeValue getPresentableText() {
                if (!item.isValid()) {
                    return LocalizeValue.empty();
                }
                return LocalizeValue.localizeTODO("Code fence");
            }

            @Override
            @RequiredReadAction
            public @Nullable String getLocationString() {
                if (!item.isValid()) {
                    return null;
                }

                StringBuilder sb = new StringBuilder();
                for (PsiElement child = item.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (!(child instanceof MarkdownCodeFenceContentImpl)) {
                        continue;
                    }
                    if (sb.length() > 0) {
                        sb.append("\\n");
                    }
                    sb.append(child.getText());

                    if (sb.length() >= MarkdownCompositePsiElementBase.PRESENTABLE_TEXT_LENGTH) {
                        break;
                    }
                }

                return sb.toString();
            }
        };
    }
}
