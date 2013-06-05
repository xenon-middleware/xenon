package nl.esciencecenter.octopus.adaptors.local;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.jobs.Streams;

public class LocalJobsTest {

    //@Rule
   // public TemporaryFolder folder = new TemporaryFolder();

//    @Test
//    public void submitJob_WithoutStdin_NoException() throws OctopusException {
//        LocalAdaptor adaptor = mock(LocalAdaptor.class);
//        OctopusEngine octopus = mock(OctopusEngine.class);
//        Scheduler scheduler = mock(Scheduler.class);
//
//        String[][] defaults = new String[][] { { LocalAdaptor.MULTIQ_MAX_CONCURRENT, "1"}, { LocalAdaptor.MAX_HISTORY, "10"}, 
//                {LocalAdaptor.POLLING_DELAY, "5000"}};
//        
//        OctopusProperties props = new OctopusProperties(defaults);
//        LocalJobs lj = new LocalJobs(props, adaptor, octopus);
//
//        JobDescription description = new JobDescription();
//        description.setExecutable("/bin/sleep");
//        description.setArguments("30");
//        String workdir = folder.getRoot().getPath();
//        description.setWorkingDirectory(workdir);
//        description.setStderr("stderr.txt");
//        description.setStdout("stdout.txt");
//        description.setQueueName("multi");
//
//        Job job = lj.submitJob(scheduler, description);
//
//        String id = job.getUUID().toString();
//        assertFalse("Job has UUID", id.isEmpty());
//        assertEquals(scheduler, job.getScheduler());
//        assertTrue("Identifier starts with localjob-", job.getIdentifier().startsWith("localjob-"));
//
//        lj.end();
//    }

    private String readFully(InputStream in) throws IOException { 
    
        byte [] buffer = new byte[1024];
         
        int offset = 0;
       
        int tmp = in.read(buffer, 0, buffer.length-offset);
        
        while (tmp != -1) {
            
            offset += tmp;
            
            if (offset == buffer.length) { 
                buffer = Arrays.copyOf(buffer, buffer.length*2);
            }
            
            tmp = in.read(buffer, offset, buffer.length-offset);
        }
        
        in.close();
        return new String(buffer, 0, offset);
    }
    
    @org.junit.Test
    public void testInteractiveJobSubmit() throws Exception {
        
        System.err.println("START: testInteractiveJobSubmit");
        
        String message = "Hello World!";
        
        Octopus octopus = OctopusFactory.newOctopus(null);
      
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/echo");
        description.setArguments("-n", message);
        description.setInteractive(true);

        Jobs jobs = octopus.jobs();
        Scheduler scheduler = jobs.getLocalScheduler();
        
        System.err.println("Submitting interactive job!");
        
        Job job = jobs.submitJob(scheduler, description);

        System.err.println("Interactive job submitted");
        
        Streams streams = jobs.getStreams(job);
        streams.getStdin().close();
        
        String out = readFully(streams.getStdout());
        String err = readFully(streams.getStderr());

        // NOTE: Job should already be done here!
        JobStatus status = jobs.waitUntilDone(job, 5000);

        if (!status.isDone()) { 
            throw new Exception("Job exceeded dealine!");
        }
        
        if (status.hasException()) {
            throw status.getException();
        }

        assertTrue(out.equals(message));
        assertTrue(err.length() == 0);
         
        octopus.end();
    }
    
    @org.junit.Test
    public void testBatchJobSubmitWithPolling() throws Exception {
        
        System.err.println("START: testBatchJobSubmitWithPolling");
        
        String message = "Hello World!";
        
        Octopus octopus = OctopusFactory.newOctopus(null);
        Jobs jobs = octopus.jobs();
        Files files = octopus.files();
        
        FileSystem filesystem = files.newFileSystem(new URI("file:///"), null, null);
        
        AbsolutePath root = files.newPath(filesystem, new RelativePath(System.getProperty("java.io.tmpdir") + "/" + 
                UUID.randomUUID().toString()));
        
        AbsolutePath out = root.resolve(new RelativePath("stdout.txt"));
        AbsolutePath err = root.resolve(new RelativePath("stderr.txt"));
        
        files.createDirectory(root);
        
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/echo");
        description.setArguments("-n", message);
        description.setInteractive(false);
        description.setWorkingDirectory(root.getPath());
        description.setStdin(null);
        description.setStdout(out.getPath());
        description.setStderr(err.getPath());

        Scheduler scheduler = jobs.getLocalScheduler();
        Job job = jobs.submitJob(scheduler, description);
        
        long deadline = System.currentTimeMillis() + 5000;
        
        JobStatus status = jobs.getJobStatus(job);
        
        while (!status.isDone()) {
            Thread.sleep(100);
            
            long now = System.currentTimeMillis();
            
            if (now > deadline) { 
                throw new Exception("Job exceeded deadline!");
            }
            
            status = jobs.getJobStatus(job);
        }

        if (status.hasException()) {
            throw status.getException();
        }

        String tmpout = readFully(files.newInputStream(out));
        String tmperr = readFully(files.newInputStream(err));

        assertTrue(tmpout != null);
        assertTrue(tmpout.length() > 0);
        assertTrue(tmpout.equals(message));
        assertTrue(tmperr.length() == 0);
        
        files.delete(out);
        files.delete(err);
        files.delete(root);
        
        octopus.end();
    }

