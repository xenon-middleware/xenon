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

package nl.esciencecenter.xenon.jobs;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.adaptors.ssh.SSHJobTestConfig;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.util.Utils;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class ITMultiJobTest {
    private String readFully(InputStream in) throws IOException {
        byte[] buffer = new byte[1024];

        int offset = 0;

        try {
            int tmp = in.read(buffer, 0, buffer.length);

            while (tmp != -1) {
                offset += tmp;

                if (offset == buffer.length) {
                    buffer = Arrays.copyOf(buffer, buffer.length * 2);
                }

                tmp = in.read(buffer, offset, buffer.length - offset);
            }
        } finally {
            in.close();
        }
        return new String(buffer, 0, offset);
    }
   
    private void submitToQueueWithPolling(String testName, String queueName, int jobCount) throws Exception {
        System.err.println("STARTING TEST submitToQueueWithPolling(" + testName + ", " + queueName + ", " + jobCount + ")");

        SSHJobTestConfig config = new SSHJobTestConfig(null);

        String user = config.getPropertyOrFail("test.ssh.user");
        String location = config.getPropertyOrFail("test.ssh.location");
        
        String TEST_ROOT = "xenon_test_SSH_" + System.currentTimeMillis();

        Xenon xenon = XenonFactory.newXenon(null);
        Files files = xenon.files();
        Jobs jobs = xenon.jobs();
        Credentials credentials = xenon.credentials();

        FileSystem filesystem = files.newFileSystem("sftp", user + "@" + location, credentials.getDefaultCredential("sftp"),
                new HashMap<String, String>(0));
        Scheduler scheduler = jobs.newScheduler("ssh", user + "@" + location, credentials.getDefaultCredential("ssh"),
                new HashMap<String, String>(0));

        String workingDir = TEST_ROOT + "/" + testName;

        Path root = Utils.resolveWithEntryPath(files, filesystem, workingDir);
        
        files.createDirectories(root);

        Path[] out = new Path[jobCount];
        Path[] err = new Path[jobCount];

        Job[] j = new Job[jobCount];

        for (int i = 0; i < j.length; i++) {
            out[i] = Utils.resolveWithRoot(files, root, "stdout" + i + ".txt");
            err[i] = Utils.resolveWithRoot(files, root, "stderr" + i + ".txt");

            JobDescription description = new JobDescription();
            description.setExecutable("/bin/sleep");
            description.setArguments("1");
            description.setWorkingDirectory(workingDir);

            description.setQueueName(queueName);
            description.setInteractive(false);
            description.setStdin(null);
            description.setStdout("stdout" + i + ".txt");
            description.setStderr("stderr" + i + ".txt");

            j[i] = jobs.submitJob(scheduler, description);
        }

        // Bit hard to determine realistic deadline here ?
        long deadline = System.currentTimeMillis() + (60 * jobCount * 1000);

        boolean done = false;

        while (!done) {
            JobStatus[] status = jobs.getJobStatuses(j);

            int count = 0;

            for (int i = 0; i < j.length; i++) {
                if (j[i] != null) {
                    if (status[i].isDone()) {
                        if (status[i].hasException()) {
                            System.err.println("Job " + i + " failed!");
                            throw new Exception("Job " + i + " failed", status[i].getException());
                        }

                        System.err.println("Job " + i + " done.");
                        j[i] = null;
                    } else {
                        count++;
                    }
                }
            }

            if (count == 0) {
                done = true;
            } else {
                Thread.sleep(1000);

                long now = System.currentTimeMillis();

                if (now > deadline) {
                    throw new Exception("Job exceeded deadline!");
                }
            }
        }

        for (int i = 0; i < j.length; i++) {
            String tmpout = readFully(files.newInputStream(out[i]));
            String tmperr = readFully(files.newInputStream(err[i]));

            assertTrue(tmpout != null);
            assertTrue(tmpout.length() == 0);

            assertTrue(tmperr != null);
            assertTrue(tmperr.length() == 0);

            files.delete(out[i]);
            files.delete(err[i]);
        }

        jobs.close(scheduler);
        files.delete(root);
        files.close(filesystem);

        XenonFactory.endXenon(xenon);
    }

    @org.junit.Test
    public void testMultiBatchJobSubmitWithPolling() throws Exception {
        submitToQueueWithPolling("testMultiBatchJobSubmitWithPolling", "unlimited", 100);
    }
}
