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
package nl.esciencecenter.octopus.files;

import java.util.Set;

import nl.esciencecenter.octopus.exceptions.AttributeNotSupportedException;

/**
 * FileAttributes represents a set of attributes of a path.
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface FileAttributes {

    /**
     * Does the path refer to a directory ?
     * 
     * @return If the path refers to a directory.
     * 
     * @throws AttributeNotSupportedException
     *             If the attribute is not supported by the adaptor.
     */
    boolean isDirectory() throws AttributeNotSupportedException;

    /**
     * Is the path not a file, link or directory ?
     * 
     * @return If the file is a directory.
     * 
     * @throws AttributeNotSupportedException
     *             If the attribute is not supported by the adaptor.
     */
    boolean isOther() throws AttributeNotSupportedException;

    /**
     * Does the path refer to a regular file ?
     * 
     * @return If the path refers to a regular file.
     * 
     * @throws AttributeNotSupportedException
     *             If the attribute is not supported by the adaptor.
     */
    boolean isRegularFile() throws AttributeNotSupportedException;

    /**
     * Does the path refer to a symbolic link ?
     * 
     * @return If the path refers to a symbolic link.
     * 
     * @throws AttributeNotSupportedException
     *             If the attribute is not supported by the adaptor.
     */
    boolean isSymbolicLink() throws AttributeNotSupportedException;

    /**
     * Does the path refer to an executable file ?
     * 
     * @return If the path refers an executable file ?
     * 
     * @throws AttributeNotSupportedException
     *             If the attribute is not supported by the adaptor.
     */
    boolean isExecutable() throws AttributeNotSupportedException;

    /**
     * Does the path refer to an hidden file ?
     * 
     * @return If the path refers an hidden file ?
     * 
     * @throws AttributeNotSupportedException
     *             If the attribute is not supported by the adaptor.
     */
    boolean isHidden() throws AttributeNotSupportedException;

    /**
     * Does the path refer to an readable file ?
     * 
     * @return If the path refers an readable file ?
     * 
     * @throws AttributeNotSupportedException
     *             If the attribute is not supported by the adaptor.
     */
    boolean isReadable() throws AttributeNotSupportedException;

    /**
     * Does the path refer to a writable file ?
     * 
     * @return If the path refers a writable file ?
     * 
     * @throws AttributeNotSupportedException
     *             If the attribute is not supported by the adaptor.
     */
    boolean isWritable() throws AttributeNotSupportedException;

    /**
     * Get the creation time for this file.
     * 
     * @return The creation time for this file.
     * 
     * @throws AttributeNotSupportedException
     *             If the attribute is not supported by the adaptor.
     */
    long creationTime() throws AttributeNotSupportedException;

    /**
     * Get the last access time for this file.
     * 
     * @return The last access time for this file.
     * 
     * @throws AttributeNotSupportedException
     *             If the attribute is not supported by the adaptor.
     */
    long lastAccessTime() throws AttributeNotSupportedException;

    /**
     * Get the last modified time for this file.
     * 
     * @return The last modified time for this file.
     * 
     * @throws AttributeNotSupportedException
     *             If the attribute is not supported by the adaptor.
     */
    long lastModifiedTime() throws AttributeNotSupportedException;

    /**
     * Get the size of this file.
     * 
     * @return The size of this file.
     * 
     * @throws AttributeNotSupportedException
     *             If the attribute is not supported by the adaptor.
     */
    long size() throws AttributeNotSupportedException;

    /**
     * Get the group of this file.
     * 
     * @return The group of this file.
     * 
     * @throws AttributeNotSupportedException
     *             If the attribute is not supported by the adaptor.
     */
    String group() throws AttributeNotSupportedException;

    /**
     * Get the owner of this file.
     * 
     * @return The owner of this file.
     * 
     * @throws AttributeNotSupportedException
     *             If the attribute is not supported by the adaptor.
     */
    String owner() throws AttributeNotSupportedException;

    /**
     * Get the permissions of this file.
     * 
     * @return The permissions of this file.
     * 
     * @throws AttributeNotSupportedException
     *             If the attribute is not supported by the adaptor.
     */
    Set<PosixFilePermission> permissions() throws AttributeNotSupportedException;
}
