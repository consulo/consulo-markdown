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
package org.intellij.plugins.markdown.lang.parser;

import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiParser;
import consulo.language.version.LanguageVersion;
import org.jetbrains.annotations.NotNull;

public class MarkdownParserAdapter implements PsiParser {
  @NotNull
  public ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder builder, LanguageVersion languageVersion) {

    PsiBuilder.Marker rootMarker = builder.mark();

    final org.intellij.markdown.ast.ASTNode parsedTree = MarkdownParserManager.parseContent(builder.getOriginalText());

    assert builder.getCurrentOffset() == 0;
    new PsiBuilderFillingVisitor(builder).visitNode(parsedTree);
    assert builder.eof();

    rootMarker.done(root);

    return builder.getTreeBuilt();
  }
}
