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
package org.intellij.markdown.flavours.gfm;

import consulo.language.ast.ASTNode;
import org.intellij.markdown.MarkdownAstUtil;

import org.intellij.markdown.html.GeneratingProvider;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.markdown.html.entities.EntityConverter;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Special version of CodeSpanGeneratingProvider that will correctly escape
 * table pipes if the code span is inside a table cell.
 */
public class TableAwareCodeSpanGeneratingProvider implements GeneratingProvider {
    private static final Pattern NEWLINE_PATTERN = Pattern.compile("\\r\\n?|\\n");

    @Override
    public void processNode(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
        boolean isInsideTable = isInsideTable(node);
        List<ASTNode> nodes = collectContentNodes(node);
        StringBuilder sb = new StringBuilder();
        for (ASTNode child : nodes) {
            sb.append(replaceNewLines(processChild(child, text, isInsideTable)));
        }
        String output = trimForCodeSpan(sb.toString());
        visitor.consumeTagOpen(node, "code");
        visitor.consumeHtml(output);
        visitor.consumeTagClose("code");
    }

    /** From GFM spec: First, line endings are converted to spaces. */
    protected CharSequence replaceNewLines(CharSequence s) {
        return NEWLINE_PATTERN.matcher(s).replaceAll(" ");
    }

    /**
     * From GFM spec:
     * If the resulting string both begins and ends with a space character,
     * but does not consist entirely of space characters,
     * a single space character is removed from the front and back.
     */
    protected String trimForCodeSpan(String s) {
        if (s.trim().isEmpty()) {
            return s;
        }
        if (s.startsWith(" ") && s.endsWith(" ")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    protected boolean isInsideTable(ASTNode node) {
        return MarkdownAstUtil.getParentOfType(node, GFMTokenTypes.CELL) != null;
    }

    protected List<ASTNode> collectContentNodes(ASTNode node) {
        if (MarkdownAstUtil.children(node).size() < 2) {
            throw new IllegalStateException("Code span node must have at least 2 children");
        }
        return MarkdownAstUtil.children(node).subList(1, MarkdownAstUtil.children(node).size() - 1);
    }

    protected CharSequence processChild(ASTNode node, String text, boolean isInsideTable) {
        if (!isInsideTable) {
            return HtmlGenerator.leafText(text, node, false);
        }
        String nodeText = MarkdownAstUtil.getTextInNode(node, text).toString();
        String escaped = nodeText.replace("\\|", "|");
        return EntityConverter.replaceEntities(escaped, false, false);
    }
}
