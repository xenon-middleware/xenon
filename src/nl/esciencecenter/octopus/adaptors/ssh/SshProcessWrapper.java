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

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.StreamsImplementation;
import nl.esciencecenter.octopus.engine.util.ProcessWrapper;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.Streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshProcessWrapper implements ProcessWrapper {
    
    private static final Logger logger = LoggerFactory.getLogger(SshProcessWrapper.class);

    private final boolean isInteractive;
    
    private FileOutputStream stdout;
    private FileOutputStream stderr;
    private FileInputStream stdin;
    
    private ChannelExec channel;
    
    private Streams streams; 

    public SshProcessWrapper(Adaptor adaptor, Session session, JobImplementation job) throws OctopusIOException {
        
        if (!(adaptor instanceof SshAdaptor)) { 
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, "INTERNAL ERROR: Adaptor mismatch!");
        }
        
        SshAdaptor sshAdaptor = (SshAdaptor) adaptor;
        
        JobDescription description = job.getJobDescription();
        
        isInteractive = description.isInteractive();
        
        logger.debug("ssh process");

        channel = sshAdaptor.getExecChannel(session);

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
        
        logger.debug("command = " + command);

        // TODO make property for X Forwarding
        // channel.setXForwarding(true);

        // set the streams first, then connect the channel.
        if (isInteractive) {
            try {
                streams = new StreamsImplementation(job, channel.getInputStream(), channel.getOutputStream(), 
                        channel.getErrStream());
                
            } catch (IOException e) {
                throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
            }
        } else {

            String stdinPath = description.getStdin();
            
            if (stdinPath != null && stdinPath.length() != 0) {
                try {
                    stdin = new FileInputStream(stdinPath);
                } catch (FileNotFoundException e) {
                    throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
                }
                channel.setInputStream(stdin);
            } else {
                channel.setInputStream(null);
            }

            String stdoutPath = description.getStdout();
            
            if (stdoutPath != null && stdoutPath.length() != 0) {
                try {
                    stdout = new FileOutputStream(stdoutPath);
                } catch (FileNotFoundException e) {
                    throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
                }
                channel.setOutputStream(stdout);
            } else {
                channel.setOutputStream(null);
            }

            String stderrPath = description.getStderr();

            if (stderrPath != null && stderrPath.length() != 0) {
                try {
                    stderr = new FileOutputStream(stderrPath);
                } catch (FileNotFoundException e) {
                    throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
                }
                channel.setErrStream(stderr);
            } else {
                channel.setErrStream(null);
            }

            logger.debug("stdin = " + stdinPath + ", stdout = " + stdoutPath + ", stderr = " + stderrPath);
        }

        logger.debug("connecting channel");

        try {
            channel.connect();
        } catch (JSchException e) {
            throw new OctopusIOException(SshAdaptor.ADAPTOR_NAME, e.getMessage(), e);
        }
        logger.debug("Connecting channel done");
    }

    public Streams getStreams() { 
        return streams;
    }

    @Override
    public void destroy() {

        // FIXME: Not sure if this works!!!
        if (isDone()) { 
            return;
        }
        
        try {
            channel.sendSignal("KILL");
        } catch (Exception e) {
            logger.debug("Failed to kill remote process!", e);
        }

        channel.disconnect();
        closeStreams();
    }
    
    private void close(Closeable c) { 
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
    
    private void closeStreams() { 
        
        close(stdin);
        close(stdout);
        close(stderr);
    
        stdin = null;
        stdout = null;
        stderr = null;        
    }
    
    @Override
    public boolean isDone() {
        
        boolean done = channel.isClosed();
        
        if (done && !isInteractive) { 
            closeStreams();
        }
        
        return done;
    }

    @Override
    public int getExitStatus() {
        return channel.getExitStatus();
    }
}
