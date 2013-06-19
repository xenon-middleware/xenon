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

package nl.esciencecenter.octopus.adaptors;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.exceptions.InvalidCredentialsException;
import nl.esciencecenter.octopus.exceptions.InvalidLocationException;
import nl.esciencecenter.octopus.exceptions.InvalidPropertyException;
import nl.esciencecenter.octopus.exceptions.NoSuchQueueException;
import nl.esciencecenter.octopus.exceptions.NoSuchSchedulerException;
import nl.esciencecenter.octopus.exceptions.UnknownPropertyException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.OpenOption;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Jobs;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.jobs.Streams;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public abstract class AbstractJobTest {
    
    private static final String TEST_DIR = "octopus_test_" + System.currentTimeMillis();
    
    protected Octopus octopus;
    protected Files files;
    protected Jobs jobs;
    protected AbsolutePath testDir;
    
    @BeforeClass
    public static void prepareClass() throws OctopusIOException, OctopusException { 

        Octopus octopus = OctopusFactory.newOctopus(null);
        
        Files files = octopus.files();
        FileSystem fs = files.getLocalCWDFileSystem();
        AbsolutePath root = fs.getEntryPath();
        AbsolutePath testDir = root.resolve(new RelativePath(TEST_DIR));
        files.createDirectory(testDir);
        
        OctopusFactory.endOctopus(octopus);
    }

    @AfterClass
    public static void cleanupClass() throws OctopusException, OctopusIOException { 
        
        Octopus octopus = OctopusFactory.newOctopus(null);
        
        Files files = octopus.files();
        FileSystem fs = files.getLocalCWDFileSystem();
        AbsolutePath root = fs.getEntryPath();
        AbsolutePath testDir = root.resolve(new RelativePath(TEST_DIR));
    
        if (files.exists(testDir)) { 
            files.delete(testDir);
        }
        
        OctopusFactory.endOctopus(octopus);
    }
    
    @Before
    public void prepare() throws OctopusException { 
        octopus = OctopusFactory.newOctopus(null);
        files = octopus.files();
        jobs = octopus.jobs();
    }
    
    @After
    public void cleanup() throws OctopusException { 
        OctopusFactory.endOctopus(octopus);
    }
    
    public abstract URI getValidURI() throws Exception;
    public abstract URI getInvalidLocationURI() throws Exception;
    public abstract URI getInvalidPathURI() throws Exception;
    
    public abstract boolean supportsCredentials() throws Exception;
    public abstract Credential getDefaultCredential() throws Exception;
    public abstract Credential getInvalidCredential() throws Exception ;
    
    public abstract boolean supportsProperties() throws Exception;
    public abstract Properties getDefaultProperties() throws Exception;
    public abstract Properties getUnknownProperties() throws Exception;
    public abstract Properties getInvalidProperties() throws Exception;
    
    public abstract boolean supportsClose() throws Exception;
    
    public abstract Scheduler getDefaultScheduler() throws Exception;
    public abstract FileSystem getDefaultFileSystem() throws Exception;
    
    public abstract String getInvalidQueueName() throws Exception;
    
    
    // TEST: newScheduler
    //
    // location: null / valid URI / invalid URI 
    // credential: null / default / set / wrong
    // properties: null / empty / set / wrong
    
    
    @Test(expected = NullPointerException.class)
    public void test00_newScheduler() throws Exception { 
        jobs.newScheduler(null, null, null);
    } 
    
    @Test
    public void test01_newScheduler() throws Exception { 
        jobs.newScheduler(getValidURI(), null, null);
    }
    
    @Test(expected = InvalidLocationException.class)
    public void test02a_newScheduler() throws Exception { 
        jobs.newScheduler(getInvalidLocationURI(), null, null);
    }

    @Test(expected = InvalidLocationException.class)
    public void test02b_newScheduler() throws Exception { 
        jobs.newScheduler(getInvalidPathURI(), null, null);
    }
    
    @Test
    public void test03_newScheduler() throws Exception {
        jobs.newScheduler(getValidURI(), getDefaultCredential(), null);
    }

    @Test
    public void test04a_newScheduler() throws Exception {
        if (supportsCredentials()) { 
            try {
                jobs.newScheduler(getValidURI(), getInvalidCredential(), null);
                throw new Exception("newScheduler did NOT throw InvalidCredentialsException");
            } catch (InvalidCredentialsException e) {
                // expected
            }
        }
    }

    @Test
    public void test04b_newScheduler() throws Exception {
        if (!supportsCredentials()) { 
            try {
                Credential c = new Credential() { 
                    @Override
                    public Properties getProperties() {
                        return null;
                    }
                    
                    @Override
                    public String getAdaptorName() {
                        return "local";
                    }
                };
                
                jobs.newScheduler(getValidURI(), c, null);
                
                throw new Exception("newScheduler did NOT throw OctopusException");
            } catch (OctopusException e) {
                // expected
            }
        }
    }

    @Test
    public void test05_newScheduler() throws Exception {
        jobs.newScheduler(getValidURI(), getDefaultCredential(), new Properties());
    }

    @Test
    public void test06_newScheduler() throws Exception {
        jobs.newScheduler(getValidURI(), getDefaultCredential(), getDefaultProperties());
    }
    
    @Test
    public void test07_newScheduler() throws Exception {
        if (supportsProperties()) { 
            try { 
                jobs.newScheduler(getValidURI(), getDefaultCredential(), getInvalidProperties());
                throw new Exception("newScheduler did NOT throw InvalidPropertyException");
            } catch (InvalidPropertyException e) {
                // expected
            }
        }
    }

    @Test
    public void test08_newScheduler() throws Exception {
        if (supportsProperties()) { 
            try { 
                jobs.newScheduler(getValidURI(), getDefaultCredential(), getUnknownProperties());
                throw new Exception("newScheduler did NOT throw UnknownPropertyException");
            } catch (UnknownPropertyException e) {
                // expected
            }
        }
    }

    @Test
    public void test09_newScheduler() throws Exception {
        if (!supportsProperties()) { 
            try { 
                Properties p = new Properties();
                p.put("aap", "noot");
                jobs.newScheduler(getValidURI(), getDefaultCredential(), p);
                throw new Exception("newScheduler did NOT throw OctopusException");
            } catch (OctopusException e) {
                // expected
            }
        }
    }
    
    @Test
    public void test10_getLocalScheduler() throws Exception {
        
        Scheduler s = jobs.getLocalScheduler();
        
        assertTrue(s != null);
        assertTrue(s.getAdaptorName().equals("local"));
    } 
     
    @Test
    public void test11_open_close() throws Exception {
        if (supportsClose()) { 
            Scheduler s = getDefaultScheduler(); 
             
            assertTrue(jobs.isOpen(s));
            
            jobs.close(s);
            
            assertFalse(jobs.isOpen(s));
        }
    }
    
    @Test
    public void test12_open_close() throws Exception {
        if (!supportsClose()) { 
            Scheduler s = getDefaultScheduler(); 
             
            assertTrue(jobs.isOpen(s));
            
            jobs.close(s);
            
            assertTrue(jobs.isOpen(s));
        }
    }

    @Test
    public void test13_open_close() throws Exception {
        if (supportsClose()) { 
            Scheduler s = getDefaultScheduler(); 
            jobs.close(s);
            
            try { 
                jobs.close(s);
                throw new Exception("close did NOT throw NoSuchSchedulerException");
            } catch (NoSuchSchedulerException e) { 
                // expected
            }
        }
    }
 
    @Test
    public void test14a_getJobs() throws Exception {
        Scheduler s = getDefaultScheduler();        
        jobs.getJobs(s, s.getQueueNames());
        jobs.close(s);
    }
    
    @Test
    public void test14b_getJobs() throws Exception {
        Scheduler s = getDefaultScheduler();        
        jobs.getJobs(s);
        jobs.close(s);
    }
    
    @Test
    public void test15_getJobs() throws Exception {
        Scheduler s = getDefaultScheduler();        

        try {
            jobs.getJobs(s, getInvalidQueueName());
            throw new Exception("close did NOT throw NoSuchQueueException");
        } catch (NoSuchQueueException e) { 
            // expected
        } finally { 
            jobs.close(s);
        }
    }

    @Test
    public void test16_getJobs() throws Exception {
        
        if (supportsClose()) { 
        
            Scheduler s = getDefaultScheduler();        

            jobs.close(s);
            
            try {
                jobs.getJobs(s, s.getQueueNames());
                throw new Exception("close did NOT throw NoSuchSchedulerException");
            } catch (NoSuchSchedulerException e) { 
                // expected
            }
        }
    }
    
    
    @Test
    public void test17_getQueueStatus() throws Exception {        
        Scheduler s = getDefaultScheduler();        
        jobs.getQueueStatus(s, s.getQueueNames()[0]);
        jobs.close(s);
    }
    
    @Test(expected = NoSuchQueueException.class)
    public void test18a_getQueueStatus() throws Exception {        
        Scheduler s = getDefaultScheduler();
        try { 
            jobs.getQueueStatus(s, getInvalidQueueName());
        } finally { 
            jobs.close(s);
        }
    }
    
    @Test
    public void test18b_getQueueStatus() throws Exception {        
        Scheduler s = getDefaultScheduler();
        try { 
            jobs.getQueueStatus(s, null);
        } finally { 
            jobs.close(s);
        }
    }

    
    
    @Test(expected = NullPointerException.class)
    public void test19_getQueueStatus() throws Exception {        
        jobs.getQueueStatus(null, null);        
    }

    @Test
    public void test20_getQueueStatus() throws Exception {
        
        if (supportsClose()) { 
        
            Scheduler s = getDefaultScheduler();
            jobs.close(s);
            
            try { 
                jobs.getQueueStatus(s, s.getQueueNames()[0]);
                throw new Exception("getQueueStatus did NOT throw NoSuchSchedulerException");
            } catch (NoSuchSchedulerException e) { 
                // expected
            }
        }
    }

    
    @Test
    public void test21a_getQueueStatuses() throws Exception {        
        Scheduler s = getDefaultScheduler();        
        QueueStatus [] tmp = jobs.getQueueStatuses(s, s.getQueueNames());
        jobs.close(s);

        String [] names = s.getQueueNames();

        assertTrue(tmp != null);
        assertTrue(tmp.length == names.length);
        
        for (int i=0;i<tmp.length;i++) { 
            assertTrue(tmp[i].getQueueName().equals(names[i]));
        }
    }
    
    @Test
    public void test21b_getQueueStatuses() throws Exception {        
        Scheduler s = getDefaultScheduler();        
        QueueStatus [] tmp = jobs.getQueueStatuses(s);
        jobs.close(s);
        
        String [] names = s.getQueueNames();

        assertTrue(tmp != null);
        assertTrue(tmp.length == names.length);
        
        for (int i=0;i<tmp.length;i++) { 
            assertTrue(tmp[i].getQueueName().equals(names[i]));
        }

    }
    
    @Test
    public void test22_getQueueStatuses() throws Exception {        
        Scheduler s = getDefaultScheduler();        
        try { 
            QueueStatus [] tmp = jobs.getQueueStatuses(s, getInvalidQueueName());

            assertTrue(tmp != null);
            assertTrue(tmp.length == 1);
            assertTrue(tmp[0].hasException());
            assertTrue(tmp[0].getException() instanceof NoSuchQueueException);

        } finally { 
            jobs.close(s);
        }
    }
    
    
    @Test(expected = NullPointerException.class)
    public void test23_getQueueStatuses() throws Exception {        
        jobs.getQueueStatuses(null);
    }
    
    @Test
    public void test24_getQueueStatuses() throws Exception {
        
        if (supportsClose()) { 
            Scheduler s = getDefaultScheduler();
            jobs.close(s);
            
            try { 
                jobs.getQueueStatuses(s, s.getQueueNames());
                throw new Exception("getQueueStatuses did NOT throw NoSuchSchedulerException");
            } catch (NoSuchSchedulerException e) { 
                // expected
            }
        }
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

    private void writeFully(OutputStream out, String message) throws IOException { 
        out.write(message.getBytes());
        out.close();
    }

    
    @org.junit.Test
    public void test30_interactiveJobSubmit() throws Exception {
        
        String message = "Hello World!";
        
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/echo");
        description.setArguments("-n", message);
        description.setInteractive(true);

        Scheduler scheduler = getDefaultScheduler();
        
        System.err.println("Submitting interactive job to " + scheduler.getUri());
        
        Job job = jobs.submitJob(scheduler, description);

        System.err.println("Interactive job submitted to " + scheduler.getUri());
        
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
    public void test31_batchJobSubmitWithPolling() throws Exception {
        
        String message = "Hello World!";
        String workingDir = "octopus_test_" + System.currentTimeMillis();
        
        FileSystem filesystem = getDefaultFileSystem();
        Scheduler scheduler = getDefaultScheduler();
        
        AbsolutePath root = filesystem.getEntryPath().resolve(new RelativePath(workingDir));
        files.createDirectory(root);
        
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/echo");
        description.setArguments("-n", message);
        description.setInteractive(false);
        description.setWorkingDirectory(workingDir);
        description.setStdin(null);

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
        
        AbsolutePath out = root.resolve(new RelativePath(description.getStdout()));
        AbsolutePath err = root.resolve(new RelativePath(description.getStderr()));

        String tmpout = readFully(files.newInputStream(out));
        String tmperr = readFully(files.newInputStream(err));

        files.delete(out);
        files.delete(err);
        files.delete(root);

        System.err.println("STDOUT: " + tmpout);
        System.err.println("STDERR: " + tmperr);
        
        assertTrue(tmpout != null);
        assertTrue(tmpout.length() > 0);
        assertTrue(tmpout.equals(message));
        assertTrue(tmperr.length() == 0);
    }

    @org.junit.Test
    public void test32_batchJobSubmitWithWait() throws Exception {
        
        String message = "Hello World!";
        String workingDir = "octopus_test_" + System.currentTimeMillis();
        
        FileSystem filesystem = getDefaultFileSystem();
        Scheduler scheduler = getDefaultScheduler();
        
        AbsolutePath root = filesystem.getEntryPath().resolve(new RelativePath(workingDir));
        files.createDirectory(root);
        
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/echo");
        description.setArguments("-n", message);
        description.setInteractive(false);
        description.setWorkingDirectory(workingDir);
        description.setStdin(null);
        
        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 5000);
        
        if (!status.isDone()) { 
            throw new Exception("Job exceeded deadline!");
        }
        
        if (status.hasException()) {
            throw status.getException();
        }
       
        AbsolutePath out = root.resolve(new RelativePath(description.getStdout()));
        AbsolutePath err = root.resolve(new RelativePath(description.getStderr()));

        String tmpout = readFully(files.newInputStream(out));
        String tmperr = readFully(files.newInputStream(err));

        files.delete(out);
        files.delete(err);
        files.delete(root);
        
        System.err.println("STDOUT: " + tmpout);
        System.err.println("STDERR: " + tmperr);
        
        assertTrue(tmpout != null);
        assertTrue(tmpout.length() > 0);
        assertTrue(tmpout.equals(message));
        assertTrue(tmperr.length() == 0);
    }

    private void submitToQueueWithPolling(Scheduler scheduler, String queueName, int jobCount) throws Exception {
    
        String workingDir = "octopus_test_" + System.currentTimeMillis();
        
        FileSystem filesystem = getDefaultFileSystem();
        
        AbsolutePath root = filesystem.getEntryPath().resolve(new RelativePath(workingDir));
        files.createDirectory(root);
        
        AbsolutePath [] out = new AbsolutePath[jobCount]; 
        AbsolutePath [] err = new AbsolutePath[jobCount]; 
        
        Jobs jobs = octopus.jobs();
        
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
    }
    
    @org.junit.Test
    public void test33_testMultiBatchJobSubmitWithPolling() throws Exception {
        
        Scheduler scheduler = getDefaultScheduler();
        
        for (String queue : scheduler.getQueueNames()) { 
            submitToQueueWithPolling(scheduler, queue, 10);    
        }

        jobs.close(scheduler);
    }

    @org.junit.Test
    public void test34_batchJobSubmitWithKill() throws Exception {
        
        String workingDir = "octopus_test_" + System.currentTimeMillis();
        
        FileSystem filesystem = getDefaultFileSystem();
        Scheduler scheduler = getDefaultScheduler();
        
        AbsolutePath root = filesystem.getEntryPath().resolve(new RelativePath(workingDir));
        files.createDirectory(root);
        
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/sleep");
        description.setArguments("60");
        description.setInteractive(false);
        description.setWorkingDirectory(workingDir);
        description.setStdin(null);
        
        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.cancelJob(job);
        
        if (!status.isDone()) { 
            throw new Exception("Failed to kill job!");
        }
        
        AbsolutePath out = root.resolve(new RelativePath(description.getStdout()));
        AbsolutePath err = root.resolve(new RelativePath(description.getStderr()));

        if (files.exists(out)) { 
            files.delete(out);
        }
        
        if (files.exists(err)) { 
            files.delete(err);
        }
        
        files.delete(root);
        
        assertTrue(status.hasException());
        Exception e = status.getException(); 
        
        assertTrue(e instanceof IOException);
        assertTrue(e.getMessage().equals("Process cancelled by user."));
    }

    @org.junit.Test
    public void test35_batchJobSubmitWithKill2() throws Exception {
        
        String workingDir = "octopus_test_" + System.currentTimeMillis();
        
        FileSystem filesystem = getDefaultFileSystem();
        Scheduler scheduler = getDefaultScheduler();
        
        AbsolutePath root = filesystem.getEntryPath().resolve(new RelativePath(workingDir));
        files.createDirectory(root);
        
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/sleep");
        description.setArguments("60");
        description.setInteractive(false);
        description.setWorkingDirectory(workingDir);
        description.setStdin(null);
        
        Job job = jobs.submitJob(scheduler, description);
        
        // Wait for job to run before killing it!
        JobStatus status = jobs.waitUntilDone(job, 1000);
        
        while (!status.isDone() && !status.isRunning()) { 
            status = jobs.waitUntilDone(job, 1000);            
        }
        
        if (status.isRunning()) { 
            status = jobs.cancelJob(job);
        } else { 
            throw new Exception("Job failed to start!");
        }
        
        AbsolutePath out = root.resolve(new RelativePath(description.getStdout()));
        AbsolutePath err = root.resolve(new RelativePath(description.getStderr()));

        files.delete(out);
        files.delete(err);
        files.delete(root);
        
        assertTrue(status.hasException());
        Exception e = status.getException(); 
        
        assertTrue(e instanceof IOException);
        assertTrue(e.getMessage().equals("Process cancelled by user."));
    }

    @org.junit.Test
    public void test35_batchJobSubmitWithInput() throws Exception {
        
        String message = "Hello World!";
        String workingDir = "octopus_test_" + System.currentTimeMillis();
        
        FileSystem filesystem = getDefaultFileSystem();
        Scheduler scheduler = getDefaultScheduler();
        
        AbsolutePath root = filesystem.getEntryPath().resolve(new RelativePath(workingDir));
        files.createDirectory(root);
        
        AbsolutePath stdin = root.resolve(new RelativePath("stdin.txt"));
        
        OutputStream out = files.newOutputStream(stdin, OpenOption.CREATE, OpenOption.APPEND, OpenOption.WRITE);
        writeFully(out, message);
        
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/cat");
        description.setInteractive(false);
        description.setWorkingDirectory(workingDir);
        description.setStdin(stdin.getPath());
        
        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 5000);
        
        if (!status.isDone()) { 
            throw new Exception("Job exceeded deadline!");
        }
        
        if (status.hasException()) {
            throw status.getException();
        }
       
        AbsolutePath stdout = root.resolve(new RelativePath(description.getStdout()));
        AbsolutePath stderr = root.resolve(new RelativePath(description.getStderr()));

        String tmpout = readFully(files.newInputStream(stdout));
        String tmperr = readFully(files.newInputStream(stderr));

        files.delete(stdin);
        files.delete(stdout);
        files.delete(stderr);
        files.delete(root);
        
        System.err.println("STDOUT: " + tmpout);
        System.err.println("STDERR: " + tmperr);
        
        assertTrue(tmpout != null);
        assertTrue(tmpout.length() > 0);
        assertTrue(tmpout.equals(message));
        assertTrue(tmperr.length() == 0);
    }
    
    @org.junit.Test
    public void test36_batchJobSubmitWithInput() throws Exception {
        
        String message = "Hello World!";
        String workingDir = "octopus_test_" + System.currentTimeMillis();
        
        FileSystem filesystem = getDefaultFileSystem();
        Scheduler scheduler = getDefaultScheduler();
        
        AbsolutePath root = filesystem.getEntryPath().resolve(new RelativePath(workingDir));
        files.createDirectory(root);
        
        AbsolutePath stdin = root.resolve(new RelativePath("stdin.txt"));
        
        OutputStream out = files.newOutputStream(stdin, OpenOption.CREATE, OpenOption.APPEND, OpenOption.WRITE);
        writeFully(out, message);
        
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/cat");
        description.setInteractive(false);
        description.setWorkingDirectory(workingDir);
        description.setStdin("stdin.txt");
        
        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 5000);
        
        if (!status.isDone()) { 
            throw new Exception("Job exceeded deadline!");
        }
        
        if (status.hasException()) {
            throw status.getException();
        }
       
        AbsolutePath stdout = root.resolve(new RelativePath(description.getStdout()));
        AbsolutePath stderr = root.resolve(new RelativePath(description.getStderr()));

        String tmpout = readFully(files.newInputStream(stdout));
        String tmperr = readFully(files.newInputStream(stderr));

        files.delete(stdin);
        files.delete(stdout);
        files.delete(stderr);
        files.delete(root);
        
        System.err.println("STDOUT: " + tmpout);
        System.err.println("STDERR: " + tmperr);
        
        assertTrue(tmpout != null);
        assertTrue(tmpout.length() > 0);
        assertTrue(tmpout.equals(message));
        assertTrue(tmperr.length() == 0);
    }
    
    
    @org.junit.Test
    public void test37_batchJobSubmitWithoutWorkDir() throws Exception {
        
        Scheduler scheduler = getDefaultScheduler();
        
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/sleep");
        description.setArguments("1");
        description.setInteractive(false);
        description.setWorkingDirectory(null);
        
        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 5000);
        
        if (!status.isDone()) { 
            throw new Exception("Job exceeded deadline!");
        }
        
        if (status.hasException()) {
            throw status.getException();
        }
    }
    
    @org.junit.Test
    public void test37_multipleBatchJobSubmitWithInput() throws Exception {
        
        String message = "Hello World!";
        String workingDir = "octopus_test_" + System.currentTimeMillis();
        
        FileSystem filesystem = getDefaultFileSystem();
        Scheduler scheduler = getDefaultScheduler();
        
        AbsolutePath root = filesystem.getEntryPath().resolve(new RelativePath(workingDir));
        files.createDirectory(root);
        
        AbsolutePath stdin = root.resolve(new RelativePath("stdin.txt"));
        
        OutputStream out = files.newOutputStream(stdin, OpenOption.CREATE, OpenOption.APPEND, OpenOption.WRITE);
        writeFully(out, message);
        
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/cat");
        description.setInteractive(false);
        description.setProcessesPerNode(2);
        description.setMergeOutputStreams(false);
        description.setWorkingDirectory(workingDir);
        description.setStdin("stdin.txt");
        
        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 5000);
        
        if (!status.isDone()) { 
            throw new Exception("Job exceeded deadline!");
        }
        
        if (status.hasException()) {
            throw status.getException();
        }
       
        for (int i=0;i<2;i++) { 

            AbsolutePath stdout = root.resolve(new RelativePath(description.getStdout() + "." + i));
            AbsolutePath stderr = root.resolve(new RelativePath(description.getStderr() + "." + i));

            String tmpout = readFully(files.newInputStream(stdout));
            String tmperr = readFully(files.newInputStream(stderr));

            System.err.println("STDOUT: " + tmpout);
            System.err.println("STDERR: " + tmperr);
            
            assertTrue(tmpout != null);
            assertTrue(tmpout.length() > 0);
            assertTrue(tmpout.equals(message));
            assertTrue(tmperr.length() == 0);
            
            files.delete(stdout);
            files.delete(stderr);
        } 

        files.delete(stdin);           
        files.delete(root);        
    }
    
    
    
    
    /**
     * Submit a job to a Scheduler.
     * 
     * @param scheduler
     *            the Scheduler.
     * @param description
     *            the description of the job to submit.
     * 
     * @return Job representing the running job.
     * 
     * @throws NoSchedulerException
     *             If the scheduler is not known.
     * @throws IncompleteJobDescriptionException
     *             If the description did not contain the required information.
     * @throws InvalidJobDescriptionException
     *             If the description contains illegal or conflicting values.
     * @throws UnsupportedJobDescriptionException
     *             If the description is not legal for this scheduler.
     * @throws OctopusException
     *             If the Scheduler failed to get submit the job.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    //public Job submitJob(Scheduler scheduler, JobDescription description) throws OctopusException, OctopusIOException;

    /**
     * Get the status of a Job.
     * 
     * @param job
     *            the job.
     * 
     * @return the status of the Job.
     * 
     * @throws NoSuchJobException
     *             If the job is not known.
     * @throws OctopusException
     *             If the status of the job could not be retrieved.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
   // public JobStatus getJobStatus(Job job) throws OctopusException, OctopusIOException;

    /**
     * Get the status of all specified <code>jobs</code>.
     * 
     * The array of <code>JobStatus</code> contains one entry for each of the <code>jobs</code>. The order of the elements in the 
     * returned <code>JobStatus</code> array corresponds to the order in which the <code>jobs</code> are passed as parameters.   
     * If a <code>job</code> is <code>null</code>, the corresponding entry in the <code>JobStatus</code> array will also be 
     * <code>null</code>. If the retrieval of the <code>JobStatus</code> fails for a job, the exception will be stored in the 
     * corresponding <code>JobsStatus</code> entry.
     * 
     * @param jobs
     *            the jobs for which to retrieve the status.
     * 
     * @return an array of the resulting JobStatusses.
     * 
     * @throws OctopusException
     *             If the statuses of the job could not be retrieved.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    //public JobStatus[] getJobStatuses(Job... jobs);
    
    /** 
     * Returns the standard streams of a job.
     * 
     * The standard streams can only be retrieved if it is an interactive job.  
     * 
     * @param job the interactive job for which to retrieve the streams.
     * @return the streams of the job.
     *
     * @throws OctopusException if the job is not interactive.
     */
   // public Streams getStreams(Job job) throws OctopusException;
        
    /**
     * Cancel a job.
     * 
     * A status is returned that indicates the state of the job after the cancel.  If the jobs was already done it cannot be 
     * killed afterwards.   
     * 
     * @param job the job to kill.
     * @return the status of the Job.
     * 
     * @throws NoSuchJobException
     *             If the job is not known.
     * @throws OctopusException
     *             If the status of the job could not be retrieved.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    //public JobStatus cancelJob(Job job) throws OctopusException, OctopusIOException;


    /**
     * Wait until a job is done or until a timeout expires. 
     * 
     * This method will wait until a job is done, killed, or produces an error, or until a timeout expires. If the  
     * timeout expires, the job will continue to run normally. 
     * 
     * The timeout is in milliseconds and must be >= 0, where 0 means and infinite timeout.      
     * 
     * A JobStatus is returned that can be used to determine why the call returned.    
     * 
     * @param job the job.
     * @param timeout the maximum time to wait for the job in milliseconds.   
     * @returns  the status of the Job.
     * 
     * @throws NoSuchJobException
     *             If the job is not known.
     * @throws OctopusException
     *             If the status of the job could not be retrieved.
     * @throws OctopusIOException
     *             If an I/O error occurred.
     */
    //public JobStatus waitUntilDone(Job job, long timeout) throws OctopusException, OctopusIOException;
}
