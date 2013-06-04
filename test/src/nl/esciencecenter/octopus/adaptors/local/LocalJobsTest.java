package nl.esciencecenter.octopus.adaptors.local;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
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

    private void writeFully(OutputStream in, String text) throws IOException { 
        in.write(text.getBytes());
        in.close();
    } 
    
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
        long deadline = System.currentTimeMillis() + 5000;
        
        while (!jobs.getJobStatus(job).isDone()) {
            Thread.sleep(100);
            
            long now = System.currentTimeMillis();
            
            if (now > deadline) { 
                throw new Exception("Job exceeded deadline!");
            }
        }

        JobStatus status = octopus.jobs().getJobStatus(job);
        
        if (status.hasException()) {
            throw status.getException();
        }

        assertTrue(out.equals(message));
        assertTrue(err.length() == 0);
         
        octopus.end();
    }
    
    @org.junit.Test
    public void testBatchJobSubmitWithPolling() throws Exception {
        
        String message = "Hello World!";
        
        Octopus octopus = OctopusFactory.newOctopus(null);
      
        final String tmpDir = System.getProperty("java.io.tmpdir");
        
        System.out.println("tmpdir = " + tmpDir);
        
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/echo");
        description.setArguments("-n", message);
        description.setInteractive(false);
        description.setWorkingDirectory(tmpDir);
        description.setStdin(null);
        description.setStdout("stdout.txt");
        description.setStderr("stderr.txt");

        Jobs jobs = octopus.jobs();
        Scheduler scheduler = jobs.getLocalScheduler();
        Job job = jobs.submitJob(scheduler, description);
        
        long deadline = System.currentTimeMillis() + 5000;
        
        while (!jobs.getJobStatus(job).isDone()) {
            Thread.sleep(100);
            
            long now = System.currentTimeMillis();
            
            if (now > deadline) { 
                throw new Exception("Job exceeded deadline!");
            }
        }

        JobStatus status = jobs.getJobStatus(job);
        
        if (status.hasException()) {
            throw status.getException();
        }

        String out = readFully(new FileInputStream(new File(tmpDir + File.separator + "stdout.txt")));
        String err = readFully(new FileInputStream(new File(tmpDir + File.separator + "stderr.txt")));

        System.out.println("stdout = \"" + out + "\"");
        System.out.println("stderr = \"" + err + "\"");
        
        assertTrue(out != null);
        assertTrue(out.length() > 0);
        assertTrue(out.equals(message));
        assertTrue(err.length() == 0);
        
        octopus.end();
    }

    @org.junit.Test
    public void testBatchJobSubmitWithWait() throws Exception {
        
        String message = "Hello World!";
        
        Octopus octopus = OctopusFactory.newOctopus(null);
      
        final String tmpDir = System.getProperty("java.io.tmpdir");
        
        System.out.println("tmpdir = " + tmpDir);
        
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/echo");
        description.setArguments("-n", message);
        description.setInteractive(false);
        description.setWorkingDirectory(tmpDir);
        description.setStdin(null);
        description.setStdout("stdout.txt");
        description.setStderr("stderr.txt");

        Jobs jobs = octopus.jobs();
        Scheduler scheduler = jobs.getLocalScheduler();
        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 5000);
        
        if (status.hasException()) {
            throw status.getException();
        }

        String out = readFully(new FileInputStream(new File(tmpDir + File.separator + "stdout.txt")));
        String err = readFully(new FileInputStream(new File(tmpDir + File.separator + "stderr.txt")));

        System.out.println("stdout = \"" + out + "\"");
        System.out.println("stderr = \"" + err + "\"");
        
        assertTrue(out != null);
        assertTrue(out.length() > 0);
        assertTrue(out.equals(message));
        assertTrue(err.length() == 0);
        
        octopus.end();
    }
}
