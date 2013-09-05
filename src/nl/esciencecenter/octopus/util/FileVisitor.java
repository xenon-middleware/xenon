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

import nl.esciencecenter.octopus.exceptions.OctopusIOException;

import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.Path;

/**
 * FileVisitor contains various callback methods called by 
 * {@link Utils#walkFileTree(Files, Path, boolean, int, FileVisitor) FileUtils.walkTree}.
 * 
 * By using an implementation of this interface in combination with 
 * {@link Utils#walkFileTree(Files, Path, boolean, int, FileVisitor) FileUtils.walkTree}, an action can be defined for 
 * each file and directory encountered during a tree walk.    
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0 
 * @since 1.0
 */
public interface FileVisitor {

    /**
     * Invoked for a directory after entries in the directory, and all of their descendants, have been visited.
     * 
     * @param dir 
     *          the directory that was visited.
     * @param exception 
     *          any exception thrown while visiting the directory, or <code>null</code> if there was no exception.
     * @param files 
     *          the {@link Files} used to access the directory.
     * 
     * @return the desired action. 
     * 
     * @throws OctopusIOException
     *          if an I/O error occurs while visiting the directory.
     */
    FileVisitResult postVisitDirectory(Path dir, OctopusIOException exception, Files files) throws OctopusIOException;

    /**
     * Invoked for a directory before entries in the directory are visited.
     * 
     * @param dir 
     *          the directory to visit.
     * @param attributes 
     *          the attributes of the directory.
     * @param files 
     *          the {@link Files} used to access the directory.
     *          
     * @return the desired action.
     * 
     * @throws OctopusIOException
     *          if an I/O error occurs while visiting the directory. 
     */
    FileVisitResult preVisitDirectory(Path dir, FileAttributes attributes, Files files) throws OctopusIOException;

    /**
     * Invoked for a file in a directory.
     * 
     * @param file 
     *          the file to visit.
     * @param attributes 
     *          the attributes of the file.
     * @param files 
     *          the {@link Files} used to access the file.
     *          
     * @return the desired action.
     * 
     * @throws OctopusIOException
     *          if an I/O error occurs while visiting the directory. 
     */
    FileVisitResult visitFile(Path file, FileAttributes attributes, Files files) throws OctopusIOException;

    /**
     * Invoked for a file that could not be visited.
     *
     * @param file 
     *          the file that could not be visited.
     * @param exception 
     *          the exception thrown while visiting the file.
     * @param files 
     *          the {@link Files} used to access the file.
     *          
     * @return the desired action.
     * 
     * @throws OctopusIOException
     *          if an I/O error occurs while visiting the directory. 
     */     
    FileVisitResult visitFileFailed(Path file, OctopusIOException exception, Files files) throws OctopusIOException;
}
