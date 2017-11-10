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

import nl.esciencecenter.xenon.AdaptorDescription;

/**
 *
 */
public interface FileSystemAdaptorDescription extends AdaptorDescription {

    /**
     * Does this adaptor support third party copy ?
     *
     * In third party copy, a file is copied between two remote locations, without passing through the local machine first.
     *
     * @return if this adaptor supports third party copy.
     */
    boolean supportsThirdPartyCopy();

    /**
     * Can this adaptor read symbolic links ?
     *
     * @return if this adaptor can read symbolic links.
     */
    boolean canReadSymboliclinks();

    /**
     * Can this adaptor create symbolic links ?
     *
     * In third party copy, a file is copied between two remote locations, without passing through the local machine first.
     *
     * @return if this adaptor can create symbolic links.
     */
    boolean canCreateSymboliclinks();

    /**
     * Is this adaptor connectionless ?
     * 
     * A connectionless adaptor to not retain a connection to a resources between operations. Instead a new connection is created for each operation that is
     * performed. In contrast, connected adaptors typically perform a connection setup when they are created and reuse this connection for each operation.
     * 
     * @return if this adaptor is connectionless.
     */
    boolean isConnectionless();

    /**
     * Does this adaptor support reading of posix style permissions?
     * 
     * @return if this adaptor support reading of posix style permissions.
     */
    boolean supportsReadingPosixPermissions();

    /**
     * Does this adaptor support setting of posix style permissions?
     * 
     * @return if this adaptor supports setting of posix style permissions.
     */
    boolean supportsSettingPosixPermissions();

    /**
     * Does this adaptor support renaming of files ?
     * 
     * @return if this adaptor supports renaming of files.
     */

    boolean supportsRename();

    /**
     * Can this adaptor append data to existing files ?
     * 
     * @return if this adaptor can append data to existing files.
     */
    boolean canAppend();

    /**
     * When writing to a file, does this adaptor need to know the size of the data beforehand ?
     * 
     * @return if this adaptor needs to know the size of the date written to a file beforehand.
     */
    boolean needsSizeBeforehand();
}
