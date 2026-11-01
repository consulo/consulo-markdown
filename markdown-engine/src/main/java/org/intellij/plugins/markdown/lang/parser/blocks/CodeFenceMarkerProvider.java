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
package org.intellij.plugins.markdown.lang.parser.blocks;

import org.intellij.markdown.parser.LookaheadText;
import org.intellij.markdown.parser.constraints.MarkdownConstraints;
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider;
import org.intellij.markdown.parser.markerblocks.providers.CodeFenceProvider;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extends default {@link CodeFenceProvider} to support additional MS Azure code fence syntax
 * for Mermaid.js fences.
 *
 * <pre>
 * ::: mermaid
 * &lt;mermaid diagram syntax&gt;
 * :::
 * </pre>
 * Ported from the JetBrains Markdown plugin.
 */
public class CodeFenceMarkerProvider extends CodeFenceProvider {
    private static final Pattern OPENING_REGEX = Pattern.compile("^ {0,3}(~~~++|```++|:::++)([^`]*+)$");

    @Override
    protected @Nullable OpeningInfo obtainFenceOpeningInfo(LookaheadText.Position pos, MarkdownConstraints constraints) {
        if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, constraints)) {
            return null;
        }
        Matcher matcher = OPENING_REGEX.matcher(pos.getCurrentLineFromPosition());
        if (!matcher.find()) {
            return null;
        }
        String delimiter = matcher.group(1);
        String info = matcher.group(2);
        if (delimiter.startsWith(":::") && !info.trim().toLowerCase(Locale.ROOT).equals("mermaid")) {
            return null;
        }
        return new OpeningInfo(delimiter, info);
    }
}
