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
package nl.esciencecenter.xenon.schedulers;

import nl.esciencecenter.xenon.XenonException;

import java.util.Map;

/**
 * QueueStatus contains status information for a specific queue.
 */
public interface QueueStatus {

    /**
     * Get the Scheduler that produced this QueueStatus.
     *
     * @return the Scheduler.
     */
    Scheduler getScheduler();

    /**
     * Get the queue name.
     *
     * @return the queue name.
     */
    String getQueueName();

    /**
     * Did the queue produce an exception ?
     *
     * @return if the queue produced an exception ?
     */
    boolean hasException();

    /**
     * Get the exception produced by the queue, or <code>null</code> if <code>hasException()</code> returns <code>false</code>.
     *
     * See {@link #maybeThrowException()} for the possible exceptions.
     *
     * @return the exception.
     */
    XenonException getException();


    /**
     * Throws the exception produced by the queue if it exists. Otherwise continue.
     *@throws NoSuchQueueException
     *          if the requested queue does not exist
     * @throws XenonException
     *          if an I/O error occurred.
     */
    void maybeThrowException() throws XenonException;

    /**
     * Get scheduler specific information on the queue.
     *
     * @return Scheduler specific information on the queue.
     */
    Map<String, String> getSchedulerSpecficInformation();
}
