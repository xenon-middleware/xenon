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
package nl.esciencecenter.xenon.adaptors.schedulers.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.junit.Test;

import nl.esciencecenter.xenon.adaptors.shared.ssh.SSHUtil;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.Streams;
import nl.esciencecenter.xenon.utils.InputWriter;
import nl.esciencecenter.xenon.utils.OutputReader;

public abstract class SshInteractiveProcessITest {

    public abstract String getLocation();

    public abstract Credential getCorrectCredential();

    @Test
    public void test_run_hostname() throws Exception {
        SshClient client = SSHUtil.createSSHClient(false, false, false);
        ClientSession session = SSHUtil.connect("test", client, getLocation(), getCorrectCredential(), 10*1000);

        JobDescription desc = new JobDescription();
        desc.setExecutable("/bin/hostname");

        String id = "TESTID";

        SshInteractiveProcess p = new SshInteractiveProcess(session, desc, id);

        Streams s = p.getStreams();

        assertNotNull(s.getStdin());
        assertNotNull(s.getStdout());
        assertNotNull(s.getStderr());

        // No input, so close stdin
        s.getStdin().close();

        OutputReader stdout = new OutputReader(s.getStdout());
        OutputReader stderr = new OutputReader(s.getStderr());

        stderr.waitUntilFinished();
        stdout.waitUntilFinished();

        String output = stdout.getResultAsString();
        String error = stderr.getResultAsString();

        assertTrue(error.isEmpty());
        assertFalse(output.isEmpty());
        assertEquals(0, p.getExitStatus());
    }


    @Test
    public void test_run_cat() throws Exception {
        SshClient client = SSHUtil.createSSHClient(false, false, false);
        ClientSession session = SSHUtil.connect("test", client, getLocation(), getCorrectCredential(), 10*1000);

        JobDescription desc = new JobDescription();
        desc.setExecutable("/bin/cat");

        String id = "TESTID";

        SshInteractiveProcess p = new SshInteractiveProcess(session, desc, id);

        Streams s = p.getStreams();

        assertNotNull(s.getStdin());
        assertNotNull(s.getStdout());
        assertNotNull(s.getStderr());

        String message = "Hello World!";

        InputWriter stdin = new InputWriter(message, s.getStdin());
        OutputReader stdout = new OutputReader(s.getStdout());
        OutputReader stderr = new OutputReader(s.getStderr());

        stdin.waitUntilFinished();
        stderr.waitUntilFinished();
        stdout.waitUntilFinished();

        String output = stdout.getResultAsString();
        String error = stderr.getResultAsString();

        assertTrue(error.isEmpty());
        assertEquals(message, output);
        assertEquals(0, p.getExitStatus());
    }


    @Test
    public void test_exitStatusBeforeFinish() throws Exception {
        SshClient client = SSHUtil.createSSHClient(false, false, false);
        ClientSession session = SSHUtil.connect("test", client, getLocation(), getCorrectCredential(), 10*1000);

        JobDescription desc = new JobDescription();
        desc.setExecutable("/bin/sleep");
        desc.addArgument("5");

        String id = "TESTID";

        SshInteractiveProcess p = new SshInteractiveProcess(session, desc, id);

        // Not done yet, so exit returns -1
        assertEquals(-1, p.getExitStatus());

        Streams s = p.getStreams();

        assertNotNull(s.getStdin());
        assertNotNull(s.getStdout());
        assertNotNull(s.getStderr());

        s.getStdin().close();

        OutputReader stdout = new OutputReader(s.getStdout());
        OutputReader stderr = new OutputReader(s.getStderr());

        stderr.waitUntilFinished();
        stdout.waitUntilFinished();

        String output = stdout.getResultAsString();
        String error = stderr.getResultAsString();

        assertTrue(error.isEmpty());
        assertTrue(output.isEmpty());


        // Done yet, so exit returns 0
        assertEquals(0, p.getExitStatus());
    }

}
