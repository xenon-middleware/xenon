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
package nl.esciencecenter.xenon.adaptors.local;

import java.io.File;
import java.io.IOException;
import java.util.List;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.engine.jobs.JobImplementation;
import nl.esciencecenter.xenon.engine.jobs.StreamsImplementation;
import nl.esciencecenter.xenon.engine.util.InteractiveProcess;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.Streams;
import nl.esciencecenter.xenon.util.JavaJobDescription;

/**
 * LocalInteractiveProcess implements a {@link InteractiveProcess} for local interactive processes.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
class LocalInteractiveProcess implements InteractiveProcess {
    private final Process process;

    private int exitCode;
    private boolean done;

    private final Streams streams;

    LocalInteractiveProcess(JobImplementation job) throws XenonException {
        JobDescription description = job.getJobDescription();

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
            throw new XenonException(LocalAdaptor.ADAPTOR_NAME, "Failed to start local process!", e);
        }
        streams = new StreamsImplementation(job, process.getInputStream(), process.getOutputStream(), process.getErrorStream());
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

        LocalUtils.unixDestroy(process);
    }
}
