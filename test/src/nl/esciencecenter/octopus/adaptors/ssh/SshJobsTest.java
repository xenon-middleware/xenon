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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URI;
import java.util.UUID;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.Scheduler;

import org.apache.commons.io.FileUtils;

public class SshJobsTest {

    @org.junit.Test
    public void testJobSubmit() throws Exception {
        
        // Initialize Octopus and retrieve the various APIs
        Octopus octopus = OctopusFactory.newOctopus(null);
        Files files = octopus.files();
        Jobs jobs = octopus.jobs();
        Credentials c = octopus.credentials();
        
        // Create a temporary directory
        FileSystem filesystem = files.newFileSystem(new URI("file:///"), null, null);
        
        AbsolutePath root = files.newPath(filesystem, new RelativePath(System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID().toString()));        
        AbsolutePath out = root.resolve(new RelativePath("stdout.txt"));
        AbsolutePath err = root.resolve(new RelativePath("stderr.txt"));
        
        files.createDirectory(root);
        
        // Create a JobDescription
        String inputFile = System.getProperty("user.dir") + "/test/fixtures/lorem_ipsum.txt";
        
        JobDescription description = new JobDescription();
        description.setExecutable("/usr/bin/wc");
        description.setArguments(inputFile);
        description.setStdin(null);
        description.setStdout(out.getPath());
        description.setStderr(err.getPath());

        // Create a scheduler
        Credential credential = c.getDefaultCredential("ssh");
        String username = System.getProperty("user.name");
        
        Scheduler scheduler = jobs.newScheduler(new URI("ssh://" + username + "@localhost"), credential, null);

        // Submit the Job and wait for it to finish.
        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 10000);
        
        // Check the state and output.
        if (!status.isDone()) { 
            throw new Exception("Job exceeded deadline!");
        }
        
        if (status.hasException()) {
            throw status.getException();
        }

        assertThat(FileUtils.readFileToString(new File(out.getPath())), is("   9  525 3581 " + inputFile + "\n"));

        octopus.files().delete(out);
        octopus.files().delete(err);
        octopus.files().delete(root);
        octopus.end();
    }
}
