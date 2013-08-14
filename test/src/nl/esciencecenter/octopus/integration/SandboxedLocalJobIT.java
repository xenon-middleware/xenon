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
package nl.esciencecenter.octopus.integration;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Pathname;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.util.FileUtils;
import nl.esciencecenter.octopus.util.Sandbox;

import org.junit.Test;

public class SandboxedLocalJobIT {

    /**
     * Octopus usage example:
     * <ol>
     * <li>Create temporary work directory</li>
     * <li>Copy `test/fixtures/lorem_ipsum.txt` input file to work directory</li>
     * <li>Upload input file from work dir to sandbox.</li>
     * <li>Submit `/usr/bin/wc` of sandboxed file to local.</li>
     * <li>Poll job until isDone</li>
     * <li>Download stdout from sandbox</li>
     * <li>Verify stdout</li>
     * <li>Clean up sandbox</li>
     * 
     * </ol>
     * 
     * @throws OctopusException
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void test() throws Exception, OctopusException, URISyntaxException, InterruptedException, IOException {
        Octopus octopus = OctopusFactory.newOctopus(null);
        Credential credential = null;
        String tmpdir = System.getProperty("java.io.tmpdir");
        String work_id = UUID.randomUUID().toString();
        FileSystem localrootfs = octopus.files().newFileSystem(new URI("file:///"), credential, null);

        // create workdir
        String workFn = tmpdir + "/AAP" + work_id;
        Path workdir = octopus.files().newPath(localrootfs, new Pathname(workFn));
        octopus.files().createDirectory(workdir);

        // fill workdir
        String input_file = System.getProperty("user.dir") + "/test/fixtures/lorem_ipsum.txt";
        octopus.files().copy(octopus.files().newPath(localrootfs, new Pathname(input_file)),
                octopus.files().newPath(localrootfs, new Pathname(workFn + "/lorem_ipsum.txt")));

        // create sandbox
        String sandbox_id = "MIES" + UUID.randomUUID().toString();
        Path sandboxPath = octopus.files().newPath(localrootfs, new Pathname(tmpdir));
        Sandbox sandbox = new Sandbox(octopus.files(), sandboxPath, sandbox_id);

        sandbox.addUploadFile(octopus.files().newPath(localrootfs, new Pathname(workFn + "/lorem_ipsum.txt")),
                "lorem_ipsum.txt");

        sandbox.addDownloadFile("stdout.txt", octopus.files().newPath(localrootfs, new Pathname(workFn + "/stdout.txt")));
        sandbox.addDownloadFile("stderr.txt", octopus.files().newPath(localrootfs, new Pathname(workFn + "/stderr.txt")));

        // upload lorem_ipsum.txt to sandbox
        sandbox.upload();

        JobDescription description = new JobDescription();
        description.setArguments("lorem_ipsum.txt");
        description.setExecutable("/usr/bin/wc");
        description.setQueueName("single");
        description.setStdout("stdout.txt");
        description.setStderr("stderr.txt");
        description.setWorkingDirectory(sandbox.getPath().getPathname().getPath());

        URI sh_location = new URI("local:///");
        Scheduler scheduler = octopus.jobs().newScheduler(sh_location, null, null);

        Job job = octopus.jobs().submitJob(scheduler, description);

        // TODO add timeout
        while (!octopus.jobs().getJobStatus(job).isDone()) {
            Thread.sleep(500);
        }

        sandbox.download();
        sandbox.delete();

        JobStatus status = octopus.jobs().getJobStatus(job);
        if (status.hasException()) {
            throw status.getException();
        }

        File stdout = new File(workdir.getPath() + "/stdout.txt");
        assertThat(org.apache.commons.io.FileUtils.readFileToString(stdout), is("   9  525 3581 lorem_ipsum.txt\n"));

        FileUtils.recursiveDelete(octopus.files(), workdir);
    }
}
