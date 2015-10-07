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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.UUID;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;
import nl.esciencecenter.xenon.util.Sandbox;
import nl.esciencecenter.xenon.util.Utils;

import org.junit.Test;

public class SandboxedLocalJobIT {

    /**
     * Xenon usage example:
     * <ol>
     * <li>Create temporary work directory</li>
     * <li>Copy `src/test/resources/fixtures/lorem_ipsum.txt` input file to work directory</li>
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
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void test() throws Exception, XenonException, URISyntaxException, InterruptedException, IOException {
        Xenon xenon = XenonFactory.newXenon(null);
        String tmpdir = System.getProperty("java.io.tmpdir");
        String work_id = UUID.randomUUID().toString();
        FileSystem localrootfs = Utils.getLocalCWD(xenon.files()).getFileSystem();

        // create workdir
        String workFn = tmpdir + "/AAP" + work_id;
        Path workdir = xenon.files().newPath(localrootfs, new RelativePath(workFn));
        xenon.files().createDirectory(workdir);

        // fill workdir
        String input_file = System.getProperty("user.dir") + "/src/test/resources/fixtures/lorem_ipsum.txt";
        xenon.files().copy(xenon.files().newPath(localrootfs, new RelativePath(input_file)),
                xenon.files().newPath(localrootfs, new RelativePath(workFn + "/lorem_ipsum.txt")));

        // create sandbox
        String sandbox_id = "MIES" + UUID.randomUUID().toString();
        Path sandboxPath = xenon.files().newPath(localrootfs, new RelativePath(tmpdir));
        Sandbox sandbox = new Sandbox(xenon.files(), sandboxPath, sandbox_id);

        sandbox.addUploadFile(xenon.files().newPath(localrootfs, new RelativePath(workFn + "/lorem_ipsum.txt")),
                "lorem_ipsum.txt");

        sandbox.addDownloadFile("stdout.txt", xenon.files().newPath(localrootfs, new RelativePath(workFn + "/stdout.txt")));
        sandbox.addDownloadFile("stderr.txt", xenon.files().newPath(localrootfs, new RelativePath(workFn + "/stderr.txt")));

        // upload lorem_ipsum.txt to sandbox
        sandbox.upload();

        JobDescription description = new JobDescription();
        description.setArguments("lorem_ipsum.txt");
        description.setExecutable("/usr/bin/wc");
        description.setQueueName("single");
        description.setStdout("stdout.txt");
        description.setStderr("stderr.txt");
        description.setWorkingDirectory(sandbox.getPath().getRelativePath().getAbsolutePath());

        Scheduler scheduler = xenon.jobs().newScheduler("local", "", null, null);

        Job job = xenon.jobs().submitJob(scheduler, description);

        // TODO add timeout
        while (!xenon.jobs().getJobStatus(job).isDone()) {
            Thread.sleep(500);
        }

        sandbox.download();
        sandbox.delete();

        JobStatus status = xenon.jobs().getJobStatus(job);
        if (status.hasException()) {
            throw status.getException();
        }

        Path stdout = xenon.files().newPath(workdir.getFileSystem(), workdir.getRelativePath().resolve("stdout.txt"));
        
        assertThat(Utils.readToString(xenon.files(), stdout, Charset.defaultCharset()), is("   9  525 3581 lorem_ipsum.txt\n"));

        Utils.recursiveDelete(xenon.files(), workdir);
    }
}
