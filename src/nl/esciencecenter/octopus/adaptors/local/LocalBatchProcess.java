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

import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.util.MergingOutputStream;
import nl.esciencecenter.octopus.engine.util.ProcessWrapper;
import nl.esciencecenter.octopus.engine.util.StreamForwarder;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.Streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        
        if (!stdout.startsWith(File.separator)) { 
            stdout = workingDirectory + File.separator + stdout;
        }
        
        if (!stderr.startsWith(File.separator)) { 
            stderr = workingDirectory + File.separator + stderr;
        }

        if (stdin != null && !stdin.startsWith(File.separator)) { 
            stdin = workingDirectory + File.separator + stdin;
        }
        
        builder.command().add(description.getExecutable());
        builder.command().addAll(description.getArguments());
        builder.environment().putAll(description.getEnvironment());
        builder.directory(new java.io.File(workingDirectory));

        // Merge stdout and stderr into a single stream
        if (description.getMergeOutputStreams()) {             
            stdoutStream = new MergingOutputStream(new FileOutputStream(stdout));
            stderrStream = new MergingOutputStream(new FileOutputStream(stderr));
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
                stdinForwarders[i] = new StreamForwarder(new FileInputStream(stdin),
                                processes[i].getOutputStream());
            }

            if (description.getMergeOutputStreams()) { 
                stdoutForwarders[i] = new StreamForwarder(processes[i].getInputStream(), stdoutStream);
                stderrForwarders[i] = new StreamForwarder(processes[i].getErrorStream(), stderrStream);
            } else { 
                stdoutForwarders[i] = new StreamForwarder(processes[i].getInputStream(), 
                        new FileOutputStream(stdout + "." + i));
                 
                stderrForwarders[i] = new StreamForwarder(processes[i].getErrorStream(),
                        new FileOutputStream(stderr  + "." + i));
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

//    public int waitFor() throws InterruptedException {
//        
//        int[] results = new int[processes.length];
//
//        for (int i = 0; i < processes.length; i++) {
//            results[i] = processes[i].waitFor();
//        }
//
//        return results[0];
//    }

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
            LocalUtils.unixDestroy(processes[i]);
        }
    }

    @Override
    public Streams getStreams() {
        return null;
    }
}
