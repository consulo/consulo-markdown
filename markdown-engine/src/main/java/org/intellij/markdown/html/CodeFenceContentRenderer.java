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
package org.intellij.markdown.html;

import org.jspecify.annotations.Nullable;

/**
 * Renders the code of a code fence as HTML, for example highlighted by the language of its info string
 */
@FunctionalInterface
public interface CodeFenceContentRenderer {
    /**
     * @param infoString the whole info string after the opening backticks, empty if there is none
     * @param code       the code of the fence, without its indentation and not escaped
     * @return the HTML inside the {@code <code>} tag, or {@code null} to show the code as plain text
     */
    @Nullable
    CharSequence render(String infoString, String code);

    /**
     * @param infoString the whole info string after the opening backticks, empty if there is none
     * @param code       the code of the fence, without its indentation and not escaped
     * @return the {@code id} of the {@code <pre>} of the fence, or {@code null} for none
     */
    @Nullable
    default String getBlockId(String infoString, String code) {
        return null;
    }
}
