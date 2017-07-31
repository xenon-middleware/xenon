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
 * CopyStatus contains status information for a specific copy operation.
 */
public interface CopyStatus {

    /**
     * Get the copy identifier for which this CopyStatus was created.
     *
     * @return the Copy.
     */
    String getCopyIdentifier();

    /**
     * Get the state of the Copy operation.
     *
     * @return the state of the Copy operation.
     */
    String getState();

    /**
     * Get the exception produced by the Copy or while retrieving the status.
     *
     * @return the exception.
     */
    Throwable getException();

    /**
     * Is the Copy still running?
     *
     * @return if the Copy is running.
     */
    boolean isRunning();

    /**
     * Is the Copy done?
     *
     * @return if the Copy is done.
     */
    boolean isDone();

    /**
     * Has the Copy or status retrieval produced a exception ?
     *
     * @return if the Copy or status retrieval produced a exception.
     */
    boolean hasException();

    /**
     * Get the number of bytes that need to be copied for the entire copy operation.
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
