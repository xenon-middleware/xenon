/**
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
package nl.esciencecenter.xenon.adaptors.schedulers.local;

import static nl.esciencecenter.xenon.adaptors.schedulers.local.LocalSchedulerAdaptor.ADAPTOR_NAME;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.InteractiveProcess;
import nl.esciencecenter.xenon.adaptors.schedulers.StreamsImplementation;
import nl.esciencecenter.xenon.adaptors.shared.local.LocalUtil;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.Streams;


/**
 * LocalInteractiveProcess implements a {@link InteractiveProcess} for local interactive processes.
 * 
 * @version 1.0
 * @since 1.0
 */
class LocalInteractiveProcess implements InteractiveProcess {
    private final Process process;

    private int exitCode;
    private boolean done;

    private final Streams streams;

    LocalInteractiveProcess(JobDescription description, String jobIdentifier) throws XenonException {
        ProcessBuilder builder = new ProcessBuilder();

        builder.command().add(description.getExecutable());
        builder.command().addAll(description.getArguments());
        builder.environment().putAll(description.getEnvironment());

        String workingDirectory = description.getWorkingDirectory();
        
        if (workingDirectory == null) {
            workingDirectory = System.getProperty("user.dir");
        }

        builder.directory(new File(workingDirectory));

        try { 
            process = builder.start();
        } catch (IOException e) { 
            throw new XenonException(ADAPTOR_NAME, "Failed to start local process!", e);
        }
        streams = new StreamsImplementation(jobIdentifier, process.getInputStream(), process.getOutputStream(), process.getErrorStream());
    }

    public Streams getStreams() {
        return streams;
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
            // exit code cannot be found: indicates the process has not yet finished
            return false;
        }
    }

    public int getExitStatus() {
        return exitCode;
    }

    /**
     * Destroy (stop) process.
     * Does nothing if the process has already finished. Does not
     * re-evaluate whether process has finished. Will run the kill command on
     * Unix, and Process.destroy() if that does not work or does not apply.
     */
    public void destroy() {
        if (done) {
            return;
        }

        boolean success = false;

        if (!LocalUtil.isWindows()) { 
            try {
                final Field pidField = process.getClass().getDeclaredField("pid");

                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        pidField.setAccessible(true);
                        return null;
                    }
                });

                int pid = pidField.getInt(process);

                if (pid > 0) {
                    CommandRunner killRunner = new CommandRunner("kill", "-9", "" + pid);
                    success = (killRunner.getExitCode() == 0);
                }
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException | XenonException e) {
                // Failed, so use the regular Java destroy.
            }
        }
            
        if (!success) {
            process.destroy();
        }
    }
}
