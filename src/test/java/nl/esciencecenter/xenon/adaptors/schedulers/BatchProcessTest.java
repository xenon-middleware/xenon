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
