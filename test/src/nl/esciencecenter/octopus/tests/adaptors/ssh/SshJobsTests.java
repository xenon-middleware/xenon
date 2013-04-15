package nl.esciencecenter.octopus.tests.adaptors.ssh;

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
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

import org.apache.commons.io.FileUtils;

public class SshJobsTests {

    @org.junit.Test
    public void testJobSubmit() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);
        URI fs_location = new URI("file:///");
        FileSystem filesystem = octopus.files().newFileSystem(fs_location, null, null);
        RelativePath location = new RelativePath(System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID().toString());
        AbsolutePath root = octopus.files().newPath(filesystem, location);
        octopus.files().createDirectory(root);
        String input_file = System.getProperty("user.dir") + "/test/fixtures/lorem_ipsum.txt";

        JobDescription description = new JobDescription();
        description.setArguments(input_file);
        description.setExecutable("/usr/bin/wc");
        description.setStdin(null);
        description.setStdout(root.resolve(new RelativePath("stdout.txt")).getPath());
        description.setStderr(root.resolve(new RelativePath("stderr.txt")).getPath());

        String username = System.getProperty("user.name");
        URI sh_location = new URI("ssh://" + username + "@localhost");
        Credentials c = octopus.credentials();
        Credential credential =
                c.newCertificateCredential("ssh", null, "/home/" + username + "/.ssh/id_rsa", "/home/" + username
                        + "/.ssh/id_rsa.pub", username, "");
        Scheduler scheduler = octopus.jobs().newScheduler(sh_location, credential, null);

        Job job = octopus.jobs().submitJob(scheduler, description);

        // TODO add timeout
        while (!octopus.jobs().getJobStatus(job).isDone()) {
            Thread.sleep(500);
        }

        JobStatus status = octopus.jobs().getJobStatus(job);
        if (status.hasException()) {
            throw status.getException();
        }

        File stdout = new File("/tmp/stdout.txt");
        assertThat(FileUtils.readFileToString(stdout), is("   9  525 3581 " + input_file + "\n"));

        octopus.files().delete(root.resolve(new RelativePath("stdout.txt")));
        octopus.files().delete(root.resolve(new RelativePath("stderr.txt")));
        octopus.files().delete(root);
    }
}
