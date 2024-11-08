package org.intellij.plugins.markdown.editor;

import jakarta.annotation.Nonnull;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

public abstract class MarkdownQuoteHandlerTest extends LightPlatformCodeInsightFixtureTestCase
{
  private void doTest(@Nonnull String text, char charToType, @Nonnull String expectedResult) {
    final PsiFile file = myFixture.configureByText("test.md", text);
    assertInstanceOf(file, MarkdownFile.class);
    
    myFixture.type(charToType);
    myFixture.checkResult(expectedResult);
  }
  
  public void testSingleQuote() {
    doTest("Hello <caret> world", '\'', "Hello '<caret>' world");
  }

  public void testSingleQuoteBeforeWord() {
    doTest("Hello <caret>world", '\'', "Hello '<caret>world");
  }

  public void testSingleQuoteAtEof() {
    doTest("Hello <caret>", '\'', "Hello '<caret>'");
  }

  public void testDoubleQuote() {
    doTest("Hello <caret> world", '"', "Hello \"<caret>\" world");
  }

  public void testBacktick() {
    doTest("Hello <caret> world", '`', "Hello `<caret>` world");
  }

  public void testEmphAsterisk() {
    doTest("Hello <caret> world", '*', "Hello *<caret> world");
  }

  public void testEmphUnderscore() {
    doTest("Hello <caret> world", '_', "Hello _<caret> world");
  }

  public void testSingleQuoteAsApostrophe() {
    doTest("Hello dear<caret> world", '\'', "Hello dear\'<caret> world");
  }

  public void testBacktickAsAccent() {
    doTest("Hello dear<caret> world", '`', "Hello dear`<caret> world");
  }
  
  public void testClosingQuote() {
    doTest("Hello '<caret>' world", '\'', "Hello ''<caret> world");
  }

  public void testClosingQuoteWithWord() {
    doTest("Hello 'cool<caret>' world", '\'', "Hello 'cool'<caret> world");
  }
  
  public void testBacktickShouldBeAdded() {
    doTest("Hello `<caret>` world", '`', "Hello ``<caret>` world");
  }

  public void testBackticksGoodBalance() {
    doTest("Hello ``code<caret>`` world", '`', "Hello ``code`<caret>` world");
  }

  public void testBackticksGoodBalance2() {
    doTest("Hello ``code`<caret>` world", '`', "Hello ``code``<caret> world");
  }

  public void testBackticksBadBalance() {
    doTest("Hello ``code<caret>` world", '`', "Hello ``code`<caret>` world");
  }
}
