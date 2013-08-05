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
package nl.esciencecenter.octopus.adaptors.scripting;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.engine.util.InputWriter;
import nl.esciencecenter.octopus.engine.util.OutputReader;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.jobs.Streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs a command. Constructor waits for command to finish.
 * 
 * @author Niels Drost
 * 
 */
public class RemoteCommandRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteCommandRunner.class);

    private final int exitCode;

    private final String output;

    private final String error;

    /**
     * Run a command remotely, and save stdout, stderr, and exit code for later processing.
     * 
     * @param octopus
     *            the octopus to use
     * @param scheduler
     *            the scheduler to submit the job to
     * @param adaptorName
     *            the name of the adaptor running this command (used in exception if thrown)
     * @param stdin
     *            input to feed to the command
     * @param executable
     *            command to run
     * @param arguments
     *            arguments for the command
     * @throws OctopusException
     *             if the job could not be run successfully.
     * @throws OctopusIOException
     *             if the job could not be run successfully.
     */
    public RemoteCommandRunner(Octopus octopus, Scheduler scheduler, String adaptorName, String stdin, String executable,
            String... arguments) throws OctopusException, OctopusIOException {
        long start = System.currentTimeMillis();

        JobDescription description = new JobDescription();
        description.setInteractive(true);
        description.setExecutable(executable);
        description.setArguments(arguments);

        Job job = octopus.jobs().submitJob(scheduler, description);

        Streams streams = octopus.jobs().getStreams(job);

        InputWriter in = new InputWriter(stdin, streams.getStdin());

        // we must always read the output and error streams to avoid deadlocks
        OutputReader out = new OutputReader(streams.getStdout());
        OutputReader err = new OutputReader(streams.getStderr());

        in.waitUntilFinished();
        out.waitUntilFinished();
        err.waitUntilFinished();

        JobStatus status = octopus.jobs().getJobStatus(job);

        if (!status.isDone()) {

            status = octopus.jobs().waitUntilDone(job, 0);
        }

        if (status.hasException()) {
            throw new OctopusException(adaptorName, "Could not run command remotely", status.getException());
        }

        this.exitCode = status.getExitCode();
        this.output = out.getResult();
        this.error = err.getResult();

        long runtime = System.currentTimeMillis() - start;

        LOGGER.debug("CommandRunner took {} ms, executable = {}, arguments = {}, exitcode = {}, stdout:\n{}\nstderr:\n{}",
                runtime, executable, arguments, exitCode, output, error);
    }

    public String getStdout() {
        return output;
    }

    public String getStderr() {
        return error;
    }

    public int getExitCode() {
        return exitCode;
    }

    public boolean success() {
        return exitCode == 0 && error.isEmpty();
    }

    public String toString() {
        return "CommandRunner[exitCode=" + exitCode + ",output=" + output + ",error=" + error + "]";
    }
}
