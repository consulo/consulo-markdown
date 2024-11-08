package org.intellij.plugins.markdown.structureView;

import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.fileEditor.structureView.tree.SortableTreeElement;
import consulo.fileEditor.structureView.tree.TreeElement;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.impl.psi.PsiFileImpl;
import consulo.language.psi.PsiElement;
import consulo.navigation.ItemPresentation;
import consulo.navigation.NavigationItem;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.StringUtil;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownBlockQuoteImpl;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCompositePsiElementBase;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownFile;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownListItemImpl;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MarkdownStructureElement implements StructureViewTreeElement, SortableTreeElement {
  private static final ItemPresentation DUMMY_PRESENTATION = new MarkdownBasePresentation() {
    @Nullable
    @Override
    public String getPresentableText() {
      return null;
    }

    @Nullable
    @Override
    public String getLocationString() {
      return null;
    }
  };

  static final TokenSet PRESENTABLE_TYPES =
    TokenSet.orSet(MarkdownTokenTypeSets.HEADERS,
                   TokenSet.create(MarkdownElementTypes.PARAGRAPH,
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
                                   MarkdownElementTypes.TABLE_CELL));

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
  public void navigate(boolean requestFocus) {
    if (myElement instanceof NavigationItem) {
      ((NavigationItem)myElement).navigate(requestFocus);
    }
  }

  @Override
  public boolean canNavigate() {
    return myElement instanceof NavigationItem && ((NavigationItem)myElement).canNavigate();
  }

  @Override
  public boolean canNavigateToSource() {
    return myElement instanceof NavigationItem && ((NavigationItem)myElement).canNavigateToSource();
  }

  @Nonnull
  @Override
  public String getAlphaSortKey() {
    return StringUtil.notNullize(myElement instanceof NavigationItem ? ((NavigationItem)myElement).getName() : null);
  }

  @Nonnull
  @Override
  public ItemPresentation getPresentation() {
    final ItemPresentation result = getPresentationImpl();

    if (result != null) {
      return result;
    }
    else {
      return DUMMY_PRESENTATION;
    }
  }

  @Nullable
  private ItemPresentation getPresentationImpl() {
    if (myElement instanceof PsiFileImpl) {
      return ((PsiFileImpl)myElement).getPresentation();
    }
    if (myElement instanceof NavigationItem) {
      return ((NavigationItem)myElement).getPresentation();
    }

    return null;
  }

  @Nonnull
  @Override
  public TreeElement[] getChildren() {
    final PsiElement parentToTraverse = myElement instanceof MarkdownFile ? myElement.getFirstChild() : myElement;

    if (hasTrivialChild(parentToTraverse)) return EMPTY_ARRAY;

    List<TreeElement> result = new ArrayList<TreeElement>();
    for (PsiElement element : parentToTraverse.getChildren()) {
      final IElementType type = element.getNode().getElementType();
      if (!PRESENTABLE_TYPES.contains(type)) {
        continue;
      }

      result.add(new MarkdownStructureElement(element));
    }
    return ArrayUtil.toObjectArray(result, TreeElement.class);
  }

  public static boolean hasTrivialChild(@Nonnull PsiElement parentToTraverse) {
    if ((parentToTraverse instanceof MarkdownListItemImpl
         || parentToTraverse instanceof MarkdownBlockQuoteImpl) &&
        ((MarkdownCompositePsiElementBase)parentToTraverse).hasTrivialChildren()) {
      return true;
    }
    return false;
  }
}
