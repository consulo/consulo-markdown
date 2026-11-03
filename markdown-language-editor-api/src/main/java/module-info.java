/**
 * Editor support of the Markdown language which other languages reuse - the syntax highlighter and its colors.
 */
module consulo.markdown.language.editor.api {
    requires transitive consulo.markdown.language.api;
    requires transitive consulo.language.editor.api;

    exports org.intellij.plugins.markdown.highlighting;
}
