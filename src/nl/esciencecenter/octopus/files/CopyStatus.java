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
 * CopyStatus contains status information for a specific copy operation.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface CopyStatus {

    /**
     * Get the Copy for which this CopyStatus was created.
     * 
     * @return the Copy.
     */
    Copy getCopy();

    /**
     * Get the state of the Job.
     * 
     * @return the state of the Job.
     */
    String getState();

    /**
     * Get the exception produced by the Copy or while retrieving the status.
     * 
     * @return the exception.
     */
    Exception getException();

    /**
     * Is the Copy still running.
     * 
     * @return if the Copy is running.
     */
    boolean isRunning();

    /**
     * Is the Copy done.
     * 
     * @return if the Copy is done.
     */
    boolean isDone();

    /**
     * Has the Copy or status retrieval produced a exception ?
     * 
     * @return if the Copy has an exception.
     */
    boolean hasException();

    /**
     * Get the number of bytes that need to be copied.
     * 
     * @return the number of bytes that need to be copied.
     */
    long bytesToCopy();

    /**
     * Get the number of bytes that have been copied.
     * 
     * @return the number of bytes that have been copied.
     */
    long bytesCopied();
}
