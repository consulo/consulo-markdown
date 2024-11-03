package org.intellij.plugins.markdown.settings;

import consulo.application.ApplicationManager;
import consulo.application.util.function.Computable;
import consulo.codeEditor.*;
import consulo.colorScheme.EditorColorsManager;
import consulo.component.util.pointer.NamedPointer;
import consulo.disposer.Disposable;
import consulo.document.Document;
import consulo.language.Language;
import consulo.language.LanguagePointerUtil;
import consulo.language.editor.highlight.HighlighterFactory;
import consulo.ui.color.ColorValue;
import consulo.ui.ex.awt.JBCheckBox;
import consulo.ui.util.ColorValueUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MarkdownCssSettingsForm implements MarkdownCssSettings.Holder, Disposable {
  private static final NamedPointer<Language> ourCSSLanguagePointer = LanguagePointerUtil.createPointer("CSS");

  private JPanel myMainPanel;
  private JBCheckBox myCssFromURIEnabled;
  private JTextField myCssURI;
  private JBCheckBox myApplyCustomCssText;
  private JPanel myEditorPanel;

  @Nonnull
  private String myCssText = "";
  @Nullable
  private Editor myEditor;
  @Nonnull
  private final ActionListener myUpdateListener;


  public MarkdownCssSettingsForm() {
    myUpdateListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        myCssURI.setEnabled(myCssFromURIEnabled.isSelected());
        if (myEditor != null && !myEditor.isDisposed()) {
          final boolean canEditCss = myApplyCustomCssText.isSelected();

          myEditor.getDocument().setReadOnly(!canEditCss);
          myEditor.getSettings().setCaretRowShown(canEditCss);

          ColorValue baseColor = myEditor.getColorsScheme().getDefaultBackground();
          if (canEditCss) {
            ((EditorEx)myEditor).setBackgroundColor(baseColor);
          }
          else {
            ((EditorEx)myEditor).setBackgroundColor(ColorValueUtil.isDark(baseColor) ? ColorValueUtil.brighter(baseColor) : ColorValueUtil.darker(
              baseColor));
          }
        }
      }
    };
    myCssFromURIEnabled.addActionListener(myUpdateListener);
    myApplyCustomCssText.addActionListener(myUpdateListener);
  }

  public JComponent getComponent() {
    return myMainPanel;
  }

  private void createUIComponents() {
    myEditorPanel = new JPanel(new BorderLayout());

    myEditor = createEditor();
    myEditorPanel.add(myEditor.getComponent(), BorderLayout.CENTER);
  }

  @Nonnull
  private static Editor createEditor() {
    EditorFactory editorFactory = EditorFactory.getInstance();
    Document editorDocument = editorFactory.createDocument("");
    EditorEx editor = (EditorEx)editorFactory.createEditor(editorDocument);
    fillEditorSettings(editor.getSettings());
    setHighlighting(editor);
    return editor;
  }

  private static void setHighlighting(EditorEx editor) {
    final Language language = ourCSSLanguagePointer.get();
    if (language == null) {
      return;
    }
    final EditorHighlighter editorHighlighter =
      HighlighterFactory.createHighlighter(language.getAssociatedFileType(), EditorColorsManager.getInstance().getGlobalScheme(), null);
    editor.setHighlighter(editorHighlighter);
  }

  private static void fillEditorSettings(final EditorSettings editorSettings) {
    editorSettings.setWhitespacesShown(false);
    editorSettings.setLineMarkerAreaShown(false);
    editorSettings.setIndentGuidesShown(false);
    editorSettings.setLineNumbersShown(true);
    editorSettings.setFoldingOutlineShown(false);
    editorSettings.setAdditionalColumnsCount(1);
    editorSettings.setAdditionalLinesCount(1);
    editorSettings.setUseSoftWraps(false);
  }

  @Override
  public void setMarkdownCssSettings(@Nonnull MarkdownCssSettings settings) {
    myCssFromURIEnabled.setSelected(settings.isUriEnabled());
    myCssURI.setText(settings.getStylesheetUri());
    myApplyCustomCssText.setSelected(settings.isTextEnabled());
    myCssText = settings.getStylesheetText();
    if (myEditor != null && !myEditor.isDisposed()) {
      /*ApplicationManager.getApplication().runWriteAction(new Runnable()
			{
				@Override
				public void run()
				{
					myEditor.getDocument().setText(myCssText);
				}
			});*/
    }

    //noinspection ConstantConditions
    myUpdateListener.actionPerformed(null);
  }

  @Nonnull
  @Override
  public MarkdownCssSettings getMarkdownCssSettings() {
    if (myEditor != null && !myEditor.isDisposed()) {
      myCssText = ApplicationManager.getApplication().runReadAction(new Computable<String>() {
        @Override
        public String compute() {
          return myEditor.getDocument().getText();
        }
      });
    }
    return new MarkdownCssSettings(myCssFromURIEnabled.isSelected(), myCssURI.getText(), myApplyCustomCssText.isSelected(), myCssText);
  }

  @Override
  public void dispose() {
    if (myEditor != null && !myEditor.isDisposed()) {
      EditorFactory.getInstance().releaseEditor(myEditor);
    }
    myEditor = null;
  }
}
