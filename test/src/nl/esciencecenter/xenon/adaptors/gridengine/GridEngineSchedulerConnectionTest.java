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
package nl.esciencecenter.xenon.adaptors.gridengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.gridengine.GridEngineSchedulerConnection;
import nl.esciencecenter.xenon.adaptors.scripting.FakeScriptingJob;
import nl.esciencecenter.xenon.jobs.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobCanceledException;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Niels Drost
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GridEngineSchedulerConnectionTest {

    @Test
    public void test01a_verifyJobDescription_ValidJobDescription_NoException() throws Exception {
        JobDescription description = new JobDescription();

        //all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(1);
        description.setProcessesPerNode(1);
        description.setMaxTime(1);
        //GridEngine specific info
        description.setInteractive(false);

        GridEngineSchedulerConnection.verifyJobDescription(description);
    }

    @Test
    public void test01b_verifyJobDescription_ScriptOptionSet_NoException() throws Exception {
        JobDescription description = new JobDescription();

        //all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(1);
        description.setProcessesPerNode(1);
        description.setMaxTime(1);
        //GridEngine specific info
        description.addJobOption(GridEngineSchedulerConnection.JOB_OPTION_JOB_SCRIPT, "some.script");
        description.setInteractive(false);

        GridEngineSchedulerConnection.verifyJobDescription(description);
    }

    @Test
    public void test01c_verifyJobDescription_JobScriptSet_NoFurtherChecking() throws Exception {
        JobDescription description = new JobDescription();

        //set a job option
        description.addJobOption(GridEngineSchedulerConnection.JOB_OPTION_JOB_SCRIPT, "some.script");
        description.setInteractive(false);

        //All these settings are wrong. This should not lead to an error
        description.setExecutable(null);
        description.setNodeCount(0);
        description.setProcessesPerNode(0);
        description.setMaxTime(0);
        //GridEngine specific info

        GridEngineSchedulerConnection.verifyJobDescription(description);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test01d_verifyJobDescription_InvalidOptions_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        //set a job option
        description.addJobOption("wrong.setting", "wrong.value");
        description.setInteractive(false);

        GridEngineSchedulerConnection.verifyJobDescription(description);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test01e_verifyJobDescription_InteractiveJob_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        description.setInteractive(true);

        GridEngineSchedulerConnection.verifyJobDescription(description);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test01f_verifyJobDescription_InvalidStandardSetting_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        //verify the standard settings are also checked
        description.setExecutable("bin/bla");
        description.setMaxTime(0);

        GridEngineSchedulerConnection.verifyJobDescription(description);
    }
    
    @Test
    public void test01g_verifyJobDescription_ValidParallelJobDescriptionWithQueue_NoException() throws Exception {
        JobDescription description = new JobDescription();

        //all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(2);
        description.setProcessesPerNode(2);
        description.setMaxTime(1);
        //GridEngine specific info
        description.setInteractive(false);
        description.addJobOption(GridEngineSchedulerConnection.JOB_OPTION_PARALLEL_ENVIRONMENT, "some.pe");
        description.setQueueName("some.queue");

        GridEngineSchedulerConnection.verifyJobDescription(description);
    }
    
    @Test
    public void test01h_verifyJobDescription_ValidParallelJobDescriptionWithSlots_NoException() throws Exception {
        JobDescription description = new JobDescription();

        //all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(2);
        description.setProcessesPerNode(2);
        description.setMaxTime(1);
        //GridEngine specific info
        description.setInteractive(false);
        description.addJobOption(GridEngineSchedulerConnection.JOB_OPTION_PARALLEL_ENVIRONMENT, "some.pe");
        description.addJobOption(GridEngineSchedulerConnection.JOB_OPTION_PARALLEL_SLOTS, "11");

        GridEngineSchedulerConnection.verifyJobDescription(description);
    }
    
    @Test(expected = InvalidJobDescriptionException.class)
    public void test01i_verifyJobDescription_ParallelJobDescriptionWithoutPe_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        //all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(2);
        description.setProcessesPerNode(2);
        description.setMaxTime(1);
        //GridEngine specific info
        description.setInteractive(false);
        description.setQueueName("some.queue");

        GridEngineSchedulerConnection.verifyJobDescription(description);
    }
    
    @Test(expected = InvalidJobDescriptionException.class)
    public void test01j_verifyJobDescription_ParallelJobDescriptionWithoutQueueOrSlots_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        //all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(2);
        description.setProcessesPerNode(2);
        description.setMaxTime(1);
        //GridEngine specific info
        description.setInteractive(false);
        description.addJobOption(GridEngineSchedulerConnection.JOB_OPTION_PARALLEL_ENVIRONMENT, "some.pe");

        GridEngineSchedulerConnection.verifyJobDescription(description);
    }

    @Test
    public void test03a_getJobStatusFromQacctInfo_doneJob_JobStatus() throws XenonException {
        String jobnumber = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("jobnumber", jobnumber);
        jobInfo.put("exit_status", "5");
        jobInfo.put("failed", "0");

        Job job = new FakeScriptingJob(jobnumber);
        JobStatus result = GridEngineSchedulerConnection.getJobStatusFromQacctInfo(jobInfo, job);

        assertEquals(job, result.getJob());
        assertEquals("done", result.getState());
        assertEquals(new Integer(5), result.getExitCode());
        assertFalse(result.hasException());
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }

    @Test
    public void test03b_getJobStatusFromQacctInfo_CanceledJob_JobStatusWithException() throws XenonException {
        String jobnumber = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("jobnumber", jobnumber);
        jobInfo.put("exit_status", "0");
        jobInfo.put("failed", "100: This job was canceled");

        Job job = new FakeScriptingJob(jobnumber);
        JobStatus result = GridEngineSchedulerConnection.getJobStatusFromQacctInfo(jobInfo, job);

        assertEquals(job, result.getJob());
        assertEquals("done", result.getState());
        assertEquals(new Integer(0), result.getExitCode());
        assertTrue(result.hasException());
        assertTrue(result.getException() instanceof JobCanceledException);
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }

    @Test
    public void test03c_getJobStatusFromQacctInfo_JobWithNonZeroexit_status_JobStatusWithNoException() throws XenonException {
        String jobnumber = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("jobnumber", jobnumber);
        jobInfo.put("exit_status", "11");
        jobInfo.put("failed", "0");

        Job job = new FakeScriptingJob(jobnumber);
        JobStatus result = GridEngineSchedulerConnection.getJobStatusFromQacctInfo(jobInfo, job);

        assertEquals(job, result.getJob());
        assertEquals("done", result.getState());
        assertEquals(new Integer(11), result.getExitCode());
        assertFalse(result.hasException());
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }

    @Test
    public void test03d_getJobStatusFromQacctInfo_FailedJob_JobStatusWithException() throws XenonException {
        String jobnumber = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("jobnumber", jobnumber);
        jobInfo.put("exit_status", "4");
        jobInfo.put("failed", "666: SomethingWentWrongNoIdea");

        Job job = new FakeScriptingJob(jobnumber);
        JobStatus result = GridEngineSchedulerConnection.getJobStatusFromQacctInfo(jobInfo, job);

        assertEquals(job, result.getJob());
        assertEquals("done", result.getState());
        assertEquals(new Integer(4), result.getExitCode());
        assertTrue(result.hasException());
        assertTrue(result.getException() instanceof XenonException);
        assertFalse(result.getException() instanceof JobCanceledException);
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }

    @Test
    public void test03e_getJobStatusFromQacctInfo_NullInput_NullReturned() throws XenonException {
        String jobnumber = "555";
        Job job = new FakeScriptingJob(jobnumber);
        JobStatus result = GridEngineSchedulerConnection.getJobStatusFromQacctInfo(null, job);

        assertNull(result);
    }

    @Test(expected = XenonException.class)
    public void test03f_getJobStatusFromQacctInfo_IncompleteJobInfo_ExceptionThrown() throws XenonException {
        String jobnumber = "555";
        //empty job info
        Map<String, String> jobInfo = new HashMap<String, String>();

        Job job = new FakeScriptingJob(jobnumber);

        GridEngineSchedulerConnection.getJobStatusFromQacctInfo(jobInfo, job);
    }
    
    @Test(expected = XenonException.class)
    public void test03g_getJobStatusFromQacctInfo_ExitCodeNotANumber_ExceptionThrown() throws XenonException {
        String jobnumber = "555";
        //empty job info
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("jobnumber", jobnumber);
        jobInfo.put("exit_status", "four");
        jobInfo.put("failed", "0");
        
        Job job = new FakeScriptingJob(jobnumber);

        GridEngineSchedulerConnection.getJobStatusFromQacctInfo(jobInfo, job);
    }
    
    @Test
    public void test04a_getJobStatusFromQstatInfo_PendingJob_JobStatus() throws XenonException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("JB_job_number", jobID);
        jobInfo.put("state", "qw");
        jobInfo.put("long_state", "pending");

        Map<String, Map<String, String>> input = new HashMap<String, Map<String, String>>();
        input.put(jobID, jobInfo);
        Job job = new FakeScriptingJob(jobID);
        JobStatus result = GridEngineSchedulerConnection.getJobStatusFromQstatInfo(input, job);

        assertEquals(job, result.getJob());
        assertEquals("pending", result.getState());
        assertNull(result.getExitCode());
        assertFalse(result.hasException());
        assertFalse(result.isRunning());
        assertFalse(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }

    @Test
    public void test04b_getJobStatusFromQstatInfo_RunningJob_JobStatus() throws XenonException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("JB_job_number", jobID);
        jobInfo.put("state", "r");
        jobInfo.put("long_state", "running");

        Map<String, Map<String, String>> input = new HashMap<String, Map<String, String>>();
        input.put(jobID, jobInfo);
        Job job = new FakeScriptingJob(jobID);
        JobStatus result = GridEngineSchedulerConnection.getJobStatusFromQstatInfo(input, job);

        assertEquals(job, result.getJob());
        assertEquals("running", result.getState());
        assertNull(result.getExitCode());
        assertFalse(result.hasException());
        assertTrue(result.isRunning());
        assertFalse(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }
    
    @Test
    public void test04c_getJobStatusFromQstatInfo_ErrorJob_JobStatusWithExcepion() throws XenonException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<String, String>();
        jobInfo.put("JB_job_number", jobID);
        jobInfo.put("state", "qEw");
        jobInfo.put("long_state", "error");

        Map<String, Map<String, String>> input = new HashMap<String, Map<String, String>>();
        input.put(jobID, jobInfo);
        Job job = new FakeScriptingJob(jobID);
        JobStatus result = GridEngineSchedulerConnection.getJobStatusFromQstatInfo(input, job);

        assertEquals(job, result.getJob());
        assertEquals("error", result.getState());
        assertNull(result.getExitCode());
        assertTrue(result.hasException());
        assertTrue(result.getException() instanceof XenonException);
        assertFalse(result.getException() instanceof JobCanceledException);
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }

    @Test
    public void test04d_getJobStatusFromQstatInfo_JobNotInMap_NullReturned() throws XenonException {
        String jobID = "555";
        Map<String, Map<String, String>> input = new HashMap<String, Map<String, String>>();
        Job job = new FakeScriptingJob(jobID);
        JobStatus result = GridEngineSchedulerConnection.getJobStatusFromQstatInfo(input, job);

        assertNull(result);
    }

    @Test(expected = XenonException.class)
    public void test04e_getJobStatusFromQstatInfo_IncompleteJobInfo_ExceptionThrown() throws XenonException {
        String jobID = "555";

        //very incomplete job info
        Map<String, String> jobInfo = new HashMap<String, String>();

        Map<String, Map<String, String>> input = new HashMap<String, Map<String, String>>();
        input.put(jobID, jobInfo);

        Job job = new FakeScriptingJob(jobID);

        GridEngineSchedulerConnection.getJobStatusFromQstatInfo(input, job);
    }
}
