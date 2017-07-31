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
package nl.esciencecenter.xenon.adaptors.schedulers.ssh;

import static nl.esciencecenter.xenon.adaptors.schedulers.ssh.SshSchedulerAdaptor.ADAPTOR_NAME;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.CommandLineUtils;
import nl.esciencecenter.xenon.adaptors.schedulers.InteractiveProcess;
import nl.esciencecenter.xenon.adaptors.schedulers.StreamsImplementation;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.Streams;

import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.session.ClientSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LocalBatchProcess implements a {@link InteractiveProcess} for local batch processes.
 */
class SshInteractiveProcess implements InteractiveProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshInteractiveProcess.class);

    private final ClientSession session;
    private final ChannelExec channel;
    private final Streams streams;
    private boolean done = false;

    // FIXME: should be property or parameter!
    private final long timeout = 10*1000;

    SshInteractiveProcess(ClientSession session, JobDescription description, String jobIdentifier) throws XenonException {

        if (session == null) {
            throw new IllegalArgumentException("Session is null");
        }

           if (description == null) {
            throw new IllegalArgumentException("Job description is null");
        }

           if (jobIdentifier == null) {
            throw new IllegalArgumentException("Job identifier is null");
        }

        this.session = session;

        try {
            this.channel = session.createExecChannel(buildCommand(description));

            Map<String, String> environment = description.getEnvironment();

            for (Entry<String, String> entry : environment.entrySet()) {
                channel.setEnv(entry.getKey(), entry.getValue());
            }

            // TODO: Add agent FW
            // channel.setAgentForwarding(session.useAgentForwarding());

            channel.open().verify(timeout);

            // set the streams first, then connect the channel.
            streams = new StreamsImplementation(jobIdentifier, channel.getInvertedOut(), channel.getInvertedIn(), channel.getInvertedErr());

        } catch (Exception e) {
            throw new XenonException(ADAPTOR_NAME, "Failed to start command", e);
        }
    }

    private static String buildCommand(JobDescription description) {
        StringBuilder command = new StringBuilder(200);

        String workdir = description.getWorkingDirectory();

        if (workdir != null) {
            command.append("cd ");
            command.append(CommandLineUtils.protectAgainstShellMetas(workdir));
            command.append(" && ");
        }

        command.append(description.getExecutable());

        for (String s : description.getArguments()) {
            command.append(" ");
            command.append(CommandLineUtils.protectAgainstShellMetas(s));
        }

        return command.toString();
    }

    @Override
    public Streams getStreams() {
        return streams;
    }

    private void cleanup() {
        try {
            channel.close(false).await();
        } catch (IOException e) {
            LOGGER.warn("SshInteractiveProcess failed to release exec channel!", e);
        }
    }

    @Override
    public synchronized boolean isDone() {
        if (done) {
            return true;
        }

        boolean tmp = channel.isClosed();

        if (tmp) {
            done = true;
            cleanup();
        }

        return done;
    }

    @Override
    public int getExitStatus() {

        Integer status = channel.getExitStatus();

        if (status == null) {
            return -1;
        }

        return status.intValue();
    }

    @Override
    public void destroy() {
        if (isDone()) {
            return;
        }

        try {
            // TODO: Unclear how to send signal ?
            // channel.sendSignal("KILL");
            channel.close(true).await();
        } catch (Exception e) {
            LOGGER.warn("SshInteractiveProcess failed to kill remote process!", e);
        }

        cleanup();
    }
}
