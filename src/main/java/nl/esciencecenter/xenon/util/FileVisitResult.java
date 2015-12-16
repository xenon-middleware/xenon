/**
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.esciencecenter.xenon.util;

/**
 * FileVisitResult enumerates the possible results that can be returned by the methods of {@link FileVisitor}.
 * 
 * @version 1.0 
 * @since 1.0
 */
public enum FileVisitResult {

    /**
     * Continue.
     */
    CONTINUE,

    /**
     * Continue without visiting the siblings of this file or directory.
     */
    SKIP_SIBLINGS,

    /**
     * Continue without visiting the entries in this directory.
     */
    SKIP_SUBTREE,

    /**
     * Terminate.
     */
    TERMINATE,
}
