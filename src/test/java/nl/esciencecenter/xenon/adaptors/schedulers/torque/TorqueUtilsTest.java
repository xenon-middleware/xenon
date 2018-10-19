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
package nl.esciencecenter.xenon.adaptors.schedulers.torque;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.JobCanceledException;
import nl.esciencecenter.xenon.schedulers.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TorqueUtilsTest {

    @Test
    public void test01a_generate_EmptyDescription_Result() throws XenonException {
        JobDescription description = new JobDescription();

        String result = TorqueUtils.generate(description, null);

        String expected = "#!/bin/sh\n" + "#PBS -S /bin/sh\n" + "#PBS -N xenon\n" + "#PBS -l nodes=1:ppn=1\n" + "#PBS -l walltime=00:15:00\n" + "\nnull\n";

        assertEquals(expected, result);
    }

    @Test
    public void test_generate_Name() throws XenonException {
        JobDescription description = new JobDescription();
        description.setName("test");

        String result = TorqueUtils.generate(description, null);

        String expected = "#!/bin/sh\n" + "#PBS -S /bin/sh\n" + "#PBS -N test\n" + "#PBS -l nodes=1:ppn=1\n" + "#PBS -l walltime=00:15:00\n" + "\nnull\n";

        assertEquals(expected, result);
    }

    @Test
    public void test_generate_EmptyName() throws XenonException {
        JobDescription description = new JobDescription();
        description.setName("");

        String result = TorqueUtils.generate(description, null);

        String expected = "#!/bin/sh\n" + "#PBS -S /bin/sh\n" + "#PBS -N xenon\n" + "#PBS -l nodes=1:ppn=1\n" + "#PBS -l walltime=00:15:00\n" + "\nnull\n";

        assertEquals(expected, result);
    }

    @Test
    public void test_generate_Memory() throws XenonException {
        JobDescription description = new JobDescription();
        description.setMaxMemory(1024);

        String result = TorqueUtils.generate(description, null);

        String expected = "#!/bin/sh\n" + "#PBS -S /bin/sh\n" + "#PBS -N xenon\n" + "#PBS -l nodes=1:ppn=1\n" + "#PBS -l mem=1024\n"
                + "#PBS -l walltime=00:15:00\n" + "\nnull\n";

        assertEquals(expected, result);
    }

    @Test
    public void test_generate_threadsPerProcess() throws XenonException {
        JobDescription description = new JobDescription();
        description.setThreadsPerProcess(4);

        String result = TorqueUtils.generate(description, null);

        String expected = "#!/bin/sh\n" + "#PBS -S /bin/sh\n" + "#PBS -N xenon\n" + "#PBS -l nodes=1:ppn=4\n" + "#PBS -l walltime=00:15:00\n" + "\nnull\n";

        assertEquals(expected, result);
    }

    @Test
    /**
     * Check to see if the output is _exactly_ what we expect, and not a single char different.
     *
     * @throws XenonException
     */
    public void test01b_generate__FilledDescription_Result() throws XenonException {
        JobDescription description = new JobDescription();
        description.setArguments("some", "arguments");
        description.addEnvironment("some.more", "environment value with spaces");
        description.addSchedulerArgument("-l list-of-resources");
        description.setExecutable("/bin/executable");
        description.setMaxRuntime(100);
        description.setNodeCount(1);
        description.setProcessesPerNode(1);
        description.setQueueName("the.queue");
        description.setWorkingDirectory("/some/working/directory");

        String result = TorqueUtils.generate(description, null);

        String expected = "#!/bin/sh\n" + "#PBS -S /bin/sh\n" + "#PBS -N xenon\n" + "#PBS -d /some/working/directory\n" + "#PBS -q the.queue\n"
                + "#PBS -l nodes=1:ppn=1\n" + "#PBS -l walltime=01:40:00\n" + "#PBS -l list-of-resources\n"
                + "export some.more=\"environment value with spaces\"\n\n" + "/bin/executable 'some' 'arguments'\n";

        assertEquals(expected, result);
    }

    @Test
    /**
     * Check to see if the output is _exactly_ what we expect, and not a single char different.
     *
     * @throws XenonException
     */
    public void test01c_generate__ParallelDescription_Result() throws XenonException {
        JobDescription description = new JobDescription();
        description.setArguments("some", "arguments");
        description.addEnvironment("some", "environment.value");
        description.addSchedulerArgument("-l list-of-resources");
        description.setExecutable("/bin/executable");
        description.setMaxRuntime(100);
        description.setNodeCount(4);
        description.setProcessesPerNode(10);
        description.setQueueName("the.queue");
        description.setWorkingDirectory("/some/working/directory");

        String result = TorqueUtils.generate(description, null);

        String expected = "#!/bin/sh\n" + "#PBS -S /bin/sh\n" + "#PBS -N xenon\n" + "#PBS -d /some/working/directory\n" + "#PBS -q the.queue\n"
                + "#PBS -l nodes=4:ppn=10\n" + "#PBS -l walltime=01:40:00\n" + "#PBS -l list-of-resources\n" + "export some=\"environment.value\"\n\n"
                + "/bin/executable 'some' 'arguments'\n";

        assertEquals(expected, result);
    }

    @Test
    public void test01d_generate_CustomContents() throws XenonException {
        JobDescription description = new JobDescription();
        description.addJobOption(TorqueUtils.JOB_OPTION_JOB_CONTENTS, "/myscript/or_other");

        String result = TorqueUtils.generate(description, null);

        String expected = "#!/bin/sh\n" + "#PBS -S /bin/sh\n" + "#PBS -N xenon\n" + "#PBS -l nodes=1:ppn=1\n" + "#PBS -l walltime=00:15:00\n"
                + "\n/myscript/or_other\n";

        assertEquals(expected, result);
    }

    @Test
    public void test03a_generateSerialScriptContent() {
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/executable");
        description.setArguments("some", "arguments");

        Formatter output = new Formatter();

        String expected = "/bin/executable 'some' 'arguments'\n";

        TorqueUtils.generateScriptContent(description, output);

        assertEquals("serial script content incorrect", expected, output.out().toString());
    }

    @Test
    public void test03b_generateSerialScriptContentWithStdin() {
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/executable");
        description.setArguments("some", "arguments");
        description.setStdin("in.txt");

        Formatter output = new Formatter();

        String expected = "/bin/executable 'some' 'arguments' < in.txt\n";

        TorqueUtils.generateScriptContent(description, output);

        assertEquals("serial script content incorrect", expected, output.out().toString());
    }

    @Test
    public void test01a_verifyJobDescription_ValidJobDescription_NoException() throws Exception {
        JobDescription description = new JobDescription();

        // all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(1);
        description.setProcessesPerNode(1);
        description.setMaxRuntime(1);
        // GridEngine specific info

        TorqueUtils.verifyJobDescription(description);
    }

    @Test
    public void test01b_verifyJobDescription_ScriptOptionSet_NoException() throws Exception {
        JobDescription description = new JobDescription();

        // all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(1);
        description.setProcessesPerNode(1);
        description.setMaxRuntime(1);
        // GridEngine specific info
        description.addJobOption(TorqueUtils.JOB_OPTION_JOB_SCRIPT, "some.script");

        TorqueUtils.verifyJobDescription(description);
    }

    @Test
    public void test01c_verifyJobDescription_JobScriptSet_NoFurtherChecking() throws Exception {
        JobDescription description = new JobDescription();

        // set a job option
        description.addJobOption(TorqueUtils.JOB_OPTION_JOB_SCRIPT, "some.script");

        // All these settings are wrong. This should not lead to an error
        description.setExecutable(null);
        description.setNodeCount(0);
        description.setProcessesPerNode(0);
        description.setMaxRuntime(0);
        // GridEngine specific info

        TorqueUtils.verifyJobDescription(description);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test01d_verifyJobDescription_InvalidOptions_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        // set a job option
        description.addJobOption("wrong.setting", "wrong.value");

        TorqueUtils.verifyJobDescription(description);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test01f_verifyJobDescription_InvalidStandardSetting_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        // verify the standard settings are also checked
        description.setExecutable("bin/bla");
        description.setMaxRuntime(0);

        TorqueUtils.verifyJobDescription(description);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test01l_verifyJobDescription_JobScriptAndContents_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        description.setExecutable("/bin/nothing");
        description.addJobOption(TorqueUtils.JOB_OPTION_JOB_SCRIPT, "other");
        description.addJobOption(TorqueUtils.JOB_OPTION_JOB_CONTENTS, "some");

        TorqueUtils.verifyJobDescription(description);
    }

    @Test
    public void test04a_getJobStatusFromQstatInfo_PendingJob_JobStatus() throws XenonException {
        String jobID = "555.localhost";
        Map<String, String> jobInfo = new HashMap<>(3);
        jobInfo.put("Job_Id", jobID);
        jobInfo.put("Job_Name", "test");
        jobInfo.put("job_state", "Q");

        Map<String, Map<String, String>> input = new HashMap<>(2);
        input.put(jobID, jobInfo);
        JobStatus result = TorqueUtils.getJobStatusFromQstatInfo(input, jobID);

        assertEquals(jobID, result.getJobIdentifier());
        assertEquals("Q", result.getState());
        assertNull(result.getExitCode());
        assertFalse(result.hasException());
        assertFalse(result.isRunning());
        assertFalse(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test04b_getJobStatusFromQstatInfo_RunningJob_JobStatus() throws XenonException {
        String jobID = "555.localhost";
        Map<String, String> jobInfo = new HashMap<>(3);
        jobInfo.put("Job_Id", jobID);
        jobInfo.put("Job_Name", "test");
        jobInfo.put("job_state", "R");

        Map<String, Map<String, String>> input = new HashMap<>(2);
        input.put(jobID, jobInfo);
        JobStatus result = TorqueUtils.getJobStatusFromQstatInfo(input, jobID);

        assertEquals(jobID, result.getJobIdentifier());
        assertEquals("R", result.getState());
        assertNull(result.getExitCode());
        assertFalse(result.hasException());
        assertTrue(result.isRunning());
        assertFalse(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test04c_getJobStatusFromQstatInfo_ErrorJob_JobStatusWithExcepion() throws XenonException {
        String jobID = "555.localhost";
        Map<String, String> jobInfo = new HashMap<>(3);
        jobInfo.put("Job_Id", jobID);
        jobInfo.put("Job_Name", "test");
        jobInfo.put("job_state", "E");

        Map<String, Map<String, String>> input = new HashMap<>(2);
        input.put(jobID, jobInfo);
        JobStatus result = TorqueUtils.getJobStatusFromQstatInfo(input, jobID);

        assertEquals(jobID, result.getJobIdentifier());
        assertEquals("E", result.getState());
        assertNull(result.getExitCode());
        assertTrue(result.hasException());
        assertTrue(result.getException() instanceof XenonException);
        assertFalse(result.getException() instanceof JobCanceledException);
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test04d_getJobStatusFromQstatInfo_JobNotInMap_NullReturned() throws XenonException {
        String jobID = "555.localhost";
        Map<String, Map<String, String>> input = new HashMap<>(0);
        JobStatus result = TorqueUtils.getJobStatusFromQstatInfo(input, jobID);

        assertNull(result);
    }

    @Test(expected = XenonException.class)
    public void test04e_getJobStatusFromQstatInfo_IncompleteJobInfo_ExceptionThrown() throws XenonException {
        String jobID = "555.localhost";

        // very incomplete job info
        Map<String, String> jobInfo = new HashMap<>(0);

        Map<String, Map<String, String>> input = new HashMap<>(2);
        input.put(jobID, jobInfo);

        TorqueUtils.getJobStatusFromQstatInfo(input, jobID);
    }
}
