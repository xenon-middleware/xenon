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
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.io.File;

import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.shared.local.LocalUtil;

public class CommandRunnerTest {

    @Test(expected=IllegalArgumentException.class)
    public void test_withoutCommand_throwsException() throws XenonException {
        new CommandRunner();
    }

    @Test(expected=IllegalArgumentException.class)
    public void test_emptyCommand_throwsException() throws XenonException {
        new CommandRunner(new String[0]);
    }

    @Test
    public void test_runCommand() throws XenonException {

        assumeFalse(LocalUtil.isWindows());

        File workingDir = new File("/tmp");

        CommandRunner r = new CommandRunner("Hello World\n", workingDir, "/bin/cat");

        assertEquals(0, r.getExitCode());
        assertEquals("Hello World\n", r.getStdout());
        assertTrue(r.getStderr().isEmpty());

    }

    @Test
    public void test_runCommandInPath() throws XenonException {

        assumeFalse(LocalUtil.isWindows());

        File workingDir = new File("/tmp");

        CommandRunner r = new CommandRunner("Hello World\n", workingDir, "cat");

        assertEquals(0, r.getExitCode());
        assertEquals("Hello World\n", r.getStdout());
        assertTrue(r.getStderr().isEmpty());
    }

    @Test
    public void test_runCommandWithoutWorkingDir() throws XenonException {

        assumeFalse(LocalUtil.isWindows());

        File workingDir = null;

        CommandRunner r = new CommandRunner("Hello World\n", workingDir, "/bin/cat");

        assertEquals(0, r.getExitCode());
        assertEquals("Hello World\n", r.getStdout());
        assertTrue(r.getStderr().isEmpty());

    }

    @Test(expected=CommandNotFoundException.class)
    public void test_runCommand_nonExistantWorkingDir() throws XenonException {

        assumeFalse(LocalUtil.isWindows());

        File workingDir = new File("/foo");

        new CommandRunner("Hello World\n", workingDir, "/bin/cat");
    }



}



