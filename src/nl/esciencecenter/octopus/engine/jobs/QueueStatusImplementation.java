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
package nl.esciencecenter.octopus.engine.jobs;

import java.util.Map;

import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

public class QueueStatusImplementation implements QueueStatus {
    private final Scheduler scheduler;
    private final String queueName;
    private final Exception exception;
    private final Map<String, String> schedulerSpecificInformation;

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

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public String getQueueName() {
        return queueName;
    }

    @Override
    public Exception getException() {
        return exception;
    }

    @Override
    public boolean hasException() {
        return (exception != null);
    }

    @Override
    public Map<String, String> getSchedulerSpecficInformation() {
        return schedulerSpecificInformation;
    }

    @Override
    public String toString() {
        return "QueueStatusImplementation [scheduler=" + scheduler + ", queueName=" + queueName + ", exception=" + exception
                + ", schedulerSpecificInformation=" + schedulerSpecificInformation + "]";
    }
}
