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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.util.CommandRunner;
import nl.esciencecenter.octopus.engine.util.MergingOutputStream;
import nl.esciencecenter.octopus.engine.util.StreamForwarder;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.Streams;
import nl.esciencecenter.octopus.engine.util.ProcessWrapper;

/**
 * LocalBatchProcess implements a {@link ProcessWrapper} for local batch processes. 
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
class LocalBatchProcess implements ProcessWrapper {

    private static final Logger logger = LoggerFactory.getLogger(LocalBatchProcess.class);

    private final java.lang.Process[] processes;

    private final StreamForwarder[] stdinForwarders;
    private final StreamForwarder[] stdoutForwarders;
    private final StreamForwarder[] stderrForwarders;

    private final MergingOutputStream stdoutStream;
    private final MergingOutputStream stderrStream;

    private final int [] exitCodes;
    private final boolean [] done;
    
    LocalBatchProcess(JobImplementation job) throws IOException { 

        JobDescription description = job.getJobDescription();
        
        int count = description.getProcessesPerNode();

        exitCodes = new int[count];
        done = new boolean[count];

        ProcessBuilder builder = new ProcessBuilder();
            
        String workingDirectory = description.getWorkingDirectory();
        
        String stdout = description.getStdout();
        String stderr = description.getStderr();
        String stdin = description.getStdin();
        
        if (workingDirectory == null) { 
            workingDirectory = System.getProperty("user.dir");
        }
        
        builder.command().add(description.getExecutable());
        builder.command().addAll(description.getArguments());
        builder.environment().putAll(description.getEnvironment());
        builder.directory(new java.io.File(workingDirectory));

        // Merge stdout and stderr into a single stream
        if (description.getMergeOutputStreams()) {             
            stdoutStream = new MergingOutputStream(new FileOutputStream(workingDirectory + File.separator + stdout));
            stderrStream = new MergingOutputStream(new FileOutputStream(workingDirectory + File.separator + stderr));
        } else { 
            stdoutStream = null;
            stderrStream = null;
        }

        processes = new java.lang.Process[count];
        stdinForwarders = new StreamForwarder[count];
        stdoutForwarders = new StreamForwarder[count];
        stderrForwarders = new StreamForwarder[count];

        for (int i = 0; i < count; i++) {
            processes[i] = builder.start();

            if (stdin == null) {
                stdinForwarders[i] = null;
                processes[i].getOutputStream().close();
            } else {
                stdinForwarders[i] = new StreamForwarder(new FileInputStream(workingDirectory + File.separator + stdin),
                                processes[i].getOutputStream());
            }

            if (description.getMergeOutputStreams()) { 
                stdoutForwarders[i] = new StreamForwarder(processes[i].getInputStream(), stdoutStream);
                stderrForwarders[i] = new StreamForwarder(processes[i].getErrorStream(), stderrStream);
            } else { 
                stdoutForwarders[i] = new StreamForwarder(processes[i].getInputStream(), 
                        new FileOutputStream(workingDirectory + File.separator + stdout + "." + i));
                 
                stderrForwarders[i] = new StreamForwarder(processes[i].getErrorStream(),
                        new FileOutputStream(workingDirectory + File.separator + stderr  + "." + i));
            }
        }
    }

//    private void kill() {
//        for (int i = 0; i < processes.length; i++) {
//            processes[i].destroy();
//
//            if (stdinForwarders[i] != null) {
//                stdinForwarders[i].close();
//            }
//
//            stdoutForwarders[i].close();
//            stderrForwarders[i].close();
//        }
//        
//        try {
//            if (stdoutStream != null) { 
//                stdoutStream.close();
//            }
//        } catch (IOException e) {
//            // IGNORE
//        }
//        
//        try {
//            if (stderrStream != null) { 
//                stderrStream.close();
//            }
//        } catch (IOException e) {
//            // IGNORE
//        }
//    }

    public int waitFor() throws InterruptedException {
        
        int[] results = new int[processes.length];

        for (int i = 0; i < processes.length; i++) {
            results[i] = processes[i].waitFor();
        }

        return results[0];
    }

    public boolean isDone() {
        
        for (int i=0;i<processes.length;i++) {
            
            if (!done[i]) {
                try { 
                    exitCodes[i] = processes[i].exitValue();
                    done[i] = true;
                } catch (IllegalThreadStateException e) { 
                    // ignored
                    return false;
                }
            }
        } 
        
        return true;
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
        
        for (int i=0;i<exitCodes.length;i++) { 
            if (exitCodes[i] != 0) { 
                return exitCodes[i];
            }
        }
        
        return 0;
    }
    
    public void destroy() {
        for (int i = 0; i < processes.length; i++) {
            try {
                unixDestroy(processes[i]);
            } catch (Throwable t) {
                logger.debug("Could not destroy process using getpid/kill, using normal java destroy", t);
                processes[i].destroy();
            }
        }
    }

    @Override
    public Streams getStreams() {
        return null;
    }
}
