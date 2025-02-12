package org.intellij.plugins.markdown.actions;

import org.intellij.plugins.markdown.MarkdownTestingUtil;
import jakarta.annotation.Nonnull;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;

public abstract class MarkdownToggleItalicTest extends LightPlatformCodeInsightTestCase {

  public void testSimple() {
    doTest();
  }

  public void testSimpleNoSelection() {
    doTest();
  }

  public void testMultiCaretWordsOn() {
    doTest();
  }

  public void testMultiCaretWordsOff() {
    doTest();
  }

  public void testPartOfWordOn() {
    doTest();
  }

  public void testPartiallyOff() {
    doTest();
  }

  private void doTest() {
    configureByFile(getTestName(true) + "_before.md");
    executeAction("org.intellij.plugins.markdown.ui.actions.styling.ToggleItalicAction");
    checkResultByFile(getTestName(true) + "_after.md");
  }

  @Nonnull
  @Override
  protected String getTestDataPath() {
    return MarkdownTestingUtil.TEST_DATA_PATH + "/actions/toggleItalic/";
  }
}