    @org.junit.Test
    public void testBatchJobSubmitWithWait() throws Exception {
        
        System.err.println("START: testBatchJobSubmitWithWait");
        
        String message = "Hello World!";
        
        Octopus octopus = OctopusFactory.newOctopus(null);
        Jobs jobs = octopus.jobs();
        Files files = octopus.files();

        FileSystem filesystem = files.newFileSystem(new URI("file:///"), null, null);
        
        AbsolutePath root = files.newPath(filesystem, new RelativePath(System.getProperty("java.io.tmpdir") + "/" + 
                UUID.randomUUID().toString()));
        
        AbsolutePath out = root.resolve(new RelativePath("stdout.txt"));
        AbsolutePath err = root.resolve(new RelativePath("stderr.txt"));
        
        files.createDirectory(root);

        JobDescription description = new JobDescription();
        description.setExecutable("/bin/echo");
        description.setArguments("-n", message);
        description.setInteractive(false);
        description.setWorkingDirectory(root.getPath());
        description.setStdin(null);
        description.setStdout(out.getPath());
        description.setStderr(err.getPath());

        Scheduler scheduler = jobs.getLocalScheduler();
        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 5000);
        
        if (!status.isDone()) { 
            throw new Exception("Job exceeded dealine!");
        }
        
        if (status.hasException()) {
            throw status.getException();
        }

        String tmpout = readFully(files.newInputStream(out));
        String tmperr = readFully(files.newInputStream(err));

        System.err.println("STDOUT: " + tmpout);
        System.err.println("STDERR: " + tmperr);
        
        assertTrue(tmpout != null);
        assertTrue(tmpout.length() > 0);
        assertTrue(tmpout.equals(message));
        assertTrue(tmperr.length() == 0);
        
        files.delete(out);
        files.delete(err);
        files.delete(root);
        
        octopus.end();
    }

    private void submitToQueueWithPolling(String queueName, int jobCount) throws Exception {
    
        Octopus octopus = OctopusFactory.newOctopus(null);
      
        Files files = octopus.files();

        FileSystem filesystem = files.newFileSystem(new URI("file:///"), null, null);
        
        AbsolutePath root = files.newPath(filesystem, new RelativePath(System.getProperty("java.io.tmpdir") + "/" + 
                UUID.randomUUID().toString()));        
        
        AbsolutePath [] out = new AbsolutePath[jobCount]; 
        AbsolutePath [] err = new AbsolutePath[jobCount]; 
        
        files.createDirectory(root);
        
        Jobs jobs = octopus.jobs();
        Scheduler scheduler = jobs.getLocalScheduler();
        
        Job [] j = new Job[jobCount];

        for (int i=0;i<j.length;i++) {            
            
            out[i] = root.resolve(new RelativePath("stdout" + i + ".txt"));
            err[i] = root.resolve(new RelativePath("stderr" + i + ".txt"));
            
            JobDescription description = new JobDescription();
            description.setExecutable("/bin/sleep");
            description.setArguments("1");
            description.setWorkingDirectory(root.getPath());
            description.setQueueName(queueName);
            description.setInteractive(false);
            description.setStdin(null);
            description.setStdout(out[i].getPath());
            description.setStderr(err[i].getPath());
    
            j[i] = jobs.submitJob(scheduler, description);
        }
        
        long deadline = System.currentTimeMillis() + ((jobCount * 2) * 1000);
        
        boolean done = false;
        
        while (!done) { 
            JobStatus [] status = jobs.getJobStatuses(j);

            int count = 0;
            
            for (int i=0;i<j.length;i++) { 
                if (j[i] != null) {                    
                    if (status[i].isDone()) {                         
                        if (status[i].hasException()) { 
                            System.err.println("Job " + i + " failed!");
                            throw status[i].getException();
                        }
                        
                        System.err.println("Job " + i + " done.");
                        j[i] = null;                        
                    } else {
                        count++;
                    }
                }
            }
        
            if (count == 0) { 
                done = true;
            } else { 
                Thread.sleep(100);
                
                long now = System.currentTimeMillis();
                
                if (now > deadline) { 
                    throw new Exception("Job exceeded deadline!");
                }
            }
        }
        
        for (int i=0;i<j.length;i++) {            
            
            String tmpout = readFully(files.newInputStream(out[i]));
            String tmperr = readFully(files.newInputStream(err[i]));

            assertTrue(tmpout != null);
            assertTrue(tmpout.length() == 0);
            
            assertTrue(tmperr != null);
            assertTrue(tmperr.length() == 0);
            
            files.delete(out[i]);
            files.delete(err[i]);
        }
        
        files.delete(root);
        
        octopus.end();
    }

    
    @org.junit.Test
    public void testMultiBatchJobSubmitToSingleWithPolling() throws Exception {
        System.err.println("START: testMultiBatchJobSubmitToSingleWithPolling");
        submitToQueueWithPolling("single", 10);
    }
    
    @org.junit.Test
    public void testMultiBatchJobSubmitToMultiWithPolling() throws Exception {
        System.err.println("START: testMultiBatchJobSubmitToMultiWithPolling");
        submitToQueueWithPolling("multi", 10);
    }

    @org.junit.Test
    public void testMultiBatchJobSubmitToUnlimitedWithPolling() throws Exception {
        System.err.println("START: testMultiBatchJobSubmitToMultiWithPolling");
        submitToQueueWithPolling("unlimited", 10);
    }
}
