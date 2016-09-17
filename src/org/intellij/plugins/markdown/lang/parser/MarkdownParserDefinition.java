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


import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.lexer.MarkdownToplevelLexer;
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiFactory;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownFile;
import org.jetbrains.annotations.NotNull;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import consulo.lang.LanguageVersion;

public class MarkdownParserDefinition implements ParserDefinition {

  @NotNull
  @Override
  public Lexer createLexer(@NotNull LanguageVersion languageVersion) {
    return new MarkdownToplevelLexer();
  }

  @NotNull
  @Override
  public PsiParser createParser(@NotNull LanguageVersion languageVersion) {
    return new MarkdownParserAdapter();
  }

  @NotNull
  @Override
  public IFileElementType getFileNodeType() {
    return MarkdownElementTypes.MARKDOWN_FILE_ELEMENT_TYPE;
  }

  @NotNull
  @Override
  public TokenSet getWhitespaceTokens(@NotNull LanguageVersion languageVersion) {
    return TokenSet.create();
  }

  @NotNull
  @Override
  public TokenSet getCommentTokens(@NotNull LanguageVersion languageVersion) {
    return TokenSet.EMPTY;
  }

  @NotNull
  @Override
  public TokenSet getStringLiteralElements(@NotNull LanguageVersion languageVersion) {
    return TokenSet.EMPTY;
  }

  @NotNull
  @Override
  public PsiElement createElement(ASTNode node) {
    return MarkdownPsiFactory.INSTANCE.createElement(node);
  }

  @NotNull
  @Override
  public PsiFile createFile(FileViewProvider viewProvider) {
    return new MarkdownFile(viewProvider);
  }

  @NotNull
  @Override
  public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY;
  }
}
