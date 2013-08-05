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
package nl.esciencecenter.octopus.jobs;

import java.util.Map;

/**
 * QueueStatus contains status information for a specific queue.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
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
     * Get the exception produced by the queue, or <code>null</code> if <code>hasEsception()</code> returns <code>false</code>.
     * 
     * @return the exception.
     */
    Exception getException();

    /**
     * Get scheduler specific information on the queue.
     * 
     * @return cheduler specific information on the queue.
     */
    Map<String, String> getSchedulerSpecficInformation();
}
