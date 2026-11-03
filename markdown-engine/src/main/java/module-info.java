/**
 * The JetBrains markdown parsing engine, vendored and translated from Kotlin to Java.
 * <p>
 * Upstream: https://github.com/JetBrains/markdown — the package layout is kept identical to
 * upstream so future upstream commits stay readable against this tree. Unlike upstream, element
 * types are {@link consulo.language.ast.IElementType} rather than a parallel type hierarchy.
 * <p>
 * Only the parser - the Markdown language, its PSI and its editor support are in the {@code consulo.markdown.language.*} modules.
 */
module consulo.markdown.engine {
    requires transitive consulo.language.api;

    exports org.intellij.markdown;
    exports org.intellij.markdown.flavours;
    exports org.intellij.markdown.flavours.commonmark;
    exports org.intellij.markdown.flavours.gfm;
    exports org.intellij.markdown.flavours.gfm.lexer;
    exports org.intellij.markdown.flavours.gfm.table;
    exports org.intellij.markdown.html;
    exports org.intellij.markdown.html.entities;
    exports org.intellij.markdown.lexer;
    exports org.intellij.markdown.parser;
    exports org.intellij.markdown.parser.constraints;
    exports org.intellij.markdown.parser.markerblocks;
    exports org.intellij.markdown.parser.markerblocks.impl;
    exports org.intellij.markdown.parser.markerblocks.providers;
    exports org.intellij.markdown.parser.sequentialparsers;
    exports org.intellij.markdown.parser.sequentialparsers.impl;
}
