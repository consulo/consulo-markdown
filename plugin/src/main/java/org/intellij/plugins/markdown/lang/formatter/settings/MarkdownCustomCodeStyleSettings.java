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
package org.intellij.plugins.markdown.lang.formatter.settings;

import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.CustomCodeStyleSettings;
import org.intellij.markdown.MarkdownLanguage;

/**
 * The code style settings of Markdown: blank lines around blocks, spaces, text wrapping and tables.
 * <p>
 * Read by the formatters of Markdown dialects (MDX); there is no settings page.
 */
@SuppressWarnings("FieldNamingConvention")
public class MarkdownCustomCodeStyleSettings extends CustomCodeStyleSettings {
    public int MAX_LINES_AROUND_HEADER = 1;
    public int MIN_LINES_AROUND_HEADER = 1;

    public int MAX_LINES_AROUND_BLOCK_ELEMENTS = 1;
    public int MIN_LINES_AROUND_BLOCK_ELEMENTS = 1;

    public int MAX_LINES_BETWEEN_PARAGRAPHS = 1;
    public int MIN_LINES_BETWEEN_PARAGRAPHS = 1;

    public boolean FORCE_ONE_SPACE_BETWEEN_WORDS = true;
    public boolean FORCE_ONE_SPACE_AFTER_HEADER_SYMBOL = true;
    public boolean FORCE_ONE_SPACE_AFTER_LIST_BULLET = true;
    public boolean FORCE_ONE_SPACE_AFTER_BLOCKQUOTE_SYMBOL = true;
    public boolean USE_FIXED_INDENTS_FOR_SUBLISTS = true;

    public boolean WRAP_TEXT_IF_LONG = true;
    public boolean KEEP_LINE_BREAKS_INSIDE_TEXT_BLOCKS = true;
    public boolean WRAP_TEXT_INSIDE_BLOCKQUOTES = true;
    public boolean INSERT_QUOTE_ARROWS_ON_WRAP = true;

    public boolean FORMAT_TABLES = true;

    public MarkdownCustomCodeStyleSettings(CodeStyleSettings settings) {
        super(MarkdownLanguage.INSTANCE.getID(), settings);
    }
}
