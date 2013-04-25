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

import java.io.InputStream;
import java.io.OutputStream;

import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.Scheduler;

public class JobImplementation implements Job {

    private final JobDescription description;

    private final Scheduler scheduler;

    private final String identifier;

    private final boolean interactive; 
    
    private final boolean online; 
    
    private InputStream stdout;

    private InputStream stderr;

    private OutputStream stdin;
    
    public JobImplementation(JobDescription description, Scheduler scheduler, String identifier, boolean interactive, 
            boolean online) {  
        this.description = description;
        this.scheduler = scheduler;
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

    @Override
    public String toString() {
        return "JobImplementation [identifier=" + identifier + ", scheduler=" + scheduler + ", description=" + description + "]";
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
     * @see nl.esciencecenter.octopus.jobs.Job#getStdout()
     */
    @Override
    public InputStream getStdout() throws OctopusException {
        
        if (!interactive) { 
            throw new OctopusException("Engine", "Cannot retrieve the stdout of a batch job!");
        }
        
        synchronized (this) {
            return stdout;
        }
    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.octopus.jobs.Job#getStderr()
     */
    @Override
    public InputStream getStderr() throws OctopusException {

        if (!interactive) { 
            throw new OctopusException("Engine", "Cannot retrieve the stderr of a batch job!");
        }
        
        synchronized (this) {
            return stderr;
        }
    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.octopus.jobs.Job#getStdin()
     */
    @Override
    public OutputStream getStdin() throws OctopusException {
        
        if (!interactive) { 
            throw new OctopusException("Engine", "Cannot retrieve the stdin of a batch job!");
        }
        
        synchronized (this) {
            return stdin;
        }
    }

    /**
     * Set the stdin of this job. 
     * 
     * @param outputStream
     */
    public synchronized void setStdin(OutputStream stdin) {
        this.stdin = stdin;
    }

    /**
     * Set the stdout of this job. 
     * 
     * @param inputStream
     */
    public void setStdout(InputStream stdout) {
        this.stdout = stdout;
    }

    /**
     * Set the stderr of this job. 
     * 
     * @param errorStream
     */
    public void setStderr(InputStream stderr) {
        this.stderr = stderr;
    }
}
