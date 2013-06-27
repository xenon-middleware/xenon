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

package nl.esciencecenter.octopus.adaptors.ssh;

import java.util.Map;
import java.util.Map.Entry;

import com.jcraft.jsch.ChannelExec;

import nl.esciencecenter.octopus.engine.jobs.StreamsImplementation;
import nl.esciencecenter.octopus.engine.util.InteractiveProcess;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.Streams;

/**
 * LocalBatchProcess implements a {@link InteractiveProcess} for local batch processes. 
 * 
 * @author Rob van Nieuwpoort <R.vanNieuwpoort@esciencecenter.nl>
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class SshInteractiveProcess  implements InteractiveProcess {
    
    private ChannelExec channel;    
    private Streams streams; 
    
    public SshInteractiveProcess(ChannelExec channel, Job job) throws OctopusIOException {
        
        this.channel = channel;

        JobDescription description = job.getJobDescription();
        
        String command = description.getExecutable();

        for (String s : description.getArguments()) {
            command += " " + s;
        }

        channel.setCommand(command);

        Map<String, String> environment = description.getEnvironment();
        
        if (environment != null) { 
            
            for (Entry<String, String> entry : environment.entrySet()) { 
                channel.setEnv(entry.getKey(), entry.getValue());
            }
        }
        
        // TODO make property for X Forwarding
        // channel.setXForwarding(true);

        // set the streams first, then connect the channel.
        try {
            streams = new StreamsImplementation(job, channel.getInputStream(), channel.getOutputStream(), channel.getErrStream());                
        } catch (Exception e) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }
        
        try {
            channel.connect();
        } catch (Exception e) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }
    }

    @Override
    public Streams getStreams() { 
        return streams;
    }

    @Override
    public boolean isDone() {
        return channel.isClosed();
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
            // logger.debug("Failed to kill remote process!", e);
        }

        channel.disconnect();
    }
}
