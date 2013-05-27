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

import java.util.UUID;

import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.Scheduler;

public class JobImplementation implements Job {

    private final JobDescription description;

    private final Scheduler scheduler;

    private final String identifier;

    private final UUID uuid;
    
    private final boolean interactive; 
    
    private final boolean online; 
        
    public JobImplementation(JobDescription description, Scheduler scheduler, UUID uuid, String identifier, boolean interactive, 
            boolean online) {  
        
        if (description == null) { 
            throw new IllegalArgumentException("JobDescription may not be null!");
        }
        
        if (scheduler == null) { 
            throw new IllegalArgumentException("Scheduler may not be null!");
        }
        
        if (uuid == null) { 
            throw new IllegalArgumentException("UUID may not be null!");
        }
        
        if (identifier == null) { 
            throw new IllegalArgumentException("Identifier may not be null!");
        }
        
        this.description = description;
        this.scheduler = scheduler;
        this.uuid = uuid;
        this.identifier = identifier;
        this.interactive = interactive;
        this.online = online;
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

    /* (non-Javadoc)
     * @see nl.esciencecenter.octopus.jobs.Job#isInteractive()
     */
    @Override
    public boolean isInteractive() {
        return interactive;
    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.octopus.jobs.Job#isOnline()
     */
    @Override
    public boolean isOnline() {
        return online;
    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.octopus.jobs.Job#getUUID()
     */
    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String toString() {
        return "JobImplementation [identifier=" + identifier + ", scheduler=" + scheduler + ", description=" + description + "]";
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
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
        
        return uuid.equals(other.uuid);
    }
}
