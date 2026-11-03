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
import consulo.navigation.ItemPresentation;
import consulo.navigation.ItemPresentationProvider;
import consulo.ui.ex.DelegatingItemPresentation;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownBlockQuoteImpl;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public class MarkdownBlockQuotePresentationProvider implements ItemPresentationProvider<MarkdownBlockQuoteImpl> {
    @Override
    public Class<MarkdownBlockQuoteImpl> getItemClass() {
        return MarkdownBlockQuoteImpl.class;
    }

    @Override
    public ItemPresentation getPresentation(MarkdownBlockQuoteImpl item) {
        return new DelegatingItemPresentation(MarkdownCompositePresentationProvider.createPresentation(item)) {
            @Override
            @RequiredReadAction
            public @Nullable String getLocationString() {
                if (!item.isValid()) {
                    return null;
                }
                return item.hasTrivialChildren() ? super.getLocationString() : null;
            }
        };
    }
}
