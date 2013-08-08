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
package nl.esciencecenter.octopus.adaptors.slurm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import nl.esciencecenter.octopus.adaptors.scripting.FakeScriptingJob;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.exceptions.JobCanceledException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobStatus;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Niels Drost
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SlurmSchedulerConnectionTest {

    @Test
    public void test01a_exitcodeFromString_SomeExitcode_Integer() throws OctopusException {
        String input = "5";

        Integer expected = 5;

        Integer result = SlurmSchedulerConnection.exitcodeFromString(input);

        assertEquals("Failed to obtain exit code from simple string", expected, result);
    }

    @Test
    public void test01b_exitcodeFromString_SomeExitcodeWithSignal_Integer() throws OctopusException {
        String input = "5:43";

        Integer expected = 5;

        Integer result = SlurmSchedulerConnection.exitcodeFromString(input);

        assertEquals("Failed to obtain exit code from exitcode string with signal", expected, result);
    }

    @Test
    public void test01c_exitcodeFromString_NullInput_NullOutput() throws OctopusException {
        String input = null;

        Integer expected = null;

        Integer result = SlurmSchedulerConnection.exitcodeFromString(input);

        assertEquals("Null input should lead to Null output", expected, result);
    }

    @Test(expected = OctopusException.class)
    public void test01d_exitcodeFromString_NotANumber_ExceptionThrown() throws OctopusException {
        String input = "five";

        SlurmSchedulerConnection.exitcodeFromString(input);
    }

    //new JobStatusImplementation(inputJob, state, exitCode, error, running, done, jobInfo);

    @Test
    public void test02a_getJobStatusFromSacctInfo_CompletedJob_JobStatus() throws OctopusException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("JobID", jobID);
        jobInfo.put("State", "COMPLETED");
        jobInfo.put("ExitCode", "5:0");

        Map<String, Map<String, String>> input = new HashMap<String, Map<String, String>>();
        input.put(jobID, jobInfo);
        Job job = new FakeScriptingJob(jobID);
        JobStatus result = SlurmSchedulerConnection.getJobStatusFromSacctInfo(input, job);
        
        assertEquals(job, result.getJob());
        assertEquals("COMPLETED", result.getState());
        assertEquals(new Integer(5), result.getExitCode());
        assertFalse(result.hasException());
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }
    
    @Test
    public void test02b_getJobStatusFromSacctInfo_RunningJob_JobStatus() throws OctopusException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("JobID", jobID);
        jobInfo.put("State", "RUNNING");
        jobInfo.put("ExitCode", "0:0");

        Map<String, Map<String, String>> input = new HashMap<String, Map<String, String>>();
        input.put(jobID, jobInfo);
        Job job = new FakeScriptingJob(jobID);
        JobStatus result = SlurmSchedulerConnection.getJobStatusFromSacctInfo(input, job);
        
        assertEquals(job, result.getJob());
        assertEquals("RUNNING", result.getState());
        assertEquals(new Integer(0), result.getExitCode());
        assertFalse(result.hasException());
        assertTrue(result.isRunning());
        assertFalse(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }
    
    @Test
    public void test02c_getJobStatusFromSacctInfo_CanceledJob_JobStatusWithException() throws OctopusException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("JobID", jobID);
        jobInfo.put("State", "CANCELLED");
        jobInfo.put("ExitCode", "0:0");

        Map<String, Map<String, String>> input = new HashMap<String, Map<String, String>>();
        input.put(jobID, jobInfo);
        Job job = new FakeScriptingJob(jobID);
        JobStatus result = SlurmSchedulerConnection.getJobStatusFromSacctInfo(input, job);
        
        assertEquals(job, result.getJob());
        assertEquals("CANCELLED", result.getState());
        assertEquals(new Integer(0), result.getExitCode());
        assertTrue(result.hasException());
        assertTrue(result.getException() instanceof JobCanceledException);
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }
    
    @Test
    public void test02d_getJobStatusFromSacctInfo_JobWithNonZeroExitCode_JobStatusWithNoException() throws OctopusException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("JobID", jobID);
        jobInfo.put("State", "FAILED");
        jobInfo.put("ExitCode", "11:0");

        Map<String, Map<String, String>> input = new HashMap<String, Map<String, String>>();
        input.put(jobID, jobInfo);
        Job job = new FakeScriptingJob(jobID);
        JobStatus result = SlurmSchedulerConnection.getJobStatusFromSacctInfo(input, job);
        
        assertEquals(job, result.getJob());
        assertEquals("FAILED", result.getState());
        assertEquals(new Integer(11), result.getExitCode());
        assertFalse(result.hasException());
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }
    
    @Test
    public void test02e_getJobStatusFromSacctInfo_FailedJobWithZeroExitCode_JobStatusWithException() throws OctopusException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("JobID", jobID);
        jobInfo.put("State", "FAILED");
        jobInfo.put("ExitCode", "0:0");

        Map<String, Map<String, String>> input = new HashMap<String, Map<String, String>>();
        input.put(jobID, jobInfo);
        Job job = new FakeScriptingJob(jobID);
        JobStatus result = SlurmSchedulerConnection.getJobStatusFromSacctInfo(input, job);
        
        assertEquals(job, result.getJob());
        assertEquals("FAILED", result.getState());
        assertEquals(new Integer(0), result.getExitCode());
        assertTrue(result.hasException());
        assertTrue(result.getException() instanceof OctopusException);
        assertFalse(result.getException() instanceof JobCanceledException);
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }
    
    @Test
    public void test02f_getJobStatusFromSacctInfo_JobNotInMap_NullReturned() throws OctopusException {
        String jobID = "555";
        Map<String, Map<String, String>> input = new HashMap<String, Map<String, String>>();
        Job job = new FakeScriptingJob(jobID);
        JobStatus result = SlurmSchedulerConnection.getJobStatusFromSacctInfo(input, job);
        
        assertNull(result);
    }
    
    @Test(expected=OctopusException.class)
    public void test02g_getJobStatusFromSacctInfo_InvalidJobInfo_ExceptionThrown() throws OctopusException {
        String jobID = "555";
        //very invalid info, no info at all
        Map<String, String> jobInfo = new HashMap<String, String>();

        Map<String, Map<String, String>> input = new HashMap<String, Map<String, String>>();
        input.put(jobID, jobInfo);
        
        Job job = new FakeScriptingJob(jobID);
        
        SlurmSchedulerConnection.getJobStatusFromSacctInfo(input, job);
    }


    @Test
    public void test03a_getJobStatusFromScontrolInfo_CompletedJob_JobStatus() throws OctopusException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("JobId", jobID);
        jobInfo.put("JobState", "COMPLETED");
        jobInfo.put("ExitCode", "5:0");
        jobInfo.put("Reason", "None");

        Job job = new FakeScriptingJob(jobID);
        JobStatus result = SlurmSchedulerConnection.getJobStatusFromScontrolInfo(jobInfo, job);
        
        assertEquals(job, result.getJob());
        assertEquals("COMPLETED", result.getState());
        assertEquals(new Integer(5), result.getExitCode());
        assertFalse(result.hasException());
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }
    
    @Test
    public void test03b_getJobStatusFromScontrolInfo_RunningJob_JobStatus() throws OctopusException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("JobId", jobID);
        jobInfo.put("JobState", "RUNNING");
        jobInfo.put("ExitCode", "0:0");
        jobInfo.put("Reason", "None");

        Job job = new FakeScriptingJob(jobID);
        JobStatus result = SlurmSchedulerConnection.getJobStatusFromScontrolInfo(jobInfo, job);
        
        assertEquals(job, result.getJob());
        assertEquals("RUNNING", result.getState());
        assertEquals(new Integer(0), result.getExitCode());
        assertFalse(result.hasException());
        assertTrue(result.isRunning());
        assertFalse(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }
    
    @Test
    public void test03c_getJobStatusFromScontrolInfo_CanceledJob_JobStatusWithException() throws OctopusException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("JobId", jobID);
        jobInfo.put("JobState", "CANCELLED");
        jobInfo.put("ExitCode", "0:0");
        jobInfo.put("Reason", "None");

        Job job = new FakeScriptingJob(jobID);
        JobStatus result = SlurmSchedulerConnection.getJobStatusFromScontrolInfo(jobInfo, job);
        
        assertEquals(job, result.getJob());
        assertEquals("CANCELLED", result.getState());
        assertEquals(new Integer(0), result.getExitCode());
        assertTrue(result.hasException());
        assertTrue(result.getException() instanceof JobCanceledException);
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }
    
    @Test
    public void test03d_getJobStatusFromScontrolInfo_JobWithNonZeroExitCode_JobStatusWithNoException() throws OctopusException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("JobId", jobID);
        jobInfo.put("JobState", "FAILED");
        jobInfo.put("ExitCode", "11:0");
        jobInfo.put("Reason", "NonZeroExitCode");

        Job job = new FakeScriptingJob(jobID);
        JobStatus result = SlurmSchedulerConnection.getJobStatusFromScontrolInfo(jobInfo, job);
        
        assertEquals(job, result.getJob());
        assertEquals("FAILED", result.getState());
        assertEquals(new Integer(11), result.getExitCode());
        assertFalse(result.hasException());
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }
    
    @Test
    public void test03e_getJobStatusFromScontrolInfo_FailedJob_JobStatusWithException() throws OctopusException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("JobId", jobID);
        jobInfo.put("JobState", "FAILED");
        jobInfo.put("ExitCode", "4:0");
        jobInfo.put("Reason", "SomethingWentWrongNoIdea");

        Job job = new FakeScriptingJob(jobID);
        JobStatus result = SlurmSchedulerConnection.getJobStatusFromScontrolInfo(jobInfo, job);
        
        assertEquals(job, result.getJob());
        assertEquals("FAILED", result.getState());
        assertEquals(new Integer(4), result.getExitCode());
        assertTrue(result.hasException());
        assertTrue(result.getException() instanceof OctopusException);
        assertFalse(result.getException() instanceof JobCanceledException);
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }
    
    @Test
    public void test03f_getJobStatusFromScontrolInfo_FailedJobWithNoReason_JobStatusWithException() throws OctopusException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("JobId", jobID);
        jobInfo.put("JobState", "FAILED");
        jobInfo.put("ExitCode", "4:0");
        jobInfo.put("Reason", "None");

        Job job = new FakeScriptingJob(jobID);
        JobStatus result = SlurmSchedulerConnection.getJobStatusFromScontrolInfo(jobInfo, job);
        
        assertEquals(job, result.getJob());
        assertEquals("FAILED", result.getState());
        assertEquals(new Integer(4), result.getExitCode());
        assertTrue(result.hasException());
        assertTrue(result.getException() instanceof OctopusException);
        assertFalse(result.getException() instanceof JobCanceledException);
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }
    
    @Test
    public void test03g_getJobStatusFromScontrolInfo_NullInput_NullReturned() throws OctopusException {
        String jobID = "555";
        Job job = new FakeScriptingJob(jobID);
        JobStatus result = SlurmSchedulerConnection.getJobStatusFromScontrolInfo(null, job);
        
        assertNull(result);
    }
    
    @Test(expected=OctopusException.class)
    public void test03h_getJobStatusFromScontrolInfo_IncompleteJobInfo_ExceptionThrown() throws OctopusException {
        String jobID = "555";
        //empty job info
        Map<String, String> jobInfo = new HashMap<String, String>();

        Job job = new FakeScriptingJob(jobID);
        
        SlurmSchedulerConnection.getJobStatusFromScontrolInfo(jobInfo, job);
    }

    @Test
    public void test04a_getJobStatusFromSqueueInfo_PendingJob_JobStatus() throws OctopusException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("JOBID", jobID);
        jobInfo.put("STATE", "PENDING");

        Map<String, Map<String, String>> input = new HashMap<String, Map<String, String>>();
        input.put(jobID, jobInfo);
        Job job = new FakeScriptingJob(jobID);
        JobStatus result = SlurmSchedulerConnection.getJobStatusFromSqueueInfo(input, job);
        
        assertEquals(job, result.getJob());
        assertEquals("PENDING", result.getState());
        assertNull(result.getExitCode());
        assertFalse(result.hasException());
        assertFalse(result.isRunning());
        assertFalse(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }
    
    @Test
    public void test04b_getJobStatusFromSqueueInfo_RunningJob_JobStatus() throws OctopusException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("JOBID", jobID);
        jobInfo.put("STATE", "RUNNING");

        Map<String, Map<String, String>> input = new HashMap<String, Map<String, String>>();
        input.put(jobID, jobInfo);
        Job job = new FakeScriptingJob(jobID);
        JobStatus result = SlurmSchedulerConnection.getJobStatusFromSqueueInfo(input, job);
        
        assertEquals(job, result.getJob());
        assertEquals("RUNNING", result.getState());
        assertNull(result.getExitCode());
        assertFalse(result.hasException());
        assertTrue(result.isRunning());
        assertFalse(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }
    
    @Test
    public void test04c_getJobStatusFromSqueueInfo_JobNotInMap_NullReturned() throws OctopusException {
        String jobID = "555";
        Map<String, Map<String, String>> input = new HashMap<String, Map<String, String>>();
        Job job = new FakeScriptingJob(jobID);
        JobStatus result = SlurmSchedulerConnection.getJobStatusFromSqueueInfo(input, job);
        
        assertNull(result);
    }
    
    @Test(expected=OctopusException.class)
    public void test04d_getJobStatusFromSqueueInfo_IncompleteJobInfo_ExceptionThrown() throws OctopusException {
        String jobID = "555";

        //very incomplete job info
        Map<String, String> jobInfo = new HashMap<String, String>();

        Map<String, Map<String, String>> input = new HashMap<String, Map<String, String>>();
        input.put(jobID, jobInfo);
        
        Job job = new FakeScriptingJob(jobID);
        
        SlurmSchedulerConnection.getJobStatusFromSqueueInfo(input, job);
    }

    @Test
    public void test05_getQueueStatusFromSInfo() {
        fail("Not yet implemented");
    }

    @Test
    public void test06_identifiersAsCSList() {
        fail("Not yet implemented");
    }

    @Test
    public void test07_isDoneState() {
        fail("Not yet implemented");
    }

    @Test
    public void test08_isFailedState() {
        fail("Not yet implemented");
    }

    @Test
    public void test09_verifyJobDescriptionJobDescription() {
        fail("Not yet implemented");
    }

}
