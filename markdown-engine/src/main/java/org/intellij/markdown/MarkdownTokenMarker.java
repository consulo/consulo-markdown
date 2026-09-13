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
package org.intellij.markdown;

/**
 * Implemented by markdown element types that stand for a single token rather than a composite.
 * <p>
 * The parser turns these into leaf nodes directly instead of descending into children
 * (see {@code ProductionWalker}). It exists as an interface because
 * the token-ness of a type is independent of whether it is also lazily reparsed, and Java has
 * no multiple inheritance to express both on one class.
 */
public interface MarkdownTokenMarker {
    boolean isToken();
}
