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
package nl.esciencecenter.xenon.engine.jobs;

import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.Scheduler;

public class JobImplementation implements Job {

    private JobDescription description;

    private final Scheduler scheduler;

    private final String identifier;

    private final boolean interactive;

    private final boolean online;

    public JobImplementation(Scheduler scheduler, String identifier, boolean interactive, boolean online) {

        if (scheduler == null) {
            throw new IllegalArgumentException("Scheduler may not be null!");
        }

        if (identifier == null) {
            throw new IllegalArgumentException("Identifier may not be null!");
        }

        this.description = null;
        this.scheduler = scheduler;
        this.identifier = identifier;
        this.interactive = interactive;
        this.online = online;
    }

    public JobImplementation(Scheduler scheduler, String identifier, JobDescription description, boolean interactive,
            boolean online) {

        this(scheduler, identifier, interactive, online);

        if (description == null) {
            throw new IllegalArgumentException("JobDescription may not be null!");
        }

        this.description = description;
    }

    @Override
    public JobDescription getJobDescription() {
        return description;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean isInteractive() {
        return interactive;
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public String toString() {
        return "JobImplementation [identifier=" + identifier + ", scheduler=" + scheduler + ", description=" + description
                + ", isInteractive=" + isInteractive() + ", isOnline=" + online + "]";
    }

    @Override
    public int hashCode() {
        return scheduler.hashCode() + identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        JobImplementation other = (JobImplementation) obj;

        return identifier.equals(other.identifier) && scheduler.equals(other.scheduler);
    }
}
