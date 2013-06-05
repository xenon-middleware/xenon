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

/**
 * CopyOption is an enumeration containing all possible options for copying a file.
 *
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public enum CopyOption {

    /**
     * Copy to a new destination file, failing if the file already exists.
     */
    CREATE,

    /**
     * Replace the destination file if it already exists.
     */
    REPLACE,

    /**
     * Skip the copy if the destination file if it already exists.
     */
    IGNORE,

    /**
     * Append to destination file, failing if the file does not exists.
     */
    APPEND,

    /**
     * Resume the copy to destination file, failing if the file does not exists.
     */
    RESUME,

    /**
     * When resuming a copy, verify that the destination file is a head of the source file.
     */
    VERIFY,

    /**
     * Perform the copy asynchronously.
     */
    ASYNCHRONOUS,;

    /**
     * Check if an array of <code>CopyOption</code> contains a specific option.
     *
     * If <code>option</code> or <code>options<code> is <code>null</code>, <code>false</code> will be returned.
     *
     * @param options the array to check.
     * @param option the option to check for.
     *
     * @return if <code>options</code> contains <code>option<code>.
     */
    public static boolean contains(CopyOption[] options, CopyOption option) {

        if (option == null || options == null || options.length == 0) {
            return false;
        }

        for (int i=0;i<options.length;i++) {

            if (options[i] != null && options[i] == option) {
                return true;
            }
        }

        return false;
    }
}
