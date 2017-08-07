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
package nl.esciencecenter.xenon.adaptors.schedulers;

import java.util.Map;

import nl.esciencecenter.xenon.schedulers.QueueStatus;
import nl.esciencecenter.xenon.schedulers.Scheduler;

/**
 * QueueStatus contains status information for a specific queue.
 */
public class QueueStatusImplementation implements QueueStatus {

    private final Scheduler scheduler;
    private final String queueName;
    private final Exception exception;
    private final Map<String, String> schedulerSpecificInformation;

    /**
     * Create a Queue status.
     *
     * @param scheduler
     * 		the <code>Scheduler</code> to which the queue belongs.
     * @param queueName
     * 		the name of the queue.
     * @param exception
     * 		the exception produced when retrieving the queue status (if any).
     * @param schedulerSpecificInformation
     * 		scheduler implementation specific information on the status of the queue.
     */
    public QueueStatusImplementation(Scheduler scheduler, String queueName, Exception exception,
            Map<String, String> schedulerSpecificInformation) {

        if (scheduler == null) {
            throw new IllegalArgumentException("Scheduler may not be null!");
        }

        if (queueName == null) {
            throw new IllegalArgumentException("QueueName may not be null!");
        }

        this.scheduler = scheduler;
        this.queueName = queueName;
        this.exception = exception;
        this.schedulerSpecificInformation = schedulerSpecificInformation;
    }

    /**
     * Get the Scheduler that produced this QueueStatus.
     *
     * @return the Scheduler.
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * Get the queue name.
     *
     * @return the queue name.
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Did the queue produce an exception ?
     *
     * @return if the queue produced an exception ?
     */
    public boolean hasException() {
        return (exception != null);
    }

    /**
     * Get the exception produced by the queue, or <code>null</code> if <code>hasException()</code> returns <code>false</code>.
     *
     * @return the exception.
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Get scheduler specific information on the queue.
     *
     * @return Scheduler specific information on the queue.
     */
    public Map<String, String> getSchedulerSpecficInformation() {
        return schedulerSpecificInformation;
    }

    @Override
    public String toString() {
        return "QueueStatus [scheduler=" + scheduler + ", queueName=" + queueName + ", exception=" + exception
                + ", schedulerSpecificInformation=" + schedulerSpecificInformation + "]";
    }

}
