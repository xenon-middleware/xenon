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
package nl.esciencecenter.xenon.adaptors.schedulers.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.io.OutputStream;

import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.Streams;
import nl.esciencecenter.xenon.utils.LocalFileSystemUtils;
import nl.esciencecenter.xenon.utils.OutputReader;

public class LocalInteractiveProcessTest {

    @Test
    public void test_simpleJob() throws Exception {

        assumeFalse(LocalFileSystemUtils.isWindows());

        JobDescription job = new JobDescription();
        job.setExecutable("/bin/cat");

        LocalInteractiveProcess p = new LocalInteractiveProcess(job, null, "job42");

        Streams streams = p.getStreams();

        assertEquals("job42", streams.getJobIdentifier());

        OutputReader out = new OutputReader(streams.getStdout());
        OutputReader err = new OutputReader(streams.getStderr());

        OutputStream stdin = streams.getStdin();

        stdin.write("Hello World\n".getBytes());
        stdin.write("Goodbye World\n".getBytes());
        stdin.close();

        out.waitUntilFinished();
        err.waitUntilFinished();

        // Wait up to 10 x 100 ms. until process is done.
        int count = 0;

        while (!p.isDone() && count < 10) {
            Thread.sleep(100);
            count++;
        }

        assertTrue("Process not done", p.isDone());
        assertEquals("Exitcode not 0", 0, p.getExitStatus());
        assertEquals("Hello World\nGoodbye World\n", out.getResultAsString());
    }

    @Test(expected = XenonException.class)
    public void test_simpleJob_unknownWorkDir_throwsException() throws Exception {

        assumeFalse(LocalFileSystemUtils.isWindows());

        JobDescription job = new JobDescription();
        job.setExecutable("/bin/cat");
        job.setWorkingDirectory("/foo");

        new LocalInteractiveProcess(job, "/foo", "job42");
    }

    @Test
    public void test_simpleJob_destroyAfterDone() throws Exception {

        assumeFalse(LocalFileSystemUtils.isWindows());

        JobDescription job = new JobDescription();
        job.setExecutable("/bin/cat");

        LocalInteractiveProcess p = new LocalInteractiveProcess(job, null, "job42");

        Streams streams = p.getStreams();

        assertEquals("job42", streams.getJobIdentifier());

        OutputReader out = new OutputReader(streams.getStdout());
        OutputReader err = new OutputReader(streams.getStderr());

        OutputStream stdin = streams.getStdin();

        stdin.write("Hello World\n".getBytes());
        stdin.write("Goodbye World\n".getBytes());
        stdin.close();

        out.waitUntilFinished();
        err.waitUntilFinished();

        assertEquals("Hello World\nGoodbye World\n", out.getResultAsString());

        while (!p.isDone()) {
            Thread.sleep(500);
        }

        // Should be a noop
        p.destroy();

        assertTrue(p.isDone());
    }

    @Test
    public void test_simpleJob_destroyBeforeDone() throws Exception {

        assumeFalse(LocalFileSystemUtils.isWindows());

        JobDescription job = new JobDescription();
        job.setExecutable("/bin/sleep");
        job.setArguments("60");

        LocalInteractiveProcess p = new LocalInteractiveProcess(job, null, "job42");

        p.getStreams().getStdin().close();

        assertFalse(p.isDone());

        // Should be kill the process
        p.destroy();

        Thread.sleep(250);

        assertTrue(p.isDone());
    }

    @Test
    public void test_scriptJob_destroyBeforeDone() throws Exception {

        assumeFalse(LocalFileSystemUtils.isWindows());

        JobDescription job = new JobDescription();
        job.setExecutable("/bin/bash");
        job.setArguments("/code/src/fixedClientEnvironmentTest/resources/sleepscript.sh");

        LocalInteractiveProcess p = new LocalInteractiveProcess(job, null, "job42");

        p.getStreams().getStdin().close();

        assertFalse(p.isDone());

        // Should be kill the process
        p.destroy();

        Thread.sleep(250);

        assertTrue(p.isDone());
    }

}
