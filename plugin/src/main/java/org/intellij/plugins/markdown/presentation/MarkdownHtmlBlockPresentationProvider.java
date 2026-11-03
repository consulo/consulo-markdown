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

import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.navigation.ItemPresentationProvider;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownHtmlBlockImpl;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public class MarkdownHtmlBlockPresentationProvider implements ItemPresentationProvider<MarkdownHtmlBlockImpl> {
    @Override
    public Class<MarkdownHtmlBlockImpl> getItemClass() {
        return MarkdownHtmlBlockImpl.class;
    }

    @Override
    public ItemPresentation getPresentation(MarkdownHtmlBlockImpl item) {
        return new MarkdownBasePresentation() {
            @Override
            public LocalizeValue getPresentableText() {
                return LocalizeValue.localizeTODO("HTML block");
            }

            @Override
            public @Nullable String getLocationString() {
                return null;
            }
        };
    }
}
