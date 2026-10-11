package org.intellij.plugins.markdown.structureView;

import consulo.annotation.access.RequiredReadAction;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.fileEditor.structureView.tree.SortableTreeElement;
import consulo.fileEditor.structureView.tree.TreeElement;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.impl.psi.PsiFileImpl;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.navigation.NavigationItem;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.StringUtil;
import org.intellij.plugins.markdown.lang.psi.MarkdownBasePresentation;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownBlockQuoteImpl;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCompositePsiElementBase;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownFile;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownListItemImpl;
import jakarta.annotation.Nonnull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MarkdownStructureElement implements StructureViewTreeElement, SortableTreeElement {
    private static final ItemPresentation DUMMY_PRESENTATION = new MarkdownBasePresentation() {
        @Override
        public LocalizeValue getPresentableText() {
            return LocalizeValue.empty();
        }

        @Override
        public @Nullable String getLocationString() {
            return null;
        }
    };

    static final TokenSet PRESENTABLE_TYPES =
        TokenSet.orSet(
            MarkdownTokenTypeSets.HEADERS,
            TokenSet.create(
                MarkdownElementTypes.PARAGRAPH,
                MarkdownElementTypes.ORDERED_LIST,
                MarkdownElementTypes.UNORDERED_LIST,
                MarkdownElementTypes.LIST_ITEM,
                MarkdownElementTypes.BLOCK_QUOTE,
                MarkdownElementTypes.CODE_FENCE,
                MarkdownElementTypes.CODE_BLOCK,
                MarkdownElementTypes.HTML_BLOCK,
                MarkdownElementTypes.LINK_DEFINITION,
                MarkdownElementTypes.TABLE,
                MarkdownElementTypes.TABLE_HEADER,
                MarkdownElementTypes.TABLE_ROW,
                MarkdownElementTypes.TABLE_CELL
            )
        );

    @Nonnull
    private final PsiElement myElement;

    public MarkdownStructureElement(@Nonnull PsiElement element) {
        myElement = element;
    }

    @Override
    public Object getValue() {
        return myElement;
    }

    @Override
    @RequiredReadAction
    public void navigate(boolean requestFocus) {
        if (myElement instanceof NavigationItem navItem) {
            navItem.navigate(requestFocus);
        }
    }

    @Override
    @RequiredReadAction
    public boolean canNavigate() {
        return myElement instanceof NavigationItem navItem && navItem.canNavigate();
    }

    @Override
    @RequiredReadAction
    public boolean canNavigateToSource() {
        return myElement instanceof NavigationItem navItem && navItem.canNavigateToSource();
    }

    @Nonnull
    @Override
    public String getAlphaSortKey() {
        return StringUtil.notNullize(myElement instanceof NavigationItem navItem ? navItem.getName() : null);
    }

    @Nonnull
    @Override
    public ItemPresentation getPresentation() {
        ItemPresentation result = getPresentationImpl();
        return result != null ? result : DUMMY_PRESENTATION;
    }

    @Nullable
    private ItemPresentation getPresentationImpl() {
        if (myElement instanceof PsiFileImpl file) {
            return file.getPresentation();
        }
        if (myElement instanceof NavigationItem navItem) {
            return navItem.getPresentation();
        }

        return null;
    }

    @Nonnull
    @Override
    @RequiredReadAction
    public TreeElement[] getChildren() {
        PsiElement parentToTraverse = myElement instanceof MarkdownFile ? myElement.getFirstChild() : myElement;

        if (hasTrivialChild(parentToTraverse)) {
            return EMPTY_ARRAY;
        }

        List<TreeElement> result = new ArrayList<>();
        for (PsiElement element : parentToTraverse.getChildren()) {
            IElementType type = element.getNode().getElementType();
            if (!PRESENTABLE_TYPES.contains(type)) {
                continue;
            }

            result.add(new MarkdownStructureElement(element));
        }
        return ArrayUtil.toObjectArray(result, TreeElement.class);
    }

    @RequiredReadAction
    public static boolean hasTrivialChild(@Nonnull PsiElement parentToTraverse) {
        return (parentToTraverse instanceof MarkdownListItemImpl || parentToTraverse instanceof MarkdownBlockQuoteImpl)
            && ((MarkdownCompositePsiElementBase) parentToTraverse).hasTrivialChildren();
    }
}
