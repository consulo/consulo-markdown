/*
 * Copyright 2016 JetBrains s.r.o.
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
package org.intellij.markdown;

import consulo.language.ast.ASTNode;
import consulo.language.ast.ILazyParseableElementType;
import consulo.language.lexer.Lexer;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiBuilderFactory;
import consulo.language.psi.PsiElement;
import consulo.language.version.LanguageVersionUtil;
import consulo.project.Project;
import org.intellij.markdown.lexer.MarkdownLexerAdapter;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.markdown.parser.MarkdownParserManager;
import org.intellij.markdown.parser.PsiBuilderSink;

/**
 * Element type whose inline content is reparsed on demand.
 * <p>
 * Applied to the four types whose children are inline structure rather than block structure:
 * {@code PARAGRAPH}, {@code ATX_CONTENT}, {@code SETEXT_CONTENT} and {@code CELL}.
 */
public class MarkdownLazyElementType extends ILazyParseableElementType implements MarkdownTokenMarker {
    private final boolean myIsToken;

    public MarkdownLazyElementType(String debugName, boolean isToken) {
        super(debugName, MarkdownLanguage.INSTANCE);
        myIsToken = isToken;
    }

    public MarkdownLazyElementType(String debugName) {
        this(debugName, false);
    }

    @Override
    public boolean isToken() {
        return myIsToken;
    }

    @Override
    public String toString() {
        return "Markdown:" + super.toString();
    }

    @Override
    protected ASTNode doParseContents(ASTNode chameleon, PsiElement psi) {
        Project project = psi.getProject();
        Lexer lexer = new MarkdownLexerAdapter();
        CharSequence chars = chameleon.getChars();

        PsiBuilder builder = PsiBuilderFactory.getInstance()
            .createBuilder(project,
                           chameleon,
                           lexer,
                           getLanguage(),
                           LanguageVersionUtil.findDefaultVersion(getLanguage()),
                           chars);
        assert builder.getCurrentOffset() == 0;
        new MarkdownParser(MarkdownParserManager.FLAVOUR)
            .parseInlineInto(chameleon.getElementType(), chars, 0, chars.length(), new PsiBuilderSink(builder));
        assert builder.eof();

        return builder.getTreeBuilt().getFirstChildNode();
    }
}
