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
package org.intellij.plugins.markdown.injection.aliases;

import consulo.language.Language;
import consulo.language.file.LanguageFileType;
import consulo.language.util.LanguageUtil;
import consulo.markdown.injection.LanguageGuesser;
import consulo.util.lang.Pair;
import org.jspecify.annotations.Nullable;

/**
 * Guesses the language of a code fence from its info string.
 * <p>
 * Ported from the JetBrains Markdown plugin. Consulo: the guessing itself is {@link LanguageGuesser} (JetBrains:
 * {@code CodeFenceLanguageProvider}s); the extension is the default one of the language's file type.
 */
public final class CodeFenceLanguageGuesser {
    private CodeFenceLanguageGuesser() {
    }

    /**
     * Guess the Language from a Markdown info string. It may either be a lower-cased id or one of the aliases.
     * <p>
     * The language is guaranteed to be safely injectable.
     */
    public static @Nullable Language guessLanguageForInjection(String value) {
        Pair<Language, @Nullable String> pair = guessLanguageWithExtensionForInjection(value);
        return pair == null ? null : pair.getFirst();
    }

    public static @Nullable Pair<Language, @Nullable String> guessLanguageWithExtensionForInjection(String value) {
        Language language = LanguageGuesser.EP_NAME.computeSafeIfAny(it -> it.guessLanguage(value));
        if (language == null || !LanguageUtil.isInjectableLanguage(language)) {
            return null;
        }
        LanguageFileType fileType = language.getAssociatedFileType();
        return Pair.create(language, fileType == null ? null : fileType.getDefaultExtension());
    }
}
