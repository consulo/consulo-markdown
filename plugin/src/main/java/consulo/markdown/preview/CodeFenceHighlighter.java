/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package consulo.markdown.preview;

import consulo.annotation.access.RequiredReadAction;
import consulo.colorScheme.EditorColorsScheme;
import consulo.execution.icon.ExecutionIconGroup;
import consulo.execution.runAnything.codeBlock.CodeBlockCommandRunner;
import consulo.execution.runAnything.codeBlock.CodeBlockRunner;
import consulo.language.Language;
import consulo.language.editor.codeBlock.CodeBlockHighlighter;
import consulo.language.editor.codeBlock.CodeBlockHtmlBuilder;
import consulo.markdown.injection.LanguageGuesser;
import consulo.markdown.localize.MarkdownLocalize;
import consulo.project.Project;
import consulo.ui.HtmlView;
import consulo.ui.clipboard.DataTransfer;
import consulo.ui.clipboard.DataTransferType;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import org.intellij.markdown.html.CodeFenceContentRenderer;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Highlights the code of a code fence in the preview as the editor colors the language of its info string, names every fence so
 * the preview puts a copy button over it, and marks what can be run in it. One instance serves one rendering of a document.
 */
public class CodeFenceHighlighter implements CodeFenceContentRenderer {
    private static final String BLOCK_ID_PREFIX = "consulo-code-fence-";

    private final Project myProject;
    private final EditorColorsScheme myColorsScheme;
    private final CodeBlockHighlighter myCodeBlockHighlighter;
    private final CodeBlockCommandRunner myCommandRunner;
    private final Map<String, DataTransfer> myCopyTargets = new LinkedHashMap<>();
    private final CodeFenceRunTargets myRunTargets;

    /**
     * The id {@link #getBlockId} gave the fence {@link #render} renders next
     */
    private @Nullable String myBlockId;

    public CodeFenceHighlighter(Project project, EditorColorsScheme colorsScheme, @Nullable VirtualFile workingDirectory) {
        myProject = project;
        myColorsScheme = colorsScheme;
        myCodeBlockHighlighter = project.getApplication().getInstance(CodeBlockHighlighter.class);
        myCommandRunner = project.getInstance(CodeBlockCommandRunner.class);
        myRunTargets = new CodeFenceRunTargets(workingDirectory);
    }

    /**
     * What the copy button of every fence rendered so far puts on the clipboard, by the id of its {@code pre}
     */
    public Map<String, DataTransfer> getCopyTargets() {
        return myCopyTargets;
    }

    public CodeFenceRunTargets getRunTargets() {
        return myRunTargets;
    }

    @Override
    @RequiredReadAction
    public @Nullable String getBlockId(String infoString, String code) {
        myBlockId = null;
        if (code.isEmpty()) {
            return null;
        }

        String blockId = BLOCK_ID_PREFIX + myCopyTargets.size();
        myBlockId = blockId;

        DataTransfer.Builder value = DataTransfer.builder().put(DataTransferType.TEXT, code);
        Language language = findLanguage(infoString);
        if (language != null) {
            String html = myCodeBlockHighlighter.newHtmlBuilder(myProject, language, code)
                .colorsScheme(myColorsScheme)
                .generateWrappedTags()
                .generateBackground()
                .generateFontFamily()
                .generateFontSize()
                .build();
            value.put(DataTransferType.HTML, html);
        }
        myCopyTargets.put(blockId, value.build());

        CodeBlockRunner runner = myCommandRunner.findBlockRunner(LanguageGuesser.getLanguageName(infoString), language);
        if (runner != null) {
            myRunTargets.addBlock(blockId, code, runner, new HtmlView.GutterAction(ExecutionIconGroup.gutterRun(), runner.getTitle()));
        }
        return blockId;
    }

    @Override
    @RequiredReadAction
    public @Nullable CharSequence render(String infoString, String code) {
        String blockId = myBlockId;
        myBlockId = null;

        Language language = findLanguage(infoString);
        Map<Integer, String> lineIds = blockId == null || myRunTargets.getGutterActions().containsKey(blockId)
            ? Map.of()
            : markRunnableLines(blockId, code);

        if (language == null) {
            return lineIds.isEmpty() ? null : escapeWithLineIds(code, lineIds);
        }

        CodeBlockHtmlBuilder builder = myCodeBlockHighlighter.newHtmlBuilder(myProject, language, code).colorsScheme(myColorsScheme);
        if (!lineIds.isEmpty()) {
            // every line starts a piece of text of its own, so the first piece of a runnable line becomes the element its button is next to
            builder.textHandler((startOffset, endOffset, resultBuffer, superHandler) -> {
                String lineId = lineIds.get(startOffset);
                if (lineId == null) {
                    superHandler.run();
                    return;
                }
                resultBuffer.append("<span id=\"").append(lineId).append("\">");
                superHandler.run();
                resultBuffer.append("</span>");
            });
        }
        return builder.build();
    }

    /**
     * @return the id of every runnable line, by the offset its text starts at
     */
    @RequiredReadAction
    private Map<Integer, String> markRunnableLines(String blockId, String code) {
        Map<Integer, String> lineIds = new LinkedHashMap<>();
        int lineStart = 0;
        int lineNumber = 0;
        while (lineStart < code.length()) {
            int lineEnd = code.indexOf('\n', lineStart);
            if (lineEnd < 0) {
                lineEnd = code.length();
            }
            String line = code.substring(lineStart, lineEnd);
            if (!line.isBlank() && myCommandRunner.canRunLine(line, myRunTargets.getWorkingDirectory(), false)) {
                String lineId = blockId + "-line-" + lineNumber;
                lineIds.put(lineStart, lineId);
                String command = CodeBlockCommandRunner.trimPrompt(line).trim();
                myRunTargets.addLine(lineId, line, new HtmlView.GutterAction(
                    ExecutionIconGroup.gutterRun(),
                    MarkdownLocalize.markdownPreviewRunCommand(command)
                ));
            }
            lineStart = lineEnd + 1;
            lineNumber++;
        }
        return lineIds;
    }

    private static String escapeWithLineIds(String code, Map<Integer, String> lineIds) {
        StringBuilder html = new StringBuilder(code.length() + lineIds.size() * 40);
        int lineStart = 0;
        while (lineStart < code.length()) {
            int lineEnd = code.indexOf('\n', lineStart);
            int next = lineEnd < 0 ? code.length() : lineEnd + 1;
            String line = StringUtil.escapeXmlEntities(code.substring(lineStart, next));
            String lineId = lineIds.get(lineStart);
            if (lineId != null) {
                html.append("<span id=\"").append(lineId).append("\">").append(line).append("</span>");
            }
            else {
                html.append(line);
            }
            lineStart = next;
        }
        return html.toString();
    }

    private @Nullable Language findLanguage(String infoString) {
        if (infoString.isEmpty()) {
            return null;
        }

        Language language = myProject.getApplication()
            .getExtensionPoint(LanguageGuesser.class)
            .computeSafeIfAny(guesser -> guesser.guessLanguage(infoString));
        return language != null ? language : myCodeBlockHighlighter.guessLanguage(LanguageGuesser.getLanguageName(infoString));
    }
}
