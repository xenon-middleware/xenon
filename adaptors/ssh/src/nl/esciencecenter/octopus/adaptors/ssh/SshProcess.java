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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.octopus.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.OctopusRuntimeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshProcess {
    private static final Logger logger = LoggerFactory.getLogger(SshProcess.class);

    private SshAdaptor adaptor;
    @SuppressWarnings("unused")
    private SchedulerImplementation scheduler;
    private Session session;
    private String executable;
    private List<String> arguments;
    private Map<String, String> environment;
    private String stdin;
    private String stdout;
    private String stderr;

    private FileOutputStream fosStdOut;
    private FileOutputStream fosStderr;
    private FileInputStream fis;
    private ChannelExec channel;

    public SshProcess(SshAdaptor adaptor, SchedulerImplementation scheduler, Session session, String executable,
            List<String> arguments, Map<String, String> environment, String stdin, String stdout, String stderr) {
        super();
        this.adaptor = adaptor;
        this.scheduler = scheduler;
        this.session = session;
        this.executable = executable;
        this.arguments = arguments;
        this.environment = environment;
        this.stdin = stdin;
        this.stdout = stdout;
        this.stderr = stderr;

        try {
            run();
        } catch (OctopusIOException | OctopusException e) {
            // FIXME !!!!
            throw new OctopusRuntimeException(adaptor.getName(), e.getMessage(), e);
        }
    }

    void run() throws OctopusException, OctopusIOException {
        logger.debug("ssh process");

        channel = adaptor.getExecChannel(session);

        String command = executable;

        for (String s : arguments) {
            command += " " + s;
        }

        channel.setCommand(command);

        logger.debug("command = " + command);

        // TODO make property for X Forwarding
        // channel.setXForwarding(true);

        // set the streams first, then connect the channel.

        if (stdin != null && stdin.length() != 0) {

            try {
                fis = new FileInputStream(stdin);
            } catch (FileNotFoundException e) {
                throw new OctopusIOException(adaptor.getName(), e.getMessage(), e);
            }
            channel.setInputStream(fis);
        } else {
            channel.setInputStream(null);
        }

        fosStdOut = null;
        if (stdout != null && stdout.length() != 0) {
            try {
                fosStdOut = new FileOutputStream(stdout);
            } catch (FileNotFoundException e) {
                throw new OctopusIOException(adaptor.getName(), e.getMessage(), e);
            }
            channel.setOutputStream(fosStdOut);
        } else {
            channel.setOutputStream(null);
        }

        fosStderr = null;
        if (stderr != null && stderr.length() != 0) {
            try {
                fosStderr = new FileOutputStream(stderr);
            } catch (FileNotFoundException e) {
                throw new OctopusIOException(adaptor.getName(), e.getMessage(), e);
            }
            channel.setErrStream(fosStderr);
        } else {
            channel.setErrStream(null);
        }

        logger.debug("stdin = " + stdin + ", stdout = " + stdout + ", stderr = " + stderr);

        for (Map.Entry<String, String> entry : environment.entrySet()) {
            channel.setEnv(entry.getKey(), entry.getValue());
        }

        logger.debug("connecting channel");
        try {
            channel.connect();
        } catch (JSchException e) {
            throw new OctopusIOException(adaptor.getName(), e.getMessage(), e);
        }
        logger.debug("connecting channel done");

    }

    int waitFor() throws InterruptedException {
        while (true) {
            if (channel.isClosed()) {
                logger.debug("exit-status: " + channel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
            }
        }

        channel.disconnect();

        if (fosStderr != null) {
            try {
                fosStderr.close();
            } catch (IOException e) {
                // ignore
            }
        }
        if (fosStdOut != null) {
            try {
                fosStdOut.close();
            } catch (IOException e) {
                // ignore
            }
        }
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                // ignore
            }
        }

        return channel.getExitStatus();
    }

    void destroy() {
        // TODO
    }
}
