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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

class TablesGeneratingProvider implements GeneratingProvider {

    private static final Pattern SPLIT_REGEX = Pattern.compile("\\|");

    public enum Alignment {
        LEFT("left", true),
        CENTER("center", false),
        RIGHT("right", false);

        private final String myHtmlName;
        private final boolean myIsDefault;

        Alignment(String htmlName, boolean isDefault) {
            myHtmlName = htmlName;
            myIsDefault = isDefault;
        }

        public String getHtmlName() {
            return myHtmlName;
        }

        public boolean isDefault() {
            return myIsDefault;
        }
    }

    public static final Alignment DEFAULT_ALIGNMENT;

    static {
        Alignment found = null;
        for (Alignment a : Alignment.values()) {
            if (a.isDefault()) {
                found = a;
                break;
            }
        }
        if (found == null) {
            throw new IllegalStateException("Must be default alignment");
        }
        DEFAULT_ALIGNMENT = found;
    }

    @Override
    public void processNode(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
        assert node.getElementType() == GFMElementTypes.TABLE;

        List<Alignment> alignmentInfo = getAlignmentInfo(text, node);
        int rowsPopulated = 0;

        visitor.consumeTagOpen(node, "table");
        for (ASTNode child : MarkdownAstUtil.children(node)) {
            if (child.getElementType() == GFMElementTypes.HEADER) {
                visitor.consumeHtml("<thead>");
                populateRow(visitor, child, "th", alignmentInfo, -1);
                visitor.consumeHtml("</thead>");
            } else if (child.getElementType() == GFMElementTypes.ROW) {
                if (rowsPopulated == 0) {
                    visitor.consumeHtml("<tbody>");
                }
                rowsPopulated++;
                populateRow(visitor, child, "td", alignmentInfo, rowsPopulated);
            }
        }
        if (rowsPopulated > 0) {
            visitor.consumeHtml("</tbody>");
        }
        visitor.consumeTagClose("table");
    }

    private void populateRow(
            HtmlGenerator.HtmlGeneratingVisitor visitor,
            ASTNode node,
            String cellName,
            List<Alignment> alignmentInfo,
            int rowNumber
    ) {
        String parityAttribute = (rowNumber > 0 && rowNumber % 2 == 0)
                ? "class=\"intellij-row-even\"" : null;

        visitor.consumeTagOpen(node, "tr", parityAttribute);

        int cellIndex = 0;
        for (ASTNode child : MarkdownAstUtil.children(node)) {
            if (child.getElementType() != GFMTokenTypes.CELL) {
                continue;
            }
            if (cellIndex >= alignmentInfo.size()) {
                throw new IllegalStateException("Too many cells in a row! Should check parser.");
            }

            Alignment alignment = alignmentInfo.get(cellIndex);
            String alignmentAttribute = alignment.isDefault() ? null : "align=\"" + alignment.getHtmlName() + "\"";

            visitor.consumeTagOpen(child, cellName, alignmentAttribute);
            visitor.visitNode(child);
            visitor.consumeTagClose(cellName);
            cellIndex++;
        }

        int cellCount = cellIndex;
        for (int i = cellCount; i <= alignmentInfo.size() - 1; i++) {
            visitor.consumeHtml("<td></td>");
        }
        visitor.consumeTagClose("tr");
    }

    private List<Alignment> getAlignmentInfo(String text, ASTNode node) {
        ASTNode separatorRow = MarkdownAstUtil.findChildOfType(node, GFMTokenTypes.TABLE_SEPARATOR);
        if (separatorRow == null) {
            throw new IllegalStateException("Could not find table separator");
        }

        List<Alignment> result = new ArrayList<>();

        String[] cells = SPLIT_REGEX.split(MarkdownAstUtil.getTextInNode(separatorRow, text));
        for (int i = 0; i < cells.length; i++) {
            String cell = cells[i];
            if (!cell.trim().isEmpty() || (i > 0 && i < cells.length - 1)) {
                String trimmed = cell.trim();
                boolean starts = trimmed.startsWith(":");
                boolean ends = trimmed.endsWith(":");
                if (starts && ends) {
                    result.add(Alignment.CENTER);
                } else if (starts) {
                    result.add(Alignment.LEFT);
                } else if (ends) {
                    result.add(Alignment.RIGHT);
                } else {
                    result.add(DEFAULT_ALIGNMENT);
                }
            }
        }
        return result;
    }
}
