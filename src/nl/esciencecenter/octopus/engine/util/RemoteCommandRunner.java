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
package nl.esciencecenter.octopus.engine.util;

import nl.esciencecenter.octopus.Octopus;
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

    protected static Logger logger = LoggerFactory.getLogger(RemoteCommandRunner.class);

    private final int exitCode;

    private final OutputReader out;

    private final OutputReader err;

    public RemoteCommandRunner(Octopus octopus, Scheduler scheduler, String stdin, JobDescription jobDescription)
            throws OctopusException, OctopusIOException {
        jobDescription.setInteractive(true);

        Job job = octopus.jobs().submitJob(scheduler, jobDescription);

        Streams streams = octopus.jobs().getStreams(job);

        //write given content to stdin of process
        if (stdin == null) {
            stdin = "";
        }
        InputWriter in = new InputWriter(stdin, streams.getStdin());

        // we must always read the output and error streams to avoid deadlocks
        out = new OutputReader(streams.getStdout());
        err = new OutputReader(streams.getStderr());

        in.waitUntilFinished();
        out.waitUntilFinished();
        err.waitUntilFinished();

        while (true) {
            JobStatus status = octopus.jobs().getJobStatus(job);

            if (status.hasException()) {
                throw new OctopusException("engine", "Could not run command remotely", status.getException());
            }

            if (status.isDone()) {
                this.exitCode = status.getExitCode();
                if (logger.isDebugEnabled()) {
                    logger.debug("CommandRunner out: " + out.getResult() + "\n" + "CommandRunner err: " + err.getResult());
                }
                return;
            }
            
            //wait for a bit
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public String getStdout() {
        return out.getResult();
    }

    public String getStderr() {
        return err.getResult();
    }

    public int getExitCode() {
        return exitCode;
    }

}
