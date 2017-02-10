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
package nl.esciencecenter.xenon.files;

import java.util.Set;


/**
 * FileAttributes represents a set of attributes of a path.
 * 
 * @version 1.0
 * @since 1.0
 */
public interface FileAttributes {

    /**
     * Does the path refer to a directory ?
     * 
     * @return
     *          if the path refers to a directory.
     */
    boolean isDirectory(); 

    /**
     * Is the path not a file, link or directory ?
     * 
     * @return 
     *          if the path does not refer to a file, link or directory.
     */
    boolean isOther();

    /**
     * Does the path refer to a regular file ?
     * 
     * @return 
     *          if the path refers to a regular file.
     */
    boolean isRegularFile();

    /**
     * Does the path refer to a symbolic link ?
     * 
     * @return 
     *          if the path refers to a symbolic link.
     */
    boolean isSymbolicLink();

    /**
     * Get the creation time for this file.
     * 
     * If creationTime is not supported by the adaptor, the {@link #lastModifiedTime()} will be returned instead.
     * 
     * @return 
     *          the creation time for this file.
     */
    long creationTime();

    /**
     * Get the last access time for this file.
     * 
     * If lastAccessTime is not supported by the adaptor, the {@link #lastModifiedTime()} will be returned instead.
     *
     * @return 
     *          the last access time for this file.
     */
    long lastAccessTime();

    /**
     * Get the last modified time for this file.
     * 
     * If lastModifiedTime is not supported by the adaptor, <code>0</code> will be returned instead.
     *
     * @return 
     *          the last modified time for this file.
     */
    long lastModifiedTime();

    /**
     * Get the size of this file.
     * 
     * If the file is not a regular file, <code>0</code> will be returned. 
     * 
     * @return 
     *          the size of this file.
     */
    long size();
    
    /**
     * Does the path refer to an executable file ?
     * 
     * @return 
     *          if the path refers an executable file ?
     */
    boolean isExecutable();

    /**
     * Does the path refer to an hidden file ?
     * 
     * @return 
     *          if the path refers an hidden file ?
     */
    boolean isHidden();

    /**
     * Does the path refer to an readable file ?
     * 
     * @return 
     *          if the path refers an readable file ?
     */
    boolean isReadable();

    /**
     * Does the path refer to a writable file ?
     * 
     * @return 
     *          if the path refers a writable file ?
     */
    boolean isWritable();

    /**
     * Get the group of this file.
     * 
     * @return 
     *          the group of this file.
     * 
     * @throws AttributeNotSupportedException
     *          If the attribute is not supported by the adaptor.
     */
    String group() throws AttributeNotSupportedException;

    /**
     * Get the owner of this file.
     * 
     * @return 
     *          the owner of this file.
     * 
     * @throws AttributeNotSupportedException
     *          If the attribute is not supported by the adaptor.
     */
    String owner() throws AttributeNotSupportedException;

    /**
     * Get the permissions of this file.
     * 
     * @return 
     *          the permissions of this file.
     * 
     * @throws AttributeNotSupportedException
     *          If the attribute is not supported by the adaptor.
     */
    Set<PosixFilePermission> permissions() throws AttributeNotSupportedException;
}
