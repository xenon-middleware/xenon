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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import nl.esciencecenter.octopus.exceptions.OctopusException;

/**
 * Job represents a Job belonging to a {@link Scheduler}.
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface Job {

    /**
     * Returns the {@link JobDescription} that was used to create this Job.
     * 
     * @return the JobDescription that belongs to this Job
     */
    public JobDescription getJobDescription();

    /**
     * Returns the {@link Scheduler} that was used to create this Job.
     * 
     * @return the Scheduler used to create this job.
     */
    public Scheduler getScheduler();

    /**
     * Returns the identifier that was assigned to this job by the scheduler.
     * 
     * @return the identifier that was assigned to this job by the scheduler.
     */
    public String getIdentifier();
    
    /**
     * Returns a universally unique identifier for this job.
     * 
     * @return the universally unique identifier for this job. 
     */
    public UUID getUUID();
    
    /** 
     * Returns if this is an interactive job.
      * 
     * @return if this is an interactive job.
     */    
    public boolean isInteractive();

    /** 
     * Returns if this is an online job. 
     * 
     * Online jobs will disappear when the application that submitted is exist, while offlines jobs will keep running. 
     * 
     * Interactive jobs are by definition online. Batch jobs will be online if the scheduler to which they were submitted is 
     * online.  
      * 
     * @return if this is an online job.
     */    
    public boolean isOnline();
    
    /** 
     * Returns the standard output stream of this job.
     * 
     * The standard streams can only be retrieved if this is an interactive job.  
     * 
     * @return the standard output stream of this job.
     *
     * @throws OctopusException if this job is not interactive.
     */
    public InputStream getStdout() throws OctopusException;
    
    /** 
     * Returns the standard error stream of this job.
     * 
     * The standard streams can only be retrieved if this is an interactive job.  
     * 
     * @return the standard error stream of this job.
     *
     * @throws OctopusException if this job is not interactive.
     */
    public InputStream getStderr() throws OctopusException;

    /** 
     * Returns the standard input stream of this job.
     * 
     * The standard streams can only be retrieved if this is an interactive job.  
     * 
     * @return the standard input stream of this job.
     *
     * @throws OctopusException if this job is not interactive.
     */
    public OutputStream getStdin() throws OctopusException;    
}
