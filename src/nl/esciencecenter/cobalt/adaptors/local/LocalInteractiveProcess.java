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
package nl.esciencecenter.cobalt.adaptors.local;

import java.io.File;
import java.io.IOException;
import java.util.List;

import nl.esciencecenter.cobalt.CobaltException;
import nl.esciencecenter.cobalt.engine.jobs.JobImplementation;
import nl.esciencecenter.cobalt.engine.jobs.StreamsImplementation;
import nl.esciencecenter.cobalt.engine.util.InteractiveProcess;
import nl.esciencecenter.cobalt.jobs.JobDescription;
import nl.esciencecenter.cobalt.jobs.Streams;
import nl.esciencecenter.cobalt.util.JavaJobDescription;

/**
 * LocalInteractiveProcess implements a {@link InteractiveProcess} for local interactive processes.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
class LocalInteractiveProcess implements InteractiveProcess {

    private final java.lang.Process process;

    private int exitCode;
    private boolean done;

    private Streams streams;

    LocalInteractiveProcess(JobImplementation job) throws CobaltException {

        JobDescription description = job.getJobDescription();

        ProcessBuilder builder = new ProcessBuilder();

        builder.command().add(description.getExecutable());

        //We need to special case for the java job description here,
        //as it needs to be told the path separator in case the target is a windows machine.
        List<String> arguments;
        if (description instanceof JavaJobDescription) {
            JavaJobDescription javaDescription = (JavaJobDescription) description;
            arguments = javaDescription.getArguments(File.pathSeparatorChar);
        } else {
            arguments = description.getArguments();
        }

        builder.command().addAll(arguments);
        builder.environment().putAll(description.getEnvironment());

        String workingDirectory = description.getWorkingDirectory();

        if (workingDirectory == null) {
            workingDirectory = System.getProperty("user.dir");
        }

        builder.directory(new java.io.File(workingDirectory));
        
        try { 
            process = builder.start();
        } catch (IOException e) { 
            throw new CobaltException(LocalAdaptor.ADAPTOR_NAME, "Failed to start local process!", e);
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
