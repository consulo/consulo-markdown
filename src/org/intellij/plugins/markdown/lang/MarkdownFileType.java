/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package org.intellij.plugins.markdown.lang;

import javax.annotation.Nonnull;

import org.intellij.plugins.markdown.MarkdownBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.fileTypes.LanguageFileType;
import consulo.ui.image.Image;
import icons.MarkdownIcons;

public class MarkdownFileType extends LanguageFileType {
  public static final MarkdownFileType INSTANCE = new MarkdownFileType();

  protected MarkdownFileType() {
    super(MarkdownLanguage.INSTANCE);
  }

  @Nonnull
  @NotNull
  @Override
  public String getId() {
    return "Markdown";
  }

  @Nonnull
  @NotNull
  @Override
  public String getDescription() {
    return MarkdownBundle.message("markdown.file.type.description");
  }

  @Nonnull
  @NotNull
  @Override
  public String getDefaultExtension() {
    return "md";
  }

  @Nullable
  @Override
  public Image getIcon() {
    return MarkdownIcons.MarkdownPlugin;
  }
}
