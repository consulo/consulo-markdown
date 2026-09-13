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
package org.intellij.markdown.html;

import consulo.language.ast.ASTNode;

public class SimpleTagProvider extends OpenCloseGeneratingProvider {
    private final String myTagName;

    public SimpleTagProvider(String tagName) {
        myTagName = tagName;
    }

    public String getTagName() {
        return myTagName;
    }

    @Override
    public void openTag(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
        visitor.consumeTagOpen(node, myTagName);
    }

    @Override
    public void closeTag(HtmlGenerator.HtmlGeneratingVisitor visitor, String text, ASTNode node) {
        visitor.consumeTagClose(myTagName);
    }
}
