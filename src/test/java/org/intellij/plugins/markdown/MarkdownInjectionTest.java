package org.intellij.plugins.markdown;

import org.intellij.plugins.markdown.lang.MarkdownFileType;
import org.intellij.plugins.markdown.lang.MarkdownLanguage;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

public abstract class MarkdownInjectionTest extends LightPlatformCodeInsightFixtureTestCase
{
  public void testFenceWithLang() {
    doTest("```java\n" +
           "{\"foo\":\n" +
           "  <caret>\n" +
           "  bar\n" +
           "}\n" +
           "```", true);
  }

  private void doTest(String text, boolean shouldHaveInjection) {
    final PsiFile file = myFixture.configureByText(MarkdownFileType.INSTANCE, text);
    assertEquals(shouldHaveInjection, !file.findElementAt(myFixture.getCaretOffset()).getLanguage().isKindOf(MarkdownLanguage.INSTANCE));
  }
}
