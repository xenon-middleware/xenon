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

import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobStatus;

public final class JobStatusImplementation implements JobStatus {

    private final Job job;
    private final String state;
    private final Integer exitCode;
    private final Exception exception;

    private final boolean running;
    private final boolean done;

    private final Map<String, String> schedulerSpecificInformation;

    public JobStatusImplementation(Job job, String state, Integer exitCode, Exception error, boolean running, boolean done,
            Map<String, String> schedulerSpecificInformation) {

        if (job == null) {
            throw new IllegalArgumentException("Job may not be null!");
        }

        this.job = job;
        this.state = state;
        this.exitCode = exitCode;
        this.exception = error;
        this.running = running;
        this.done = done;
        this.schedulerSpecificInformation = schedulerSpecificInformation;
    }

    @Override
    public Job getJob() {
        return job;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public Integer getExitCode() {
        return exitCode;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isDone() {
        return done;
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
        return "JobStatusImplementation [job=" + job + ", state=" + state + ", exitCode=" + exitCode + ", exception=" + exception
                + ", running=" + running + ", done=" + done + ", schedulerSpecificInformation=" + schedulerSpecificInformation
                + "]";
    }

}
