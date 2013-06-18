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
package nl.esciencecenter.octopus.adaptors.local;

import java.io.IOException;

import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.StreamsImplementation;
import nl.esciencecenter.octopus.engine.util.ProcessWrapper;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.Streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LocalInteractiveProcess implements a {@link ProcessWrapper} for local interactive processes. 
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
class LocalInteractiveProcess implements ProcessWrapper {

    private static final Logger logger = LoggerFactory.getLogger(LocalInteractiveProcess.class);

    private final java.lang.Process process;

    private int exitCode;
    private boolean done;
    
    private Streams streams;
    
    LocalInteractiveProcess(JobImplementation job) throws IOException { 

        JobDescription description = job.getJobDescription();
        
        ProcessBuilder builder = new ProcessBuilder();
            
        builder.command().add(description.getExecutable());
        builder.command().addAll(description.getArguments());
        builder.environment().putAll(description.getEnvironment());
        
        String workingDirectory = description.getWorkingDirectory();
        
        if (workingDirectory == null) { 
            workingDirectory = System.getProperty("user.dir");
        }
        
        builder.directory(new java.io.File(workingDirectory));
        
        process = builder.start();       
        streams = new StreamsImplementation(job, process.getInputStream(), process.getOutputStream(), process.getErrorStream());
    }

    public Streams getStreams() { 
        return streams;
    }
    
//    public int waitFor() throws InterruptedException {
//        exitCode = process.waitFor();
//        done = true;
//        return exitCode;
//    }

    public boolean isDone() {
        
        if (done) { 
            return true;
        }
        
        try { 
            exitCode = process.exitValue();
            done = true;
            return true;
        } catch (IllegalThreadStateException e) { 
            // ignored
            return false;
        }
    }
   
    public int getExitStatus() {
        return exitCode;
    }
    
    public void destroy() {
        
        if (done) { 
            return;
        }
        
        LocalUtils.unixDestroy(process);        
    }
}
