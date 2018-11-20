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
package nl.esciencecenter.xenon.adaptors.schedulers.local;

import static nl.esciencecenter.xenon.adaptors.schedulers.local.LocalSchedulerAdaptor.ADAPTOR_NAME;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.InteractiveProcess;
import nl.esciencecenter.xenon.adaptors.schedulers.StreamsImplementation;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.Streams;

/**
 * LocalInteractiveProcess implements a {@link InteractiveProcess} for local interactive processes.
 *
 * @version 1.0
 * @since 1.0
 */
class LocalInteractiveProcess implements InteractiveProcess {

    public final static int DEFAULT_TIMEOUT = 1000;

    private final Process process;

    private int exitCode;
    private boolean done;

    private final Streams streams;

    LocalInteractiveProcess(JobDescription description, String workdir, String jobIdentifier) throws XenonException {
        ProcessBuilder builder = new ProcessBuilder();

        builder.command().add(description.getExecutable());
        builder.command().addAll(description.getArguments());
        builder.environment().putAll(description.getEnvironment());

        String workingDirectory = workdir;

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

    private boolean destroyProcess(ProcessHandle s, int timeout, TimeUnit unit) {

        if (s.isAlive() && s.destroy()) {
            try {
                s.onExit().get(timeout, unit);
                return true;
            } catch (Exception e) {
                // Failed to destroy
            }
        }

        if (s.isAlive() && s.destroyForcibly()) {
            try {
                s.onExit().get(timeout, unit);
                return true;
            } catch (Exception e) {
                // Failed to destroy
            }
        }

        return !s.isAlive();
    }

    /**
     * Destroy (stop) process. Does nothing if the process has already finished. Will try a destroy first, followed by a destroyForcibly if the process has not
     * terminated after the given timeout. All subprocessed that can be found will also be destroyed.
     * 
     * @param timeout
     *            the timeout for each destroy and destroyForcibly operation.
     * @param unit
     *            the unit of the timeout.
     */
    public void destroy(int timeout, TimeUnit unit) {

        if (done) {
            return;
        }

        // Try to kill the process and all its children.
        ProcessHandle h = process.toHandle();

        destroyProcess(h, timeout, unit);

        h.descendants().forEach(s -> {
            destroyProcess(s, timeout, TimeUnit.MILLISECONDS);
        });
    }

    /**
     * Destroy (stop) process. Does nothing if the process has already finished. Will try a destroy first, followed by a destroyForcibly if the process has not
     * terminated after 1 second. All subprocessed that can be found will also be destroyed.
     */
    public void destroy() {
        destroy(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
    }
}
