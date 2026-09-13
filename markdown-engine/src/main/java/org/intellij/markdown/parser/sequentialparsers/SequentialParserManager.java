/*
 * Copyright 2016 JetBrains s.r.o.
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
package org.intellij.markdown.parser.sequentialparsers;

import org.intellij.markdown.ExperimentalApi;
import org.intellij.markdown.parser.CancellationToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class SequentialParserManager {

    
    public abstract List<SequentialParser> getParserSequence();

    
    public Collection<SequentialParser.Node> runParsingSequence(
            TokensCache tokensCache,
            List<IntRange> rangesToParse
    ) {
        return runParsingSequence(tokensCache, rangesToParse, CancellationToken.NON_CANCELLABLE);
    }

    @ExperimentalApi
    
    public Collection<SequentialParser.Node> runParsingSequence(
            TokensCache tokensCache,
            List<IntRange> rangesToParse,
            CancellationToken cancellationToken
    ) {
        ArrayList<SequentialParser.Node> result = new ArrayList<>();

        ArrayList<List<IntRange>> parsingSpaces = new ArrayList<>();
        parsingSpaces.add(rangesToParse);

        for (SequentialParser sequentialParser : getParserSequence()) {
            cancellationToken.checkCancelled();
            ArrayList<List<IntRange>> nextLevelSpaces = new ArrayList<>();

            for (List<IntRange> parsingSpace : parsingSpaces) {
                SequentialParser.ParsingResult currentResult = sequentialParser.parse(tokensCache, parsingSpace);
                result.addAll(currentResult.getParsedNodes());
                nextLevelSpaces.addAll(currentResult.getRangesToProcessFurther());
            }

            parsingSpaces = nextLevelSpaces;
        }

        return result;
    }
}
