/**
 * The Markdown language: file type, element types, PSI interfaces and the code fence language guessing.
 */
module consulo.markdown.language.api {
    requires transitive consulo.markdown.engine;

    exports consulo.markdown.icon;
    exports consulo.markdown.localize;
    exports consulo.markdown.injection;
    exports org.intellij.plugins.markdown.injection.aliases;
    exports org.intellij.plugins.markdown.lang;
    exports org.intellij.plugins.markdown.lang.lexer;
    exports org.intellij.plugins.markdown.lang.psi;
}
