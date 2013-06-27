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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Properties;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.exceptions.InvalidCredentialsException;
import nl.esciencecenter.octopus.exceptions.InvalidPropertyException;
import nl.esciencecenter.octopus.exceptions.NoSuchQueueException;
import nl.esciencecenter.octopus.exceptions.NoSuchSchedulerException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.UnknownPropertyException;
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

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class GenericJobAdaptorTestParent {
    
    private static String TEST_ROOT;
    
    private static JobTestConfig config;
    
    protected Octopus octopus;
    protected Files files;
    protected Jobs jobs;
    protected Credentials credentials;

    protected AbsolutePath testDir;
    
    // MUST be invoked by a @BeforeClass method of the subclass! 
    public static void prepareClass(JobTestConfig testConfig) { 
        config = testConfig;
        TEST_ROOT = "octopus_test_" + config.getAdaptorName() + "_" + System.currentTimeMillis();
    }

    // MUST be invoked by a @AfterClass method of the subclass! 
    public static void cleanupClass() throws Exception { 
        
        System.err.println("GenericJobAdaptorTest.cleanupClass() attempting to remove: " + TEST_ROOT);
        
        Octopus octopus = OctopusFactory.newOctopus(null);
        
        Files files = octopus.files();
        Credentials credentials = octopus.credentials();
        
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);
        
        AbsolutePath root = filesystem.getEntryPath().resolve(new RelativePath(TEST_ROOT));
        
        if (files.exists(root)) { 
            files.delete(root);
        }

        OctopusFactory.endOctopus(octopus);
    }
    
    @Before
    public void prepare() throws OctopusException { 
        octopus = OctopusFactory.newOctopus(null);
        files = octopus.files();
        jobs = octopus.jobs();
        credentials = octopus.credentials();
    }
    
    @After
    public void cleanup() throws OctopusException { 
        OctopusFactory.endOctopus(octopus);
    }
    
    private String getWorkingDir(String testName) { 
        return TEST_ROOT + "/" + testName;
    }
    
    
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
        Scheduler s = jobs.newScheduler(config.getCorrectURI(), null, null);
        jobs.close(s);
    }
    
    @Test(expected = OctopusException.class)
    public void test02a_newScheduler() throws Exception { 
        jobs.newScheduler(config.getURIWrongLocation(), null, null);
    }

    @Test(expected = OctopusException.class)
    public void test02b_newScheduler() throws Exception { 
        jobs.newScheduler(config.getURIWrongPath(), null, null);
    }
    
    @Test
    public void test03_newScheduler() throws Exception {
        Scheduler s = jobs.newScheduler(config.getCorrectURI(), config.getDefaultCredential(credentials), null);
        jobs.close(s);
    }

    @Test
    public void test04a_newScheduler() throws Exception {
        if (config.supportsCredentials()) { 
            try {
                Scheduler s = jobs.newScheduler(config.getCorrectURI(), config.getInvalidCredential(credentials), null);
                jobs.close(s);
                throw new Exception("newScheduler did NOT throw InvalidCredentialsException");
            } catch (InvalidCredentialsException e) {
                // expected
            } catch (OctopusException e) { 
                // allowed
            }
        }
    }

    @Test
    public void test04b_newScheduler() throws Exception {
        if (!config.supportsCredentials()) { 
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
                
                Scheduler s = jobs.newScheduler(config.getCorrectURI(), c, null);
                jobs.close(s);

                throw new Exception("newScheduler did NOT throw OctopusException");
            } catch (OctopusException e) {
                // expected
            }
        }
    }

    @Test
    public void test04c_newScheduler() throws Exception {
        if (config.supportsCredentials()) { 
            Scheduler s = jobs.newScheduler(config.getCorrectURI(), config.getPasswordCredential(credentials), 
                    config.getDefaultProperties());
            jobs.close(s);
        }
    }
    
    @Test
    public void test05_newScheduler() throws Exception {
        Scheduler s = jobs.newScheduler(config.getCorrectURI(), config.getDefaultCredential(credentials), new Properties());
        jobs.close(s);
    }

    @Test
    public void test06_newScheduler() throws Exception {
        Scheduler s = jobs.newScheduler(config.getCorrectURI(), config.getDefaultCredential(credentials), 
                config.getDefaultProperties());
        jobs.close(s);
    }
    
    @Test
    public void test07_newScheduler() throws Exception {
        if (config.supportsProperties()) {
            
            Properties [] tmp = config.getInvalidProperties();
            
            for (Properties p : tmp) { 
                try { 
                    Scheduler s = jobs.newScheduler(config.getCorrectURI(), config.getDefaultCredential(credentials), p);
                    jobs.close(s);
                    throw new Exception("newScheduler did NOT throw InvalidPropertyException");
                } catch (InvalidPropertyException e) {
                    // expected
                }
            }
        }
    }

    @Test
    public void test08_newScheduler() throws Exception {
        if (config.supportsProperties()) { 
            try { 
                Scheduler s = jobs.newScheduler(config.getCorrectURI(), config.getDefaultCredential(credentials), 
                        config.getUnknownProperties());
                jobs.close(s);

                throw new Exception("newScheduler did NOT throw UnknownPropertyException");
            } catch (UnknownPropertyException e) {
                // expected
            }
        }
    }

    @Test
    public void test09_newScheduler() throws Exception {
        if (!config.supportsProperties()) { 
            try { 
                Properties p = new Properties();
                p.put("aap", "noot");
                Scheduler s = jobs.newScheduler(config.getCorrectURI(), config.getDefaultCredential(credentials), p);
                jobs.close(s);

                throw new Exception("newScheduler did NOT throw OctopusException");
            } catch (OctopusException e) {
                // expected
            }
        }
    }
    
    @Test
    public void test10_getLocalScheduler() throws Exception {
        
        Scheduler s = null; 
        
        try { 
            s = jobs.getLocalScheduler();
            assertTrue(s != null);
            assertTrue(s.getAdaptorName().equals("local"));
        } finally { 
            if (s != null) { 
                jobs.close(s);
            }
        }
    } 
     
    @Test
    public void test11_open_close() throws Exception {
        if (config.supportsClose()) { 
            Scheduler s = config.getDefaultScheduler(jobs, credentials); 
             
            assertTrue(jobs.isOpen(s));
            
            jobs.close(s);
            
            assertFalse(jobs.isOpen(s));
        }
    }
    
    @Test
    public void test12_open_close() throws Exception {
        if (!config.supportsClose()) { 
            Scheduler s = config.getDefaultScheduler(jobs, credentials); 
             
            assertTrue(jobs.isOpen(s));
            
            jobs.close(s);
            
            assertTrue(jobs.isOpen(s));
        }
    }

    @Test
    public void test13_open_close() throws Exception {
        if (config.supportsClose()) { 
            Scheduler s = config.getDefaultScheduler(jobs, credentials); 
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
        Scheduler s = config.getDefaultScheduler(jobs, credentials);        
        jobs.getJobs(s, s.getQueueNames());
        jobs.close(s);
    }
    
    @Test
    public void test14b_getJobs() throws Exception {
        Scheduler s = config.getDefaultScheduler(jobs, credentials);        
        jobs.getJobs(s);
        jobs.close(s);
    }
    
    @Test
    public void test15_getJobs() throws Exception {
        Scheduler s = config.getDefaultScheduler(jobs, credentials);        

        try {
            jobs.getJobs(s, config.getInvalidQueueName());
            throw new Exception("close did NOT throw NoSuchQueueException");
        } catch (NoSuchQueueException e) { 
            // expected
        } finally { 
            jobs.close(s);
        }
    }

    @Test
    public void test16_getJobs() throws Exception {
        
        if (config.supportsClose()) { 
        
            Scheduler s = config.getDefaultScheduler(jobs, credentials);        

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
        Scheduler s = config.getDefaultScheduler(jobs, credentials);        
        jobs.getQueueStatus(s, s.getQueueNames()[0]);
        jobs.close(s);
    }
    
    @Test(expected = NoSuchQueueException.class)
    public void test18a_getQueueStatus() throws Exception {        
        Scheduler s = config.getDefaultScheduler(jobs, credentials);
        try { 
            jobs.getQueueStatus(s, config.getInvalidQueueName());
        } finally { 
            jobs.close(s);
        }
    }
    
    @Test
    public void test18b_getQueueStatus() throws Exception {        
        Scheduler s = config.getDefaultScheduler(jobs, credentials);
        String queueName = config.getDefaultQueueName();
        
        try { 
            jobs.getQueueStatus(s, queueName);
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
        
        if (config.supportsClose()) { 
        
            Scheduler s = config.getDefaultScheduler(jobs, credentials);
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
        Scheduler s = config.getDefaultScheduler(jobs, credentials);        
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
        Scheduler s = config.getDefaultScheduler(jobs, credentials);        
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
        Scheduler s = config.getDefaultScheduler(jobs, credentials);        
        try { 
            QueueStatus [] tmp = jobs.getQueueStatuses(s, config.getInvalidQueueName());

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
        
        if (config.supportsClose()) { 
            Scheduler s = config.getDefaultScheduler(jobs, credentials);
            jobs.close(s);
            
            try { 
                jobs.getQueueStatuses(s, s.getQueueNames());
                throw new Exception("getQueueStatuses did NOT throw NoSuchSchedulerException");
            } catch (NoSuchSchedulerException e) { 
                // expected
            }
        }
    }
    
    @Test
    public void test25a_getJobStatuses() throws Exception {
        
        JobStatus[] tmp = jobs.getJobStatuses(new Job[0]);
        
        assertTrue(tmp != null);
        assertTrue(tmp.length == 0);
    }

    @Test
    public void test25b_getJobStatuses() throws Exception {
        
        JobStatus[] tmp = jobs.getJobStatuses((Job []) null);
        
        assertTrue(tmp != null);
        assertTrue(tmp.length == 0);
    }

    
    @Test
    public void test25c_getJobStatuses() throws Exception {
        
        JobStatus[] tmp = jobs.getJobStatuses(new Job[1]);
        
        assertTrue(tmp != null);
        assertTrue(tmp.length == 1);
        assertTrue(tmp[0] == null);
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
        
        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);

        if (scheduler.isOnline()) { 
        
            String message = "Hello World! test30";

            JobDescription description = new JobDescription();
            description.setExecutable("/bin/echo");
            description.setArguments("-n", message);
            description.setInteractive(true);

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
        }
        
        jobs.close(scheduler);
    }
    
    @org.junit.Test
    public void test31_batchJobSubmitWithPolling() throws Exception {
        
        String message = "Hello World! test31";
        String workingDir = getWorkingDir("test31");
        
        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);
        
        AbsolutePath root = filesystem.getEntryPath().resolve(new RelativePath(workingDir));
        files.createDirectories(root);

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
            Thread.sleep(1000);
            
            long now = System.currentTimeMillis();
            
            if (now > deadline) { 
                throw new Exception("Job exceeded deadline!");
            }
            
            status = jobs.getJobStatus(job);
        }

        if (status.hasException()) {
            throw status.getException();
        }
        
        AbsolutePath out = root.resolve(new RelativePath("stdout.txt"));
        AbsolutePath err = root.resolve(new RelativePath("stderr.txt"));
        
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
        
        String message = "Hello World! test32";
        String workingDir = getWorkingDir("test32");
        
        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);
                
        AbsolutePath root = filesystem.getEntryPath().resolve(new RelativePath(workingDir));
        files.createDirectories(root);
        
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

        jobs.close(scheduler);
        
        AbsolutePath out = root.resolve(new RelativePath("stdout.txt"));
        AbsolutePath err = root.resolve(new RelativePath("stderr.txt"));
        
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

    private void submitToQueueWithPolling(String testName, String queueName, int jobCount) throws Exception {
    
        String workingDir = getWorkingDir(testName);
        
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);
        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        
        AbsolutePath root = filesystem.getEntryPath().resolve(new RelativePath(workingDir));
        files.createDirectories(root);
        
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
            description.setWorkingDirectory(workingDir);
            
            description.setQueueName(queueName);
            description.setInteractive(false);
            description.setStdin(null);
            description.setStdout("stdout" + i + ".txt");
            description.setStderr("stderr" + i + ".txt");
    
            j[i] = jobs.submitJob(scheduler, description);
        }
        
        long deadline = System.currentTimeMillis() + (10 * jobCount * 1000);
        
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
                Thread.sleep(1000);
                
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

        jobs.close(scheduler);
        files.delete(root);
    }
    
    @org.junit.Test
    public void test33a_testMultiBatchJobSubmitWithPolling() throws Exception {               
        for (String queue : config.getQueueNames()) { 
            submitToQueueWithPolling("test33a_" + queue, queue, 1);    
        }

    }

    @org.junit.Test
    public void test33b_testMultiBatchJobSubmitWithPolling() throws Exception {        
        for (String queue : config.getQueueNames()) { 
            submitToQueueWithPolling("test33b_" + queue, queue, 3);    
        }
    }
    
    @org.junit.Test
    public void test34_batchJobSubmitWithKill() throws Exception {
        
        String workingDir = getWorkingDir("test34");
        
        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);
        
        AbsolutePath root = filesystem.getEntryPath().resolve(new RelativePath(workingDir));
        files.createDirectories(root);
        
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
        
        jobs.close(scheduler);
        
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
        
        String workingDir = getWorkingDir("test35");
        
        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);
        
        AbsolutePath root = filesystem.getEntryPath().resolve(new RelativePath(workingDir));
        files.createDirectories(root);
        
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
        
        jobs.close(scheduler);
        
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
    public void test36a_batchJobSubmitWithInput() throws Exception {
        
        String message = "Hello World! test36a";
        String workingDir = getWorkingDir("test36a");
        
        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);
        
        AbsolutePath root = filesystem.getEntryPath().resolve(new RelativePath(workingDir));
        files.createDirectories(root);
        
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
        
        jobs.close(scheduler);

        AbsolutePath stdout = root.resolve(new RelativePath("stdout.txt"));
        AbsolutePath stderr = root.resolve(new RelativePath("stderr.txt"));
        
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
    public void test36b_batchJobSubmitWithInput() throws Exception {
        
        String message = "Hello World! test36b";
        String workingDir = getWorkingDir("test36b");
        
        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);
        
        AbsolutePath root = filesystem.getEntryPath().resolve(new RelativePath(workingDir));
        files.createDirectories(root);
        
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
       
        jobs.close(scheduler);
        
        AbsolutePath stdout = root.resolve(new RelativePath("stdout.txt"));
        AbsolutePath stderr = root.resolve(new RelativePath("stderr.txt"));
        
        String tmpout = readFully(files.newInputStream(stdout));
        String tmperr = readFully(files.newInputStream(stderr));
        
        System.err.println("STDOUT: " + tmpout);
        System.err.println("STDERR: " + tmperr);
        
        assertTrue(tmpout != null);
        assertTrue(tmpout.length() > 0);
        assertTrue(tmpout.equals(message));
        assertTrue(tmperr.length() == 0);
        
        files.delete(stdin);
        files.delete(stdout);
        files.delete(stderr);
        files.delete(root);
    }    
    
    @org.junit.Test
    public void test37_batchJobSubmitWithoutWorkDir() throws Exception {
        
        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);
          
        AbsolutePath stdout = filesystem.getEntryPath().resolve(new RelativePath("stdout.txt"));
        AbsolutePath stderr = filesystem.getEntryPath().resolve(new RelativePath("stderr.txt"));
        
        if (files.exists(stdout)) { 
            files.delete(stdout);
        }
        
        if (files.exists(stderr)) { 
            files.delete(stderr);
        }
        
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
    
    //@org.junit.Test
    public void test38_multipleBatchJobSubmitWithInput() throws Exception {
        
        String message = "Hello World! test38";
        String workingDir = getWorkingDir("test38");
        
        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        FileSystem filesystem = config.getDefaultFileSystem(files, credentials);
        
        AbsolutePath root = filesystem.getEntryPath().resolve(new RelativePath(workingDir));
        files.createDirectories(root);
        
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

            AbsolutePath stdoutTmp = root.resolve(new RelativePath("stdout.txt." + i));
            AbsolutePath stderrTmp = root.resolve(new RelativePath("stderr.txt." + i));

            String tmpout = readFully(files.newInputStream(stdoutTmp));
            String tmperr = readFully(files.newInputStream(stderrTmp));

            System.err.println("STDOUT: " + tmpout);
            System.err.println("STDERR: " + tmperr);
            
            assertTrue(tmpout != null);
            assertTrue(tmpout.length() > 0);
            assertTrue(tmpout.equals(message));
            assertTrue(tmperr.length() == 0);
            
            files.delete(stdoutTmp);
            files.delete(stderrTmp);
        } 

        files.delete(stdin);           
        files.delete(root);        
    }
    
    @org.junit.Test
    public void test39_multipleBatchJobSubmitWithExceptions() throws Exception {

        // NOTE: This test assumes that an exception is when the status of a job is requested twice after the job is done!
        //       This may not be true for all schedulers.
        
        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/sleep");
        description.setArguments("1");
        description.setInteractive(false);
        description.setWorkingDirectory(null);
        
        Job[] j = new Job[2];
        
        j[0] = jobs.submitJob(scheduler, description);
        
        description = new JobDescription();
        description.setExecutable("/bin/sleep");
        description.setArguments("2");
        description.setInteractive(false);
        description.setWorkingDirectory(null);
        
        j[1] = jobs.submitJob(scheduler, description);
        
        long now = System.currentTimeMillis();
        long deadline = now + 10000;

        JobStatus [] s = null;

        while (now < deadline) { 
        
            s = jobs.getJobStatuses(j);

            if (s[0].hasException() && s[1].hasException()) { 
                break;
            }
            
            try { 
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignored
            }
            
            now = System.currentTimeMillis();           
        } 
        
        if (s == null || !(s[0].hasException() && s[1].hasException())) { 
            throw new Exception("Job exceeded deadline!");
        }
    }
}
