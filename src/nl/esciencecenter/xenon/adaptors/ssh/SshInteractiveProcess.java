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

package nl.esciencecenter.xenon.adaptors.ssh;

import java.util.Map;
import java.util.Map.Entry;

import nl.esciencecenter.xenon.CobaltException;
import nl.esciencecenter.xenon.engine.jobs.StreamsImplementation;
import nl.esciencecenter.xenon.engine.util.CommandLineUtils;
import nl.esciencecenter.xenon.engine.util.InteractiveProcess;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.Streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;

/**
 * LocalBatchProcess implements a {@link InteractiveProcess} for local batch processes.
 * 
 * @author Rob van Nieuwpoort <R.vanNieuwpoort@esciencecenter.nl>
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class SshInteractiveProcess implements InteractiveProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshInteractiveProcess.class);
    
    private final SshMultiplexedSession session;
    private final ChannelExec channel;
    private final Streams streams;
    private boolean done = false;
    
    public SshInteractiveProcess(SshMultiplexedSession session, Job job) throws CobaltException {

        this.session = session;
        this.channel = session.getExecChannel();

        JobDescription description = job.getJobDescription();

        StringBuilder command = new StringBuilder(description.getExecutable());

        for (String s : description.getArguments()) {
            command.append(" ");
            command.append(CommandLineUtils.protectAgainstShellMetas(s));
        }

        channel.setCommand(command.toString());

        Map<String, String> environment = description.getEnvironment();

        for (Entry<String, String> entry : environment.entrySet()) {
            channel.setEnv(entry.getKey(), entry.getValue());
        }
    
        // set the streams first, then connect the channel.
        try {
            streams = new StreamsImplementation(job, channel.getInputStream(), channel.getOutputStream(), channel.getErrStream());
        } catch (Exception e) {
            session.failedExecChannel(channel);
            throw new CobaltException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }

        try {
            channel.connect();
        } catch (Exception e) {
            session.failedExecChannel(channel);
            throw new CobaltException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }
    }

    @Override
    public Streams getStreams() {
        return streams;
    }

    private void cleanup() {
        try {
            session.releaseExecChannel(channel);
        } catch (CobaltException e) {
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

        return tmp;
    }

    @Override
    public int getExitStatus() {
        return channel.getExitStatus();
    }

    @Override
    public void destroy() {

        if (isDone()) {
            return;
        }

        try {
            channel.sendSignal("KILL");
        } catch (Exception e) {
            LOGGER.warn("SshInteractiveProcess failed to kill remote process!", e);
        }

        cleanup();
    }
}
