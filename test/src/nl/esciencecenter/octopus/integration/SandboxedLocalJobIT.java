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
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.util.Sandbox;

import org.apache.commons.io.FileUtils;
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
        String workFn = tmpdir + "/" + work_id;
        AbsolutePath workdir = octopus.files().newPath(localrootfs, new RelativePath(workFn));
        octopus.files().createDirectory(workdir);

        // fill workdir
        String input_file = System.getProperty("user.dir") + "/test/fixtures/lorem_ipsum.txt";
        octopus.files().copy(octopus.files().newPath(localrootfs, new RelativePath(input_file)),
                octopus.files().newPath(localrootfs, new RelativePath(workFn+"/lorem_ipsum.txt")));

        // create sandbox
        String sandbox_id = UUID.randomUUID().toString();
        String sandboxFn = tmpdir + "/" + sandbox_id;
        AbsolutePath sandboxPath = octopus.files().newPath(localrootfs, new RelativePath(sandboxFn));
        Sandbox sandbox = new Sandbox(octopus, sandboxPath, sandbox_id);
        sandbox.addUploadFile(octopus.files().newPath(localrootfs, new RelativePath(workFn+"/lorem_ipsum.txt")), "lorem_ipsum.txt");
        sandbox.addDownloadFile("stdout.txt");
        sandbox.addDownloadFile("stderr.txt");

        // upload lorem_ipsum.txt to sandbox
        sandbox.upload();

        JobDescription description = new JobDescription();
        description.setArguments("lorem_ipsum.txt");
        description.setExecutable("/usr/bin/wc");
        description.setStdout("stdout.txt");
        description.setStderr("stderr.txt");
        description.setWorkingDirectory(sandboxPath.getPath());

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
        assertThat(FileUtils.readFileToString(stdout), is("   9  525 3581 " + input_file + "\n"));

        octopus.files().delete(workdir.resolve(new RelativePath("stdout.txt")));
        octopus.files().delete(workdir.resolve(new RelativePath("stderr.txt")));
        octopus.files().delete(workdir);
    }
}
