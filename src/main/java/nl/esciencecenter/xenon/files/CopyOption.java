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
package nl.esciencecenter.xenon.files;

/**
 * CopyOption is an enumeration containing all possible options for copying a file.
 * 
 * Note that the <code>CREATE</code>, <code>REPLACE</code>,<code>IGNORE</code>,<code>APPEND</code> and <code>RESUME</code> options
 * are mutually exclusive.  
 * 
 * The <code>VERIFY</code> option can only be used in combination with <code>RESUME</code>.
 * 
 * The <code>ASYNCHRONOUS</code> option can be combined with all others. 
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
    ASYNCHRONOUS;

    /**
     * Check if the CopyOption is listed in a sequence.
     * 
     * If <code>options<code> is <code>null</code><code>false</code> will be returned.
     * 
     * @param options
     *            the options to check.
     * 
     * @return if <code>options</code> contains <code>CopyOption<code>.
     */
    public boolean occursIn(CopyOption... options) {
        if (options == null) {
            return false;
        }

        for (CopyOption option : options) {
            if (this == option) {
                return true;
            }
        }

        return false;
    }
}
