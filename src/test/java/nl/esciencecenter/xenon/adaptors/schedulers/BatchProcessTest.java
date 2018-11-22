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
package nl.esciencecenter.xenon.adaptors.schedulers;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.filesystems.MockFileSystem;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.schedulers.JobDescription;

public class BatchProcessTest {

    @Test
    public void test_jobIDSubstitution() throws XenonException, IOException {

        Path workdir = new Path("/");
        MockFileSystem fs = new MockFileSystem("FS", "0", "file://", workdir, null);
        MockScheduler s = new MockScheduler(false, "", 0);
        MockInteractiveProcessFactory f = new MockInteractiveProcessFactory();

        JobDescription job = new JobDescription();
        job.setExecutable("test");
        job.setStdout("out.%j");
        job.setStderr("err.%j");

        BatchProcess b = new BatchProcess(fs, workdir, job, "ID0", f, 0);

        assertTrue(fs.exists(new Path("/out.ID0")));
        assertTrue(fs.exists(new Path("/err.ID0")));
    }
}
