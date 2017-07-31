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
package nl.esciencecenter.xenon.filesystems;

/**
 * PosixFilePermission is an enumeration containing all supported Posix file permissions.
 * 
 * @version 1.0
 * @since 1.0
 */
public enum PosixFilePermission {

    /** 
     * Execute/search permission, group.
     */
    GROUP_EXECUTE,
    /**
     * Read permission, group.
     */
    GROUP_READ,
    /**
     * Write permission, group.
     */
    GROUP_WRITE,
    /**
     * Execute/search permission, others.
     */
    OTHERS_EXECUTE,
    /**
     * Read permission, others.
     */
    OTHERS_READ,
    /**
     * Write permission, others.
     */
    OTHERS_WRITE,
    /**
     * Execute/search permission, owner.
     */
    OWNER_EXECUTE,
    /**
     * Read permission, owner.
     */
    OWNER_READ,
    /**
     * Write permission, owner.
     */
    OWNER_WRITE;

    /**
     * Check if a sequence of <code>PosixFilePermission</code>s contains a specific option.
     * 
     * If <code>option</code> or <code>options</code> is <code>null</code>, <code>false</code> will be returned.
     *
     * @param toFind
     *            the option to find.
     * @param options
     *            the options to check.
     * 
     * @return if <code>options</code> contains <code>option</code>.
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public static boolean contains(PosixFilePermission toFind, PosixFilePermission... options) {

        if (toFind == null || options == null || options.length == 0) {
            return false;
        }

        for (PosixFilePermission curr : options) {
            if (curr == toFind) {
                return true;
            }
        }
        return false;
    }
}
