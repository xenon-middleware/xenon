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
package nl.esciencecenter.xenon.adaptors.torque;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.scripting.FakeScriptingJob;
import nl.esciencecenter.xenon.jobs.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobCanceledException;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;

/**
 * @author Niels Drost
 * @author Joris Borgdorff
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TorqueSchedulerConnectionTest {

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

        TorqueSchedulerConnection.verifyJobDescription(description);
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
        description.addJobOption(TorqueSchedulerConnection.JOB_OPTION_JOB_SCRIPT, "some.script");
        description.setInteractive(false);

        TorqueSchedulerConnection.verifyJobDescription(description);
    }

    @Test
    public void test01c_verifyJobDescription_JobScriptSet_NoFurtherChecking() throws Exception {
        JobDescription description = new JobDescription();

        //set a job option
        description.addJobOption(TorqueSchedulerConnection.JOB_OPTION_JOB_SCRIPT, "some.script");
        description.setInteractive(false);

        //All these settings are wrong. This should not lead to an error
        description.setExecutable(null);
        description.setNodeCount(0);
        description.setProcessesPerNode(0);
        description.setMaxTime(0);
        //GridEngine specific info

        TorqueSchedulerConnection.verifyJobDescription(description);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test01d_verifyJobDescription_InvalidOptions_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        //set a job option
        description.addJobOption("wrong.setting", "wrong.value");
        description.setInteractive(false);

        TorqueSchedulerConnection.verifyJobDescription(description);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test01e_verifyJobDescription_InteractiveJob_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        description.setInteractive(true);

        TorqueSchedulerConnection.verifyJobDescription(description);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test01f_verifyJobDescription_InvalidStandardSetting_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        //verify the standard settings are also checked
        description.setExecutable("bin/bla");
        description.setMaxTime(0);

        TorqueSchedulerConnection.verifyJobDescription(description);
    }
    
    @Test(expected = InvalidJobDescriptionException.class)
    public void test01k_verifyJobDescription_InteractiveJob_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        description.setExecutable("/bin/nothing");
        description.setInteractive(true);

        TorqueSchedulerConnection.verifyJobDescription(description);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test01l_verifyJobDescription_InteractiveJob_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        description.setExecutable("/bin/nothing");
        description.setInteractive(true);
        description.addJobOption(TorqueSchedulerConnection.JOB_OPTION_JOB_CONTENTS, "some");
        description.addJobOption(TorqueSchedulerConnection.JOB_OPTION_JOB_SCRIPT, "other");

        TorqueSchedulerConnection.verifyJobDescription(description);
    }

    @Test
    public void test04a_getJobStatusFromQstatInfo_PendingJob_JobStatus() throws XenonException {
        String jobID = "555.localhost";
        Map<String, String> jobInfo = new HashMap<>(3);
        jobInfo.put("Job_Id", jobID);
        jobInfo.put("job_state", "Q");
        
        Map<String, Map<String, String>> input = new HashMap<>(2);
        input.put(jobID, jobInfo);
        Job job = new FakeScriptingJob(jobID);
        JobStatus result = TorqueSchedulerConnection.getJobStatusFromQstatInfo(input, job);

        assertEquals(job, result.getJob());
        assertEquals("Q", result.getState());
        assertNull(result.getExitCode());
        assertFalse(result.hasException());
        assertFalse(result.isRunning());
        assertFalse(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }

    @Test
    public void test04b_getJobStatusFromQstatInfo_RunningJob_JobStatus() throws XenonException {
        String jobID = "555.localhost";
        Map<String, String> jobInfo = new HashMap<>(3);
        jobInfo.put("Job_Id", jobID);
        jobInfo.put("job_state", "R");
        
        Map<String, Map<String, String>> input = new HashMap<>(2);
        input.put(jobID, jobInfo);
        Job job = new FakeScriptingJob(jobID);
        JobStatus result = TorqueSchedulerConnection.getJobStatusFromQstatInfo(input, job);

        assertEquals(job, result.getJob());
        assertEquals("R", result.getState());
        assertNull(result.getExitCode());
        assertFalse(result.hasException());
        assertTrue(result.isRunning());
        assertFalse(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecficInformation());
    }
    
    @Test
    public void test04c_getJobStatusFromQstatInfo_ErrorJob_JobStatusWithExcepion() throws XenonException {
        String jobID = "555.localhost";
        Map<String, String> jobInfo = new HashMap<>(3);
        jobInfo.put("Job_Id", jobID);
        jobInfo.put("job_state", "E");
        
        Map<String, Map<String, String>> input = new HashMap<>(2);
        input.put(jobID, jobInfo);
        Job job = new FakeScriptingJob(jobID);
        JobStatus result = TorqueSchedulerConnection.getJobStatusFromQstatInfo(input, job);

        assertEquals(job, result.getJob());
        assertEquals("E", result.getState());
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
        String jobID = "555.localhost";
        Map<String, Map<String, String>> input = new HashMap<>(0);
        Job job = new FakeScriptingJob(jobID);
        JobStatus result = TorqueSchedulerConnection.getJobStatusFromQstatInfo(input, job);

        assertNull(result);
    }

    @Test(expected = XenonException.class)
    public void test04e_getJobStatusFromQstatInfo_IncompleteJobInfo_ExceptionThrown() throws XenonException {
        String jobID = "555.localhost";

        //very incomplete job info
        Map<String, String> jobInfo = new HashMap<>(0);

        Map<String, Map<String, String>> input = new HashMap<>(2);
        input.put(jobID, jobInfo);

        Job job = new FakeScriptingJob(jobID);

        TorqueSchedulerConnection.getJobStatusFromQstatInfo(input, job);
    }
}
