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

import org.intellij.markdown.parser.LookaheadText;
import org.intellij.markdown.parser.constraints.CommonMarkdownConstraints;

public final class GFMConstraints extends CommonMarkdownConstraints {
    private final boolean myIsCheckbox;

    private GFMConstraints(int[] indents, char[] types, boolean[] isExplicit, int charsEaten, boolean isCheckbox) {
        super(indents, types, isExplicit, charsEaten);
        myIsCheckbox = isCheckbox;
    }

    public static final GFMConstraints BASE = new GFMConstraints(new int[0], new char[0], new boolean[0], 0, false);

    @Override
    protected CommonMarkdownConstraints getBase() {
        return BASE;
    }

    @Override
    protected CommonMarkdownConstraints createNewConstraints(int[] indents, char[] types, boolean[] isExplicit, int charsEaten) {
        char initialType = types[types.length - 1];
        char originalType = toOriginalType(initialType);
        types[types.length - 1] = originalType;
        return new GFMConstraints(indents, types, isExplicit, charsEaten, initialType != originalType);
    }

    public boolean hasCheckbox() {
        return myIsCheckbox;
    }

    @Override
    protected ListMarkerInfo fetchListMarker(LookaheadText.Position pos) {
        ListMarkerInfo baseMarkerInfo = super.fetchListMarker(pos);
        if (baseMarkerInfo == null) {
            return null;
        }

        String line = pos.getCurrentLine();
        int offset = pos.getOffsetInCurrentLine() + baseMarkerInfo.getMarkerLength();

        while (offset < line.length() && (line.charAt(offset) == ' ' || line.charAt(offset) == '\t')) {
            offset++;
        }

        if (offset + 3 <= line.length()
                && line.charAt(offset) == '['
                && line.charAt(offset + 2) == ']'
                && (line.charAt(offset + 1) == 'x' || line.charAt(offset + 1) == 'X' || line.charAt(offset + 1) == ' ')) {
            return new ListMarkerInfo(offset + 3 - pos.getOffsetInCurrentLine(),
                    toCheckboxType(baseMarkerInfo.getMarkerType()),
                    baseMarkerInfo.getMarkerLength());
        } else {
            return baseMarkerInfo;
        }
    }

    private static char toCheckboxType(char originalType) {
        return (char) (originalType + 100);
    }

    private static char toOriginalType(char checkboxType) {
        if (checkboxType < 128) {
            return checkboxType;
        }
        return (char) (checkboxType - 100);
    }
}
