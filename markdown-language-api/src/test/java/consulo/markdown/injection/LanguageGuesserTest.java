package consulo.markdown.injection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LanguageGuesserTest {
  @Test
  public void languageNameIsTheFirstWord() {
    assertEquals("php", LanguageGuesser.getLanguageName("php"));
    assertEquals("twig", LanguageGuesser.getLanguageName("twig title=\"base.html.twig\""));
    assertEquals("twig", LanguageGuesser.getLanguageName(" twig "));
    assertEquals("js", LanguageGuesser.getLanguageName("js{.line-numbers}"));
    assertEquals("yaml", LanguageGuesser.getLanguageName("yaml,linenums"));
    assertEquals("", LanguageGuesser.getLanguageName(""));
  }
}
