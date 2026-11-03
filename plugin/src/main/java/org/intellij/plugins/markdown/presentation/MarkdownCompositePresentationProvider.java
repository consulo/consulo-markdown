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
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.navigation.ItemPresentationProvider;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCompositePsiElementBase;
import org.jspecify.annotations.Nullable;

/**
 * The presentation of the composite Markdown elements: their tag name, and the text of an element without composite children.
 */
@ExtensionImpl
public class MarkdownCompositePresentationProvider implements ItemPresentationProvider<MarkdownCompositePsiElementBase> {
    @Override
    public Class<MarkdownCompositePsiElementBase> getItemClass() {
        return MarkdownCompositePsiElementBase.class;
    }

    @Override
    public ItemPresentation getPresentation(MarkdownCompositePsiElementBase item) {
        return createPresentation(item);
    }

    static ItemPresentation createPresentation(MarkdownCompositePsiElementBase item) {
        return new MarkdownBasePresentation() {
            @Override
            @RequiredReadAction
            public LocalizeValue getPresentableText() {
                if (!item.isValid()) {
                    return LocalizeValue.empty();
                }
                return item.getPresentableTagName();
            }

            @Override
            @RequiredReadAction
            public @Nullable String getLocationString() {
                if (!item.isValid()) {
                    return null;
                }
                return item.getCompositeChildren().isEmpty() ? item.shrinkTextTo(MarkdownCompositePsiElementBase.PRESENTABLE_TEXT_LENGTH) : null;
            }
        };
    }
}
