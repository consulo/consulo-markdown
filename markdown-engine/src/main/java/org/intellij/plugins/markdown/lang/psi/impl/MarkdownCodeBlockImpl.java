package org.intellij.plugins.markdown.lang.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;
import org.intellij.plugins.markdown.lang.psi.MarkdownPsiElement;
import org.intellij.plugins.markdown.lang.psi.MarkdownBasePresentation;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MarkdownCodeBlockImpl extends ASTWrapperPsiElement implements MarkdownPsiElement {
    public MarkdownCodeBlockImpl(@Nonnull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new MarkdownBasePresentation() {
            @Override
            public LocalizeValue getPresentableText() {
                return LocalizeValue.localizeTODO("Code block");
            }

            @Override
            @RequiredReadAction
            public @Nullable String getLocationString() {
                if (!isValid()) {
                    return null;
                }

                StringBuilder sb = new StringBuilder();
                for (PsiElement child = getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (child.getNode().getElementType() != MarkdownTokenTypes.CODE_LINE) {
                        continue;
                    }
                    if (sb.length() > 0) {
                        sb.append("\\n");
                    }
                    sb.append(child.getText());

                    if (sb.length() >= MarkdownCompositePsiElementBase.PRESENTABLE_TEXT_LENGTH) {
                        break;
                    }
                }

                return sb.toString();
            }
        };
    }

    @Nonnull
    @Override
    public List<MarkdownPsiElement> getCompositeChildren() {
        return Collections.emptyList();
    }
}
