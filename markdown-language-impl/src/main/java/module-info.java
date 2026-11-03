/**
 * The Markdown PSI, its parser definition, and the conversion of Markdown to HTML.
 */
module consulo.markdown.language.impl {
    requires transitive consulo.markdown.language.api;
    requires consulo.language.impl;

    exports org.intellij.plugins.markdown.html;
    exports org.intellij.plugins.markdown.lang.parser;
    exports org.intellij.plugins.markdown.lang.parser.blocks;
    exports org.intellij.plugins.markdown.lang.psi.impl;
}
