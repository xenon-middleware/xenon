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

/**
 * OpenOption is an enumeration containing all possible options for opening a stream to a file.
 * 
 * @version 1.0
 * @since 1.0
 */
public enum OpenOption {

    /**
     * Create a new file, failing if the file already exists.
     */
    CREATE,

    /**
     * Open an existing file (or create a new one if it does not exists) and write bytes from the start of the file.
     */
    REPLACE,

    /**
     * Open an existing file (failing if the file does not exist) and write bytes to the end of the file.
     */
    APPEND,

    
    
//    /**
//     * Open an existing file, failing if the file does not exist.
//     */
//    OPEN,
//
//    /**
//     * Open an existing file or create a new file if it does not exist.
//     */
//    OPEN_OR_CREATE,


//    /**
//     * Open for read access.
//     */
//    READ,
//
//    /**
//     * Open for write access.
//     */
//    WRITE;

    /**
     * Check if a sequence of <code>OpenOption</code>s contains this option.
     * 
     * If <code>options</code> is <code>null</code>, <code>false</code> will be returned.
     * 
     * @param options
     *            the array to check.
     * 
     * @return if <code>options</code> contains <code>option</code>.
     */
//    public boolean occursIn(OpenOption... options) {
//        if (options == null) {
//            return false;
//        }
//
//        for (OpenOption curr : options) {
//            if (curr == this) {
//                return true;
//            }
//        }
//        return false;
//    }
}
