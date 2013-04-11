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
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class SandboxedLocalJobIT {

    /**
     * Octopus usage example:
     * <ol>
     * <li>Add `test/fixtures/lorem_ipsum.txt` to sandbox.</li>
     * <li>Submit `/usr/bin/wc` of sandboxed file to local.</li>
     * <li>Poll job until isDone</li>
     * <li>Verify stdout</li>
     * <li>Clean up sandbox</li>
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
        URI fs_location = new URI("file:///");
        FileSystem filesystem = octopus.files().newFileSystem(fs_location, null, null);
        RelativePath location = new RelativePath(System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID().toString());
        AbsolutePath root = octopus.files().newPath(filesystem, location);
        octopus.files().createDirectory(root);
        String input_file = System.getProperty("user.dir") + "/test/fixtures/lorem_ipsum.txt";
        // TODO: use sandbox when done
//        Sandbox sandbox = new Sandbox(octopus, root, "mysandbox");

        JobDescription description = new JobDescription();
        description.setArguments(input_file);
        description.setExecutable("/usr/bin/wc");
        description.setStdin(null);
        description.setStdout("stdout.txt");
        description.setStderr("stderr.txt");
        description.setWorkingDirectory(location.getPath());

        URI sh_location = new URI("local:///");
        Scheduler scheduler = octopus.jobs().newScheduler(sh_location, null, null);

        Job job = octopus.jobs().submitJob(scheduler, description);

        // TODO add timeout
        while (!octopus.jobs().getJobStatus(job).isDone()) {
            Thread.sleep(500);
        }

        JobStatus status = octopus.jobs().getJobStatus(job);
        if (status.hasException()) {
            throw status.getException();
        }

        // TODO copy file from sandbox to somewhere

        File stdout = new File(root.getPath()+"/stdout.txt");
        assertThat(FileUtils.readFileToString(stdout), is("   9  525 3581 " + input_file + "\n"));

//        sandbox.delete();
        octopus.files().delete(root.resolve(new RelativePath("stdout.txt")));
        octopus.files().delete(root.resolve(new RelativePath("stderr.txt")));
        octopus.files().delete(root);
    }
}
