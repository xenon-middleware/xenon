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
import java.lang.reflect.Field;

import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.util.CommandRunner;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.Streams;
import nl.esciencecenter.octopus.engine.util.ProcessWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        streams = new Streams(job, process.getInputStream(), process.getOutputStream(), process.getErrorStream());
    }

    public Streams getStreams() { 
        return streams;
    }
    
    public int waitFor() throws InterruptedException {
        exitCode = process.waitFor();
        done = true;
        return exitCode;
    }

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
    
    private void unixDestroy(java.lang.Process process) throws Throwable {
        Field pidField = process.getClass().getDeclaredField("pid");

        pidField.setAccessible(true);

        int pid = pidField.getInt(process);

        if (pid <= 0) {
            throw new Exception("Pid reported as 0 or negative: " + pid);
        }

        CommandRunner killRunner = new CommandRunner("kill", "-9", "" + pid);

        if (killRunner.getExitCode() != 0) {
            throw new OctopusException(LocalAdaptor.ADAPTOR_NAME, "Failed to kill process, exit code was " + 
                    killRunner.getExitCode() + " output: " + killRunner.getStdout() + " error: " + killRunner.getStderr());
        }

    }

    public int getExitStatus() {
        return exitCode;
    }
    
    public void destroy() {
        
        if (done) { 
            return;
        }
        
        try {
            unixDestroy(process);
        } catch (Throwable t) {
            logger.debug("Could not destroy process using getpid/kill, using normal java destroy", t);
            process.destroy();
        }
    }
}
