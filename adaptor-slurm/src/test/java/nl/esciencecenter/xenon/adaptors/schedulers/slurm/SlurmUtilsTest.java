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
package nl.esciencecenter.xenon.adaptors.schedulers.slurm;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.JobCanceledException;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.schedulers.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;
import nl.esciencecenter.xenon.schedulers.QueueStatus;
import nl.esciencecenter.xenon.schedulers.Scheduler;

/**
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SlurmUtilsTest {

    @Test
    public void test01a_exitcodeFromString_SomeExitcode_Integer() throws XenonException {
        String input = "5";

        Integer expected = 5;

        Integer result = SlurmUtils.exitcodeFromString(input);

        assertEquals("Failed to obtain exit code from simple string", expected, result);
    }

    @Test
    public void test01b_exitcodeFromString_SomeExitcodeWithSignal_Integer() throws XenonException {
        String input = "5:43";

        Integer expected = 5;

        Integer result = SlurmUtils.exitcodeFromString(input);

        assertEquals("Failed to obtain exit code from exitcode string with signal", expected, result);
    }

    @Test
    public void test01c_exitcodeFromString_NullInput_NullOutput() throws XenonException {
        String input = null;

        Integer expected = null;

        Integer result = SlurmUtils.exitcodeFromString(input);

        assertEquals("Null input should lead to Null output", expected, result);
    }

    @Test(expected = XenonException.class)
    public void test01d_exitcodeFromString_NotANumber_ExceptionThrown() throws XenonException {
        String input = "five";

        SlurmUtils.exitcodeFromString(input);
    }

    // new JobStatusImplementation(inputJob, state, exitCode, error, running, done, jobInfo);

    @Test
    public void test02a_getJobStatusFromSacctInfo_CompletedJob_JobStatus() throws XenonException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("JobID", jobID);
        jobInfo.put("State", "COMPLETED");
        jobInfo.put("ExitCode", "5:0");

        Map<String, Map<String, String>> input = new HashMap<>();
        input.put(jobID, jobInfo);
        JobStatus result = SlurmUtils.getJobStatusFromSacctInfo(input, jobID);

        assertEquals(jobID, result.getJobIdentifier());
        assertEquals("COMPLETED", result.getState());
        assertEquals(new Integer(5), result.getExitCode());
        assertFalse(result.hasException());
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test02b_getJobStatusFromSacctInfo_RunningJob_JobStatus() throws XenonException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("JobID", jobID);
        jobInfo.put("State", "RUNNING");
        jobInfo.put("ExitCode", "0:0");

        Map<String, Map<String, String>> input = new HashMap<>();
        input.put(jobID, jobInfo);
        JobStatus result = SlurmUtils.getJobStatusFromSacctInfo(input, jobID);

        assertEquals(jobID, result.getJobIdentifier());
        assertEquals("RUNNING", result.getState());
        assertEquals(new Integer(0), result.getExitCode());
        assertFalse(result.hasException());
        assertTrue(result.isRunning());
        assertFalse(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test02c_getJobStatusFromSacctInfo_CanceledJob_JobStatusWithException() throws XenonException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("JobID", jobID);
        jobInfo.put("State", "CANCELLED");
        jobInfo.put("ExitCode", "0:0");

        Map<String, Map<String, String>> input = new HashMap<>();
        input.put(jobID, jobInfo);
        JobStatus result = SlurmUtils.getJobStatusFromSacctInfo(input, jobID);

        assertEquals(jobID, result.getJobIdentifier());
        assertEquals("CANCELLED", result.getState());
        assertEquals(new Integer(0), result.getExitCode());
        assertTrue(result.hasException());
        assertTrue(result.getException() instanceof JobCanceledException);
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test02d_getJobStatusFromSacctInfo_JobWithNonZeroExitCode_JobStatusWithNoException() throws XenonException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("JobID", jobID);
        jobInfo.put("State", "FAILED");
        jobInfo.put("ExitCode", "11:0");

        Map<String, Map<String, String>> input = new HashMap<>();
        input.put(jobID, jobInfo);
        JobStatus result = SlurmUtils.getJobStatusFromSacctInfo(input, jobID);

        assertEquals(jobID, result.getJobIdentifier());
        assertEquals("FAILED", result.getState());
        assertEquals(new Integer(11), result.getExitCode());
        assertFalse(result.hasException());
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test02e_getJobStatusFromSacctInfo_FailedJobWithZeroExitCode_JobStatusWithException() throws XenonException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("JobID", jobID);
        jobInfo.put("State", "FAILED");
        jobInfo.put("ExitCode", "0:0");

        Map<String, Map<String, String>> input = new HashMap<>();
        input.put(jobID, jobInfo);
        JobStatus result = SlurmUtils.getJobStatusFromSacctInfo(input, jobID);

        assertEquals(jobID, result.getJobIdentifier());
        assertEquals("FAILED", result.getState());
        assertEquals(new Integer(0), result.getExitCode());
        assertTrue(result.hasException());
        assertTrue(result.getException() instanceof XenonException);
        assertFalse(result.getException() instanceof JobCanceledException);
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test02f_getJobStatusFromSacctInfo_JobNotInMap_NullReturned() throws XenonException {
        String jobID = "555";
        Map<String, Map<String, String>> input = new HashMap<>();
        JobStatus result = SlurmUtils.getJobStatusFromSacctInfo(input, jobID);

        assertNull(result);
    }

    @Test(expected = XenonException.class)
    public void test02g_getJobStatusFromSacctInfo_InvalidJobInfo_ExceptionThrown() throws XenonException {
        String jobID = "555";
        // very invalid info, no info at all
        Map<String, String> jobInfo = new HashMap<>();

        Map<String, Map<String, String>> input = new HashMap<>();
        input.put(jobID, jobInfo);

        SlurmUtils.getJobStatusFromSacctInfo(input, jobID);
    }

    @Test
    public void test03a_getJobStatusFromScontrolInfo_CompletedJob_JobStatus() throws XenonException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("JobId", jobID);
        jobInfo.put("JobState", "COMPLETED");
        jobInfo.put("ExitCode", "5:0");
        jobInfo.put("Reason", "None");

        JobStatus result = SlurmUtils.getJobStatusFromScontrolInfo(jobInfo, jobID);

        assertEquals(jobID, result.getJobIdentifier());
        assertEquals("COMPLETED", result.getState());
        assertEquals(new Integer(5), result.getExitCode());
        assertFalse(result.hasException());
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test03b_getJobStatusFromScontrolInfo_RunningJob_JobStatus() throws XenonException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("JobId", jobID);
        jobInfo.put("JobState", "RUNNING");
        jobInfo.put("ExitCode", "0:0");
        jobInfo.put("Reason", "None");

        JobStatus result = SlurmUtils.getJobStatusFromScontrolInfo(jobInfo, jobID);

        assertEquals(jobID, result.getJobIdentifier());
        assertEquals("RUNNING", result.getState());
        assertEquals(new Integer(0), result.getExitCode());
        assertFalse(result.hasException());
        assertTrue(result.isRunning());
        assertFalse(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test03c_getJobStatusFromScontrolInfo_CanceledJob_JobStatusWithException() throws XenonException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("JobId", jobID);
        jobInfo.put("JobState", "CANCELLED");
        jobInfo.put("ExitCode", "0:0");
        jobInfo.put("Reason", "None");

        JobStatus result = SlurmUtils.getJobStatusFromScontrolInfo(jobInfo, jobID);

        assertEquals(jobID, result.getJobIdentifier());
        assertEquals("CANCELLED", result.getState());
        assertEquals(new Integer(0), result.getExitCode());
        assertTrue(result.hasException());
        assertTrue(result.getException() instanceof JobCanceledException);
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test03d_getJobStatusFromScontrolInfo_JobWithNonZeroExitCode_JobStatusWithNoException() throws XenonException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("JobId", jobID);
        jobInfo.put("JobState", "FAILED");
        jobInfo.put("ExitCode", "11:0");
        jobInfo.put("Reason", "NonZeroExitCode");

        JobStatus result = SlurmUtils.getJobStatusFromScontrolInfo(jobInfo, jobID);

        assertEquals(jobID, result.getJobIdentifier());
        assertEquals("FAILED", result.getState());
        assertEquals(new Integer(11), result.getExitCode());
        assertFalse(result.hasException());
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test03e_getJobStatusFromScontrolInfo_FailedJob_JobStatusWithException() throws XenonException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("JobId", jobID);
        jobInfo.put("JobState", "FAILED");
        jobInfo.put("ExitCode", "4:0");
        jobInfo.put("Reason", "SomethingWentWrongNoIdea");

        JobStatus result = SlurmUtils.getJobStatusFromScontrolInfo(jobInfo, jobID);

        assertEquals(jobID, result.getJobIdentifier());
        assertEquals("FAILED", result.getState());
        assertEquals(new Integer(4), result.getExitCode());
        assertTrue(result.hasException());
        assertTrue(result.getException() instanceof XenonException);
        assertFalse(result.getException() instanceof JobCanceledException);
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test03f_getJobStatusFromScontrolInfo_FailedJobWithNoReason_JobStatusWithException() throws XenonException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("JobId", jobID);
        jobInfo.put("JobState", "FAILED");
        jobInfo.put("ExitCode", "4:0");
        jobInfo.put("Reason", "None");

        JobStatus result = SlurmUtils.getJobStatusFromScontrolInfo(jobInfo, jobID);

        assertEquals(jobID, result.getJobIdentifier());
        assertEquals("FAILED", result.getState());
        assertEquals(new Integer(4), result.getExitCode());
        assertTrue(result.hasException());
        assertTrue(result.getException() instanceof XenonException);
        assertFalse(result.getException() instanceof JobCanceledException);
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test03g_getJobStatusFromScontrolInfo_NullInput_NullReturned() throws XenonException {
        String jobID = "555";

        JobStatus result = SlurmUtils.getJobStatusFromScontrolInfo(null, jobID);

        assertNull(result);
    }

    @Test(expected = XenonException.class)
    public void test03h_getJobStatusFromScontrolInfo_IncompleteJobInfo_ExceptionThrown() throws XenonException {
        String jobID = "555";
        // empty job info
        Map<String, String> jobInfo = new HashMap<>();

        SlurmUtils.getJobStatusFromScontrolInfo(jobInfo, jobID);
    }

    @Test
    public void test04a_getJobStatusFromSqueueInfo_PendingJob_JobStatus() throws XenonException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("JOBID", jobID);
        jobInfo.put("STATE", "PENDING");

        Map<String, Map<String, String>> input = new HashMap<>();
        input.put(jobID, jobInfo);
        JobStatus result = SlurmUtils.getJobStatusFromSqueueInfo(input, jobID);

        assertEquals(jobID, result.getJobIdentifier());
        assertEquals("PENDING", result.getState());
        assertNull(result.getExitCode());
        assertFalse(result.hasException());
        assertFalse(result.isRunning());
        assertFalse(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test04b_getJobStatusFromSqueueInfo_RunningJob_JobStatus() throws XenonException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("JOBID", jobID);
        jobInfo.put("STATE", "RUNNING");

        Map<String, Map<String, String>> input = new HashMap<>();
        input.put(jobID, jobInfo);
        JobStatus result = SlurmUtils.getJobStatusFromSqueueInfo(input, jobID);

        assertEquals(jobID, result.getJobIdentifier());
        assertEquals("RUNNING", result.getState());
        assertNull(result.getExitCode());
        assertFalse(result.hasException());
        assertTrue(result.isRunning());
        assertFalse(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test04c_getJobStatusFromSqueueInfo_JobNotInMap_NullReturned() throws XenonException {
        String jobID = "555";
        Map<String, Map<String, String>> input = new HashMap<>();
        JobStatus result = SlurmUtils.getJobStatusFromSqueueInfo(input, jobID);

        assertNull(result);
    }

    @Test(expected = XenonException.class)
    public void test04d_getJobStatusFromSqueueInfo_IncompleteJobInfo_ExceptionThrown() throws XenonException {
        String jobID = "555";

        // very incomplete job info
        Map<String, String> jobInfo = new HashMap<>();

        Map<String, Map<String, String>> input = new HashMap<>();
        input.put(jobID, jobInfo);

        SlurmUtils.getJobStatusFromSqueueInfo(input, jobID);
    }

    @Test
    public void test05a_getQueueStatusFromSInfo_CorrectInfo_Result() {
        String queueName = "some.q";

        Scheduler scheduler = new MockScheduler("0", "TEST", "MEM", true, true, true, null);

        Map<String, String> queueInfo = new HashMap<>();

        queueInfo.put("MaxUsers", "5");
        queueInfo.put("Nodes", "23");

        Map<String, Map<String, String>> input = new HashMap<>();
        input.put(queueName, queueInfo);

        QueueStatus result = SlurmUtils.getQueueStatusFromSInfo(input, queueName, scheduler);

        assertNotNull(result);
        assertEquals(queueName, result.getQueueName());
        assertEquals(queueInfo, result.getSchedulerSpecificInformation());
        assertEquals(scheduler, result.getScheduler());
    }

    @Test
    public void test05b_getQueueStatusFromSInfo_QueueNotInInfo_NullReturned() {
        String queueName = "some.q";

        Scheduler scheduler = new MockScheduler("0", "TEST", "MEM", true, true, true, null);

        Map<String, Map<String, String>> input = new HashMap<>();

        QueueStatus result = SlurmUtils.getQueueStatusFromSInfo(input, queueName, scheduler);

        assertNull(result);
    }

    @Test
    public void test06_isDoneState() {
        assertTrue(SlurmUtils.isDoneOrFailedState("COMPLETED"));

        assertTrue(SlurmUtils.isDoneOrFailedState("FAILED"));

        assertFalse(SlurmUtils.isDoneOrFailedState("SOMERANDOMSTATE"));

        assertFalse(SlurmUtils.isDoneOrFailedState("RUNNING"));
    }

    @Test
    public void test07_isFailedState() {
        assertFalse(SlurmUtils.isFailedState("COMPLETED"));

        assertTrue(SlurmUtils.isFailedState("FAILED"));

        assertFalse(SlurmUtils.isFailedState("SOMERANDOMSTATE"));

        assertFalse(SlurmUtils.isFailedState("RUNNING"));
    }

    @Test
    public void test08a_verifyJobDescription_ValidJobDescription_NoException() throws Exception {
        JobDescription description = new JobDescription();

        // all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(1);
        description.setProcessesPerNode(1);
        description.setMaxRuntime(1);
        // slurm specific info
        SlurmUtils.verifyJobDescription(description, false);
    }

    @Test
    public void test08b_verifyJobDescription_ScriptOptionSet_NoException() throws Exception {
        JobDescription description = new JobDescription();

        // all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(1);
        description.setProcessesPerNode(1);
        description.setMaxRuntime(1);
        // slurm specific info
        description.addJobOption(SlurmUtils.JOB_OPTION_JOB_SCRIPT, "some.script");

        SlurmUtils.verifyJobDescription(description, false);
    }

    @Test
    public void test08c_verifyJobDescription_JobScriptSet_NoFurtherChecking() throws Exception {
        JobDescription description = new JobDescription();

        // set a job option
        description.addJobOption(SlurmUtils.JOB_OPTION_JOB_SCRIPT, "some.script");

        // All these settings are wrong. This should not lead to an error
        description.setExecutable(null);
        description.setNodeCount(0);
        description.setProcessesPerNode(0);
        description.setMaxRuntime(0);

        SlurmUtils.verifyJobDescription(description, false);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test08d_verifyJobDescription_InvalidOptions_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        // set a job option
        description.addJobOption("wrong.setting", "wrong.value");

        SlurmUtils.verifyJobDescription(description, false);
    }

    // @Test(expected = InvalidJobDescriptionException.class)
    // public void test08e_verifyJobDescription_InteractiveJob_ExceptionThrown() throws Exception {
    // JobDescription description = new JobDescription();
    //
    // description.setInteractive(true);
    //
    // SlurmSchedulerConnection.verifyJobDescription(description);
    // }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test08f_verifyJobDescription_InvalidStandardSetting_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        // verify the standard settings are also checked
        description.setExecutable("bin/bla");
        description.setMaxRuntime(0);

        SlurmUtils.verifyJobDescription(description, false);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_verifyJobDescriptionInteractive_FailsScriptOptionSet() throws Exception {
        JobDescription description = new JobDescription();

        // all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(1);
        description.setProcessesPerNode(1);
        description.setMaxRuntime(1);
        // slurm specific info
        description.addJobOption(SlurmUtils.JOB_OPTION_JOB_SCRIPT, "some.script");

        SlurmUtils.verifyJobDescription(description, true);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_verifyJobDescriptionInteractive_FailsSingleProcess() throws Exception {
        JobDescription description = new JobDescription();

        // all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(1);
        description.setProcessesPerNode(1);
        description.setMaxRuntime(1);
        // slurm specific info
        description.setStartSingleProcess(true);

        SlurmUtils.verifyJobDescription(description, true);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_verifyJobDescriptionInteractive__FailsStdinSet() throws Exception {
        JobDescription description = new JobDescription();

        // all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(1);
        description.setProcessesPerNode(1);
        description.setMaxRuntime(1);
        // slurm specific info
        description.setStdin("stdin.txt");

        SlurmUtils.verifyJobDescription(description, true);
    }

    @Test
    public void test_verifyJobDescriptionInteractive__StdoutSet() throws Exception {
        JobDescription description = new JobDescription();

        // all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(1);
        description.setProcessesPerNode(1);
        description.setMaxRuntime(1);
        // slurm specific info
        description.setStdout("stdout.txt");

        SlurmUtils.verifyJobDescription(description, true);
    }

    @Test
    public void test_verifyJobDescriptionInteractive__StderrSet() throws Exception {
        JobDescription description = new JobDescription();

        // all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(1);
        description.setProcessesPerNode(1);
        description.setMaxRuntime(1);
        // slurm specific info
        description.setStderr("stderr.txt");

        SlurmUtils.verifyJobDescription(description, true);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_verifyJobDescriptionInteractive__FailsStdoutSet() throws Exception {
        JobDescription description = new JobDescription();

        // all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(1);
        description.setProcessesPerNode(1);
        description.setMaxRuntime(1);
        // slurm specific info
        description.setStdout("foobar.txt");

        SlurmUtils.verifyJobDescription(description, true);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_verifyJobDescriptionInteractive__FailsStderrSet() throws Exception {
        JobDescription description = new JobDescription();

        // all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(1);
        description.setProcessesPerNode(1);
        description.setMaxRuntime(1);
        // slurm specific info
        description.setStderr("foobar.txt");

        SlurmUtils.verifyJobDescription(description, true);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_verifyJobDescriptionInteractive__FailsEnvSet() throws Exception {
        JobDescription description = new JobDescription();

        // all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(1);
        description.setProcessesPerNode(1);
        description.setMaxRuntime(1);
        // slurm specific info

        HashMap<String, String> env = new HashMap<>();
        env.put("key", "value");

        description.setEnvironment(env);

        SlurmUtils.verifyJobDescription(description, true);
    }

    @Test
    public void test_generateInterActiveArguments() {
        Path entry = new Path("/entry");
        UUID tag = new UUID(0, 42);

        JobDescription description = new JobDescription();
        description.setExecutable("exec");
        description.setArguments(new String[] { "a", "b", "c" });

        String[] expected = new String[] { "--quiet", "--job-name=" + tag.toString(), "--nodes=1", "--ntasks-per-node=1", "--time=15", "exec", "a", "b", "c" };

        String[] result = SlurmUtils.generateInteractiveArguments(description, entry, tag);

        assertArrayEquals(expected, result);
    }

    @Test
    public void test_generateInterActiveArgumentsWithRelativeDirAndQueue() {
        Path entry = new Path("/entry");
        UUID tag = new UUID(0, 42);

        JobDescription description = new JobDescription();
        description.setExecutable("exec");
        description.setArguments(new String[] { "a", "b", "c" });
        description.setWorkingDirectory("workdir");
        description.setQueueName("queue");

        String[] expected = new String[] { "--quiet", "--job-name=" + tag, "--chdir=" + entry.resolve("workdir"), "--partition=queue", "--nodes=1",
                "--ntasks-per-node=1", "--time=15", "exec", "a", "b", "c" };

        String[] result = SlurmUtils.generateInteractiveArguments(description, entry, tag);

        assertArrayEquals(expected, result);
    }

    @Test
    public void test_generateInterActiveArgumentsWithAbsoluteDirAndQueue() {
        Path entry = new Path("/entry");
        UUID tag = new UUID(0, 42);

        JobDescription description = new JobDescription();
        description.setExecutable("exec");
        description.setArguments(new String[] { "a", "b", "c" });
        description.setWorkingDirectory("/workdir");
        description.setQueueName("queue");

        String[] expected = new String[] { "--quiet", "--job-name=" + tag.toString(), "--chdir=/workdir", "--partition=queue", "--nodes=1",
                "--ntasks-per-node=1", "--time=15", "exec", "a", "b", "c" };

        String[] result = SlurmUtils.generateInteractiveArguments(description, entry, tag);

        assertArrayEquals(expected, result);
    }

    @Test
    public void test_generateBasic() {
        Path entry = new Path("/entry");

        JobDescription description = new JobDescription();
        description.setExecutable("exec");
        description.setArguments(new String[] { "a", "b", "c" });

        String expected = "#!/bin/sh\n" + "#SBATCH --job-name xenon\n" + "#SBATCH --nodes=1\n" + "#SBATCH --ntasks-per-node=1\n" + "#SBATCH --time=15\n"
                + "#SBATCH --output=/dev/null\n" + "#SBATCH --error=/dev/null\n" + "\n" + "srun exec 'a' 'b' 'c'\n";

        String result = SlurmUtils.generate(description, entry);

        assertEquals(expected, result);
    }

    @Test
    public void test_generateComplex() {
        Path entry = new Path("/entry");

        JobDescription description = new JobDescription();
        description.setExecutable("exec");
        description.setArguments(new String[] { "a", "b", "c" });
        description.setWorkingDirectory("workdir");
        description.setStdin("in.txt");
        description.setStdout("out.txt");
        description.setStderr("err.txt");
        description.setQueueName("queue");

        LinkedHashMap<String, String> env = new LinkedHashMap<>();
        env.put("key1", "value1");
        env.put("key2", "value2");

        description.setEnvironment(env);
        description.setStartSingleProcess(true);

        String expected = "#!/bin/sh\n" + "#SBATCH --job-name xenon\n" + "#SBATCH --workdir='" + entry.resolve("workdir").toString() + "'\n"
                + "#SBATCH --partition=queue\n" + "#SBATCH --nodes=1\n" + "#SBATCH --ntasks-per-node=1\n" + "#SBATCH --time=15\n" + "#SBATCH --input='in.txt'\n"
                + "#SBATCH --output='out.txt'\n" + "#SBATCH --error='err.txt'\n" + "export key1=\"value1\"\n" + "export key2=\"value2\"\n" + "\n"
                + "exec 'a' 'b' 'c'\n";

        String result = SlurmUtils.generate(description, entry);

        assertEquals(expected, result);
    }

    @Test
    public void test_isPendingStateTrue() {
        assertTrue(SlurmUtils.isPendingState("SUSPENDED"));
    }

    @Test
    public void test_isPendingStateFalse() {
        assertFalse(SlurmUtils.isPendingState("NO STATE OF MINE"));
    }

    @Test
    public void test_identifiersAsCSList() {

        String[] array = new String[] { "AAP", "NOOT" };

        String result = SlurmUtils.identifiersAsCSList(array);

        assertEquals("AAP,NOOT", result);
    }

    @Test
    public void test_identifiersAsCSListWithNull() {

        String[] array = new String[] { "AAP", "NOOT" };

        String result = SlurmUtils.identifiersAsCSList(array);

        assertEquals("AAP,NOOT", result);
    }

}
