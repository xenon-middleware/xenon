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
 * OpenOption is an enumeration containing all possible options for opening a stream or channel to a file.
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public enum OpenOption {

    /**
     * Create a new file, failing if the file already exists.
     */
    CREATE,

    /**
     * Open an existing file, failing if the file does not exist.
     */
    OPEN,

    /**
     * Open an existing file or create a new file if it does not exist.
     */
    OPEN_OR_CREATE,

    /**
     * If the file is opened for WRITE access then bytes will be written to the end of the file rather than the beginning.
     */
    APPEND,

    /**
     * If the file is opened for WRITE access, then its length is truncated to 0. All existing data is overwritten.
     */
    TRUNCATE,

    /**
     * Open for read access.
     */
    READ,

    /**
     * Open for write access.
     */
    WRITE;

    public static boolean contains(OpenOption toFind, OpenOption... options) {

        if (toFind == null || options == null || options.length == 0) {
            return false;
        }

        for (OpenOption curr : options) {
            if (curr == toFind) {
                return true;
            }
        }
        return false;
    }
}
