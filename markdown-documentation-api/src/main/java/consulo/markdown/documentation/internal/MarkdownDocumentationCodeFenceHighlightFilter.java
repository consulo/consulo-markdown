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
package consulo.markdown.documentation.internal;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.editor.rawHighlight.HighlightInfo;
import consulo.language.editor.rawHighlight.HighlightInfoFilter;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.markdown.documentation.MarkdownDocumentationProvider;
import org.jspecify.annotations.Nullable;

/**
 * Code in documentation comments is usually a fragment ({@code list.add(1);} rather than a whole file), which the language of the
 * fence cannot compile on its own - so problems in the code injected into the fences of Markdown documentation comments are not
 * shown, only its highlighting.
 */
@ExtensionImpl
public class MarkdownDocumentationCodeFenceHighlightFilter implements HighlightInfoFilter {
    @Override
    public boolean accept(HighlightInfo highlightInfo, @Nullable PsiFile file) {
        if (file == null || highlightInfo.getSeverity().compareTo(HighlightSeverity.WEAK_WARNING) < 0) {
            return true;
        }

        InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(file.getProject());
        if (!injectedLanguageManager.isInjectedFragment(file)) {
            return true;
        }
        PsiLanguageInjectionHost host = injectedLanguageManager.getInjectionHost(file);
        return host == null || MarkdownDocumentationProvider.findByCodeFence(host) == null;
    }
}
