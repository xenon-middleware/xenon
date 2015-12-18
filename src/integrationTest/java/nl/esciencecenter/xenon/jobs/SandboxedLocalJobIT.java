/**
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.UUID;

import nl.esciencecenter.xenon.JobException;
import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;
import nl.esciencecenter.xenon.util.Sandbox;
import nl.esciencecenter.xenon.util.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SandboxedLocalJobIT {
    private Xenon xenon;
    private Scheduler scheduler;
    private Files files;

    @Before
    public void setupXenon() throws XenonException {
        xenon = XenonFactory.newXenon(null);
        files = xenon.files();
        scheduler = xenon.jobs().newScheduler("local", "", null, null);
    }

    @After
    public void cleanupXenon() throws XenonException {
        xenon.jobs().close(scheduler);
        XenonFactory.endAll();
    }

    /**
     * Xenon usage example:
     * <ol>
     * <li>Create temporary work directory</li>
     * <li>Copy `/fixtures/lorem_ipsum.txt` input file to work directory</li>
     * <li>Upload input file from work dir to sandbox.</li>
     * <li>Submit `/usr/bin/wc` of sandboxed file to local.</li>
     * <li>Poll job until isDone</li>
     * <li>Download stdout from sandbox</li>
     * <li>Verify stdout</li>
     * <li>Clean up sandbox</li>
     * 
     * </ol>
     * 
     * @throws XenonException
     */
    @Test
    public void test() throws XenonException {
        // test uses unix utilities
        if (Utils.isWindows()) {
            return;
        }
        String tmpdir = System.getProperty("java.io.tmpdir");
        String work_id = UUID.randomUUID().toString();
        FileSystem localrootfs = Utils.getLocalCWD(files).getFileSystem();

        // create workdir
        String workFn = tmpdir + "/AAP" + work_id;
        Path workdir = files.newPath(localrootfs, new RelativePath(workFn));

        // create sandbox
        String sandbox_id = "MIES" + UUID.randomUUID().toString();
        Path sandboxPath = files.newPath(localrootfs, new RelativePath(tmpdir));
        Sandbox sandbox = new Sandbox(files, sandboxPath, sandbox_id);

        try {
            files.createDirectory(workdir);

            // fill workdir
            URL inputURL = SandboxedLocalJobIT.class.getResource("/fixtures/lorem_ipsum.txt");
            files.copy(files.newPath(localrootfs, new RelativePath(inputURL.getPath())),
                    files.newPath(localrootfs, new RelativePath(workFn + "/lorem_ipsum.txt")));

            sandbox.addUploadFile(files.newPath(localrootfs, new RelativePath(workFn + "/lorem_ipsum.txt")),
                    "lorem_ipsum.txt");

            sandbox.addDownloadFile("stdout.txt", files.newPath(localrootfs, new RelativePath(workFn + "/stdout.txt")));
            sandbox.addDownloadFile("stderr.txt", files.newPath(localrootfs, new RelativePath(workFn + "/stderr.txt")));

            // upload lorem_ipsum.txt to sandbox
            sandbox.upload();

            JobDescription description = new JobDescription();
            description.setArguments("lorem_ipsum.txt");
            description.setExecutable("/usr/bin/wc");
            description.setQueueName("single");
            description.setStdout("stdout.txt");
            description.setStderr("stderr.txt");
            description.setWorkingDirectory(sandbox.getPath().getRelativePath().getAbsolutePath());

            Job job = xenon.jobs().submitJob(scheduler, description);
            JobStatus status = xenon.jobs().waitUntilDone(job, 1000);

            sandbox.download();

            if (status.hasException()) {
                throw new JobException("Job failed", status.getException());
            }

            Path stdout = files.newPath(workdir.getFileSystem(), workdir.getRelativePath().resolve("stdout.txt"));

            assertThat(Utils.readToString(files, stdout, Charset.defaultCharset()), is("   9  525 3581 lorem_ipsum.txt\n"));
        } finally {
            try {
                sandbox.delete();
            } finally {
                Utils.recursiveDelete(files, workdir);
            }
        }
    }
}
