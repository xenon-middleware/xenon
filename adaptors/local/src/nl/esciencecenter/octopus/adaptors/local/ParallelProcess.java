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
package nl.esciencecenter.octopus.adaptors.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.engine.util.CommandRunner;
import nl.esciencecenter.octopus.engine.util.MergingOutputStream;
import nl.esciencecenter.octopus.engine.util.StreamForwarder;
import nl.esciencecenter.octopus.exceptions.OctopusException;

class ParallelProcess {

    private static final Logger logger = LoggerFactory.getLogger(ParallelProcess.class);

    private final Process[] processes;

    private final StreamForwarder[] stdinForwarders;
    private final StreamForwarder[] stdoutForwarders;
    private final StreamForwarder[] stderrForwarders;

    private final MergingOutputStream stdoutStream;
    private final MergingOutputStream stderrStream;

    ParallelProcess(int count, String executable, List<String> arguments, Map<String, String> environment,
            String workingDirectory, String stdin, String stdout, String stderr) throws IOException {
        ProcessBuilder builder = new ProcessBuilder();

        builder.command().add(executable);
        builder.command().addAll(arguments);

        builder.environment().putAll(environment);
        builder.directory(new java.io.File(workingDirectory));

        // buffered streams, will also synchronize
        stdoutStream = new MergingOutputStream(new FileOutputStream(workingDirectory + File.separator + stdout));
        stderrStream = new MergingOutputStream(new FileOutputStream(workingDirectory + File.separator + stderr));

        processes = new Process[count];
        stdinForwarders = new StreamForwarder[count];
        stdoutForwarders = new StreamForwarder[count];
        stderrForwarders = new StreamForwarder[count];

        for (int i = 0; i < count; i++) {
            processes[i] = builder.start();

            if (stdin == null) {
                stdinForwarders[i] = null;
                processes[i].getOutputStream().close();
            } else {
                stdinForwarders[i] =
                        new StreamForwarder(new FileInputStream(workingDirectory + File.separator + stdin),
                                processes[i].getOutputStream());
            }

            stdoutForwarders[i] = new StreamForwarder(processes[i].getInputStream(), stdoutStream);
            stderrForwarders[i] = new StreamForwarder(processes[i].getErrorStream(), stderrStream);
        }
    }

    void kill() {
        for (int i = 0; i < processes.length; i++) {
            processes[i].destroy();

            if (stdinForwarders[i] != null) {
                stdinForwarders[i].close();
            }

            stdoutForwarders[i].close();
            stderrForwarders[i].close();
        }
        try {
            stdoutStream.close();
        } catch (IOException e) {
            // IGNORE
        }
        try {
            stderrStream.close();
        } catch (IOException e) {
            // IGNORE
        }
    }

    public int waitFor() throws InterruptedException {
        int[] results = new int[processes.length];

        for (int i = 0; i < processes.length; i++) {
            results[i] = processes[i].waitFor();
        }

        return results[0];
    }

    private void unixDestroy(Process process) throws Throwable {
        Field pidField = process.getClass().getDeclaredField("pid");

        pidField.setAccessible(true);

        int pid = pidField.getInt(process);

        if (pid <= 0) {
            throw new Exception("Pid reported as 0 or negative: " + pid);
        }

        CommandRunner killRunner = new CommandRunner("kill", "-9", "" + pid);

        if (killRunner.getExitCode() != 0) {
            throw new OctopusException("Failed to kill process, exit code was " + killRunner.getExitCode() + " output: "
                    + killRunner.getStdout() + " error: " + killRunner.getStderr(), "local", null);
        }

    }

    public void destroy() {
        for (int i = 0; i < processes.length; i++) {
            try {
                unixDestroy(processes[i]);
            } catch (Throwable t) {
                logger.debug("Could not destroy process using getpid/kill, using normal java destroy", t);
                processes[i].destroy();
            }
        }
    }

}
