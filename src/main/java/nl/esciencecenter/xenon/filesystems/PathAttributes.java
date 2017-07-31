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
package nl.esciencecenter.xenon.filesystems;

import java.util.Set;

/**
 * FileAttributes represents a set of attributes of a path.
 */
public interface PathAttributes {

    /**
     * Get the path these attributes belong to.
     *
     * @return
     *       the path these attributes belong to.
     */
    Path getPath();

    /**
     * Does the path refer to a directory ?
     *
     * @return
     *          if the path refers to a directory.
     */
    boolean isDirectory();

    /**
     * Does the path refer to a regular file ?
     *
     * @return
     *          if the path refers to a regular file.
     */
    boolean isRegular();

    /**
     * Does the path refer to a symbolic link ?
     *
     * @return
     *          if the path refers to a symbolic link.
     */
    boolean isSymbolicLink();

    /**
     * Is the path not a file, link or directory ?
     *
     * @return
     *          if the path does not refer to a file, link or directory.
     */
    boolean isOther();

    /**
     * Does the path refer to an executable file ?
     *
     * @return
     *          if the path refers an executable file ?
     */
    boolean isExecutable();

    /**
     * Does the path refer to an readable file ?
     *
     * @return
     *          if the path refers an readable file ?
     */
    public boolean isReadable();

    /**
     * Does the path refer to a writable file ?
     *
     * @return
     *          if the path refers a writable file ?
     */
    boolean isWritable();

    /**
     * Does the path refer to an hidden file ?
     *
     * @return
     *          if the path refers an hidden file ?
     */
    boolean isHidden();

    /**
     * Get the creation time for this file.
     *
     * If creationTime is not supported by the adaptor, {@link #getLastModifiedTime()} will be returned instead.
     *
     * @return
     *          the creation time for this file.
     */
    long getCreationTime();

    /**
     * Get the last access time for this file.
     *
     * If lastAccessTime is not supported by the adaptor, use {@link #getLastModifiedTime()} will be returned instead.
     *
     * @return
     *          the last access time for this file.
     */
    long getLastAccessTime();

    /**
     * Get the last modified time for this file.
     *
     * If lastModifiedTime is not supported by the adaptor, <code>0</code> will be returned instead.
     *
     * @return
     *          the last modified time for this file.
     */
    long getLastModifiedTime();

    /**
     * Get the size of this file in bytes.
     *
     * If the file is not a regular file, <code>0</code> will be returned.
     *
     * @return
     *          the size of this file.
     */
    long getSize();

    /**
     * Get the owner of this file (optional operation).
     *
     * @return
     *          the owner of this file.
     *
     * @throws AttributeNotSupportedException
     *          If the attribute is not supported by the adaptor.
     */
    String getOwner() throws AttributeNotSupportedException;

     /**
     * Get the group of this file (optional operation).
     *
     * @return
     *          the group of this file.
     *
     * @throws AttributeNotSupportedException
     *          If the attribute is not supported by the adaptor.
     */
    String getGroup() throws AttributeNotSupportedException;

    /**
     * Get the permissions of this file (optional operation).
     *
     * @return
     *          the permissions of this file.
     *
     * @throws AttributeNotSupportedException
     *          If the attribute is not supported by the adaptor.
     */
    Set<PosixFilePermission> getPermissions() throws AttributeNotSupportedException;
}
