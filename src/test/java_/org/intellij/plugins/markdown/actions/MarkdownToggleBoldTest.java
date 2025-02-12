package org.intellij.plugins.markdown.actions;

import com.intellij.testFramework.LightPlatformCodeInsightTestCase;
import org.intellij.plugins.markdown.MarkdownTestingUtil;
import jakarta.annotation.Nonnull;

public abstract class MarkdownToggleBoldTest extends LightPlatformCodeInsightTestCase {

  public void testSimple() {
    doTest();
  }

  private void doTest() {
    configureByFile(getTestName(true) + "_before.md");
    executeAction("org.intellij.plugins.markdown.ui.actions.styling.ToggleBoldAction");
    checkResultByFile(getTestName(true) + "_after.md");
  }

  @Nonnull
  @Override
  protected String getTestDataPath() {
    return MarkdownTestingUtil.TEST_DATA_PATH + "/actions/toggleBold/";
  }
}
