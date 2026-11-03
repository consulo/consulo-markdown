/**
 * Markdown documentation comments of other languages - Java {@code ///}, Rust {@code ///} and {@code //!} and so on.
 * <p>
 * A language implements {@link consulo.markdown.documentation.MarkdownDocumentationProvider} for its comment, and reuses the
 * highlighting, the injection into code fences and the rendering of the Markdown implementation.
 */
module consulo.markdown.documentation.api {
    requires transitive consulo.markdown.language.impl;
    requires transitive consulo.markdown.language.editor.api;
    requires transitive consulo.language.editor.api;

    exports consulo.markdown.documentation;
    exports consulo.markdown.documentation.flavour;
}
