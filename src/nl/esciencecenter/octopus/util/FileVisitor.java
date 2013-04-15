/*
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
package nl.esciencecenter.octopus.util;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.AbsolutePath;

public interface FileVisitor {

    /**
     * Invoked for a directory after entries in the directory, and all of their descendants, have been visited.
     */
    FileVisitResult postVisitDirectory(AbsolutePath dir, OctopusIOException exception, Octopus octopus) throws OctopusIOException;

    /**
     * Invoked for a directory before entries in the directory are visited.
     */
    FileVisitResult preVisitDirectory(AbsolutePath dir, FileAttributes attributes, Octopus octopus) throws OctopusIOException;

    /**
     * Invoked for a file in a directory.
     */
    FileVisitResult visitFile(AbsolutePath file, FileAttributes attributes, Octopus octopus) throws OctopusIOException;

    /**
     * Invoked for a file that could not be visited.
     */
    FileVisitResult visitFileFailed(AbsolutePath file, OctopusIOException exception, Octopus octopus) throws OctopusIOException;
}
