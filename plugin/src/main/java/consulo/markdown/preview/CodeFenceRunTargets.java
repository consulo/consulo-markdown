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

import consulo.execution.runAnything.codeBlock.CodeBlockCommandRunner;
import consulo.execution.runAnything.codeBlock.CodeBlockRunner;
import consulo.project.Project;
import consulo.ui.HtmlView;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What the run buttons of one rendering of a document run, by the id of the element each button is next to
 */
public final class CodeFenceRunTargets {
    public static final CodeFenceRunTargets EMPTY = new CodeFenceRunTargets(null);

    private record BlockRun(String code, CodeBlockRunner runner) {
    }

    private final @Nullable VirtualFile myWorkingDirectory;
    private final Map<String, HtmlView.GutterAction> myGutterActions = new LinkedHashMap<>();
    private final Map<String, String> myLines = new LinkedHashMap<>();
    private final Map<String, BlockRun> myBlocks = new LinkedHashMap<>();

    CodeFenceRunTargets(@Nullable VirtualFile workingDirectory) {
        myWorkingDirectory = workingDirectory;
    }

    void addLine(String targetId, String line, HtmlView.GutterAction action) {
        myLines.put(targetId, line);
        myGutterActions.put(targetId, action);
    }

    void addBlock(String targetId, String code, CodeBlockRunner runner, HtmlView.GutterAction action) {
        myBlocks.put(targetId, new BlockRun(code, runner));
        myGutterActions.put(targetId, action);
    }

    public @Nullable VirtualFile getWorkingDirectory() {
        return myWorkingDirectory;
    }

    public Map<String, HtmlView.GutterAction> getGutterActions() {
        return Map.copyOf(myGutterActions);
    }

    @RequiredUIAccess
    public void run(Project project, String targetId) {
        String line = myLines.get(targetId);
        if (line != null) {
            project.getInstance(CodeBlockCommandRunner.class).runLine(line, myWorkingDirectory, false);
            return;
        }

        BlockRun block = myBlocks.get(targetId);
        if (block != null) {
            block.runner().run(project, block.code(), myWorkingDirectory);
        }
    }
}
