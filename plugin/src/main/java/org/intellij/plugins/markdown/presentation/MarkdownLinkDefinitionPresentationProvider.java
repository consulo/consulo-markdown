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
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownLinkDefinitionImpl;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public class MarkdownLinkDefinitionPresentationProvider implements ItemPresentationProvider<MarkdownLinkDefinitionImpl> {
    @Override
    public Class<MarkdownLinkDefinitionImpl> getItemClass() {
        return MarkdownLinkDefinitionImpl.class;
    }

    @Override
    public ItemPresentation getPresentation(MarkdownLinkDefinitionImpl item) {
        return new MarkdownBasePresentation() {
            @Override
            @RequiredReadAction
            public LocalizeValue getPresentableText() {
                if (!item.isValid()) {
                    return LocalizeValue.empty();
                }

                return LocalizeValue.localizeTODO("Def: " + item.getLinkLabel().getText() + " → " + item.getLinkDestination().getText());
            }

            @Override
            @RequiredReadAction
            public @Nullable String getLocationString() {
                if (!item.isValid()) {
                    return null;
                }

                PsiElement linkTitle = item.getLinkTitle();
                return linkTitle == null ? null : linkTitle.getText();
            }
        };
    }
}
