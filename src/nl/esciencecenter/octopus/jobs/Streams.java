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

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public interface Streams {
    
    /**
     * Get the job for which this Streams was created.
     * 
     * @return the Job.
     */
    public Job getJob(); 
    
    /** 
     * Returns the standard output stream of job.
     * 
     * @return the standard output stream of job.
     */
    public InputStream getStdout(); 
    
    /** 
     * Returns the standard error stream of job.
     * 
     * @return the standard error stream of job.
     */
    public InputStream getStderr(); 

    /** 
     * Returns the standard input stream of job.
     * 
     * @return the standard input stream of this job.
     */
    public OutputStream getStdin(); 
}
