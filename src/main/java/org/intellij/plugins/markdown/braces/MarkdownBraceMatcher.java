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
package org.intellij.plugins.markdown.braces;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.BracePair;
import consulo.language.Language;
import consulo.language.PairedBraceMatcher;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.lang.MarkdownLanguage;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;

@ExtensionImpl
public class MarkdownBraceMatcher implements PairedBraceMatcher {
  @Override
  public BracePair[] getPairs() {
    return new BracePair[]{
      new BracePair(MarkdownTokenTypes.LPAREN, MarkdownTokenTypes.RPAREN, false),
      new BracePair(MarkdownTokenTypes.LBRACKET, MarkdownTokenTypes.RBRACKET, false),
      new BracePair(MarkdownTokenTypes.LT, MarkdownTokenTypes.GT, false),
      new BracePair(MarkdownTokenTypes.CODE_FENCE_START, MarkdownTokenTypes.CODE_FENCE_END, true)
    };
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return MarkdownLanguage.INSTANCE;
  }
}
