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
import consulo.colorScheme.TextAttributesKey;
import consulo.execution.process.ConsoleHighlighter;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.navigation.ItemPresentationProvider;
import consulo.ui.ex.ColoredItemPresentation;
import consulo.ui.image.Image;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCompositePsiElementBase;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownListItemImpl;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public class MarkdownListItemPresentationProvider implements ItemPresentationProvider<MarkdownListItemImpl> {
    @Override
    public Class<MarkdownListItemImpl> getItemClass() {
        return MarkdownListItemImpl.class;
    }

    @Override
    public ItemPresentation getPresentation(MarkdownListItemImpl item) {
        return new MyItemPresentation(item);
    }

    private static class MyItemPresentation extends MarkdownBasePresentation implements ColoredItemPresentation {
        private final MarkdownListItemImpl myItem;

        MyItemPresentation(MarkdownListItemImpl item) {
            myItem = item;
        }

        @Override
        @RequiredReadAction
        public LocalizeValue getPresentableText() {
            if (!myItem.isValid()) {
                return LocalizeValue.empty();
            }
            PsiElement markerElement = myItem.getMarkerElement();
            if (markerElement == null) {
                return LocalizeValue.empty();
            }
            return LocalizeValue.of(markerElement.getText());
        }

        @Override
        @RequiredReadAction
        public @Nullable String getLocationString() {
            if (!myItem.isValid()) {
                return null;
            }

            if (myItem.hasTrivialChildren()) {
                MarkdownCompositePsiElementBase element = PsiTreeUtil.getChildOfType(myItem, MarkdownCompositePsiElementBase.class);
                assert element != null;
                return element.shrinkTextTo(MarkdownCompositePsiElementBase.PRESENTABLE_TEXT_LENGTH);
            }
            else {
                return null;
            }
        }

        @Override
        public @Nullable Image getIcon() {
            return null;
        }

        @Override
        @RequiredReadAction
        public @Nullable TextAttributesKey getTextAttributesKey() {
            PsiElement checkBox = myItem.getCheckBox();
            if (checkBox == null) {
                return null;
            }
            return checkBox.textContains('x') || checkBox.textContains('X') ? ConsoleHighlighter.GREEN : ConsoleHighlighter.RED;
        }
    }
}
