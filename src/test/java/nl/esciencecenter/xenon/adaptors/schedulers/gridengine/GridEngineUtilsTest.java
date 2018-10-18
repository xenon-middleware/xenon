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
package nl.esciencecenter.xenon.adaptors.schedulers.gridengine;

import static nl.esciencecenter.xenon.adaptors.schedulers.gridengine.GridEngineSetupTest.getGridEngineSetup;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.JobCanceledException;
import nl.esciencecenter.xenon.schedulers.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GridEngineUtilsTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test01a_generate_EmptyDescription_Result() throws XenonException {
        JobDescription description = new JobDescription();

        String result = GridEngineUtils.generate(description, null, null);

        String expected = "#!/bin/sh\n" + "#$ -S /bin/sh\n" + "#$ -N xenon\n" + "#$ -l h_rt=00:15:00\n" + "#$ -o /dev/null\n" + "#$ -e /dev/null\n" + "\n"
                + "null\n";

        assertEquals(expected, result);
    }

    @Test
    public void test_generate_name() throws XenonException {
        JobDescription description = new JobDescription();

        description.setName("test");

        String result = GridEngineUtils.generate(description, null, null);

        String expected = "#!/bin/sh\n" + "#$ -S /bin/sh\n" + "#$ -N test\n" + "#$ -l h_rt=00:15:00\n" + "#$ -o /dev/null\n" + "#$ -e /dev/null\n" + "\n"
                + "null\n";

        assertEquals(expected, result);
    }

    @Test
    public void test_generate_name_empty() throws XenonException {
        JobDescription description = new JobDescription();

        description.setName("");

        String result = GridEngineUtils.generate(description, null, null);

        String expected = "#!/bin/sh\n" + "#$ -S /bin/sh\n" + "#$ -N xenon\n" + "#$ -l h_rt=00:15:00\n" + "#$ -o /dev/null\n" + "#$ -e /dev/null\n" + "\n"
                + "null\n";

        assertEquals(expected, result);
    }

    @Test
    public void test_generate_memory() throws XenonException {
        JobDescription description = new JobDescription();

        description.setMaxMemory(1024);

        String result = GridEngineUtils.generate(description, null, null);

        String expected = "#!/bin/sh\n" + "#$ -S /bin/sh\n" + "#$ -N xenon\n" + "#$ -l h_rt=00:15:00\n" + "#$ -l mem_free=1024M,h_vmem=1024M\n"
                + "#$ -o /dev/null\n" + "#$ -e /dev/null\n" + "\n" + "null\n";

        assertEquals(expected, result);
    }

    @Test
    public void test_generate_schedulerArguments() throws XenonException {
        JobDescription description = new JobDescription();

        description.setSchedulerArguments("-l gpu=1");

        String result = GridEngineUtils.generate(description, null, null);

        String expected = "#!/bin/sh\n" + "#$ -S /bin/sh\n" + "#$ -N xenon\n" + "#$ -l h_rt=00:15:00\n" + "#$ -l gpu=1\n" + "#$ -o /dev/null\n"
                + "#$ -e /dev/null\n" + "\n" + "null\n";

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
        description.addJobOption(GridEngineUtils.JOB_OPTION_RESOURCES, "list-of-resources");
        description.setExecutable("/bin/executable");
        description.setMaxRuntime(100);
        description.setNodeCount(1);
        description.setProcessesPerNode(1);
        description.setQueueName("the.queue");
        description.setStderr("stderr.file");
        description.setStdin("stdin.file");
        description.setStdout("stdout.file");
        description.setWorkingDirectory("/some/working/directory");

        String result = GridEngineUtils.generate(description, null, null);

        String expected = "#!/bin/sh\n" + "#$ -S /bin/sh\n" + "#$ -N xenon\n" + "#$ -wd '/some/working/directory'\n" + "#$ -q the.queue\n"
                + "#$ -l h_rt=01:40:00\n" + "#$ -l list-of-resources\n" + "#$ -i 'stdin.file'\n" + "#$ -o 'stdout.file'\n" + "#$ -e 'stderr.file'\n"
                + "export some.more=\"environment value with spaces\"\n" + "\n" + "/bin/executable 'some' 'arguments'\n";

        assertEquals(expected, result);
    }

    @Test
    /**
     * Check to see if the output is _exactly_ what we expect, and not a single char different.
     *
     * @throws XenonException
     */
    public void test01c_generate__MultinodeDescription_Result() throws XenonException {
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/executable");
        description.setArguments("some", "arguments");
        description.setMaxRuntime(100);
        description.setNodeCount(4);
        description.setProcessesPerNode(10);
        description.setQueueName("some.q");
        description.setStderr("stderr.file");
        description.setStdin("stdin.file");
        description.setStdout("stdout.file");
        description.setWorkingDirectory("/some/working/directory");

        ParallelEnvironmentInfo pe = new ParallelEnvironmentInfo("some.pe", 100, ParallelEnvironmentInfo.AllocationRule.INTEGER, 10);
        GridEngineSetup setup = getGridEngineSetup(pe);

        String result = GridEngineUtils.generate(description, null, setup);

        String expected = "#!/bin/sh\n" + "#$ -S /bin/sh\n" + "#$ -N xenon\n" + "#$ -wd '/some/working/directory'\n" + "#$ -q some.q\n"
                + "#$ -pe some.pe 40\n" + "#$ -l h_rt=01:40:00\n" + "#$ -i 'stdin.file'\n" + "#$ -o 'stdout.file'\n" + "#$ -e 'stderr.file'\n" + "\n"
                + "for host in `cat $PE_HOSTFILE | cut -d \" \" -f 1` ; do\n"
                + "  ssh -o StrictHostKeyChecking=false $host \"cd `pwd` && /bin/executable 'some' 'arguments'\"&\n"
                + "  ssh -o StrictHostKeyChecking=false $host \"cd `pwd` && /bin/executable 'some' 'arguments'\"&\n"
                + "  ssh -o StrictHostKeyChecking=false $host \"cd `pwd` && /bin/executable 'some' 'arguments'\"&\n"
                + "  ssh -o StrictHostKeyChecking=false $host \"cd `pwd` && /bin/executable 'some' 'arguments'\"&\n"
                + "  ssh -o StrictHostKeyChecking=false $host \"cd `pwd` && /bin/executable 'some' 'arguments'\"&\n"
                + "  ssh -o StrictHostKeyChecking=false $host \"cd `pwd` && /bin/executable 'some' 'arguments'\"&\n"
                + "  ssh -o StrictHostKeyChecking=false $host \"cd `pwd` && /bin/executable 'some' 'arguments'\"&\n"
                + "  ssh -o StrictHostKeyChecking=false $host \"cd `pwd` && /bin/executable 'some' 'arguments'\"&\n"
                + "  ssh -o StrictHostKeyChecking=false $host \"cd `pwd` && /bin/executable 'some' 'arguments'\"&\n"
                + "  ssh -o StrictHostKeyChecking=false $host \"cd `pwd` && /bin/executable 'some' 'arguments'\"&\n" + "done\n" + "\n" + "wait\n"
                + "exit 0\n\n";

        assertEquals(expected, result);
    }

    @Test
    /**
     * Check to see if the output is _exactly_ what we expect, and not a single char different.
     *
     * @throws XenonException
     */
    public void test01c_generate__ThreadsDescription_Result() throws XenonException {
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/executable");
        description.setArguments("some", "arguments");
        description.setMaxRuntime(100);
        description.setNodeCount(1);
        description.setProcessesPerNode(10);
        description.setQueueName("some.q");
        description.setStderr("stderr.file");
        description.setStdin("stdin.file");
        description.setStdout("stdout.file");
        description.setWorkingDirectory("/some/working/directory");
        description.setStartSingleProcess(true);

        ParallelEnvironmentInfo pe = new ParallelEnvironmentInfo("some.pe", 100, ParallelEnvironmentInfo.AllocationRule.INTEGER, 10);
        GridEngineSetup setup = getGridEngineSetup(pe);

        String result = GridEngineUtils.generate(description, null, setup);

        String expected = "#!/bin/sh\n" + "#$ -S /bin/sh\n" + "#$ -N xenon\n" + "#$ -wd '/some/working/directory'\n" + "#$ -q some.q\n"
            + "#$ -pe some.pe 10\n" + "#$ -l h_rt=01:40:00\n" + "#$ -i 'stdin.file'\n" + "#$ -o 'stdout.file'\n" + "#$ -e 'stderr.file'\n" + "\n"
            + "/bin/executable 'some' 'arguments'\n";

        assertEquals(expected, result);
    }

    @Test
    public void test02a__generate_peAsSchedulerArgument() throws XenonException {
        JobDescription description = new JobDescription();
        description.addSchedulerArgument("-pe some.pe 5");

        String expected = "#$ -pe some.pe 5\n";

        String script = GridEngineUtils.generate(description, null, null);

        assertThat(script, containsString(expected));
    }

    @Test
    public void testGenerate_PeNotFoundForMultiNode() throws XenonException {
        thrown.expect(InvalidJobDescriptionException.class);
        thrown.expectMessage("Unable to find a parallel environment for multiple nodes, replace node count and cores per node with scheduler.addSchedulerArgument(\"-pe <name of parallel environment (qconf -spl)> <number of slots>\")");

        JobDescription description = new JobDescription();
        description.setNodeCount(10);

        ParallelEnvironmentInfo pe = new ParallelEnvironmentInfo("some.pe", 100, ParallelEnvironmentInfo.AllocationRule.FILL_UP, 0);
        GridEngineSetup setup = getGridEngineSetup(pe);

        GridEngineUtils.generate(description, null, setup);
    }

    @Test
    public void testGenerate_PeNotFoundForSingleNode() throws XenonException {
        thrown.expect(InvalidJobDescriptionException.class);
        thrown.expectMessage("Unable to find a parallel environment for multi core on single node, replace node count and cores per node with scheduler.addSchedulerArgument(\"-pe <name of parallel environment (qconf -spl)> <number of slots>\")");

        JobDescription description = new JobDescription();
        description.setProcessesPerNode(10);

        ParallelEnvironmentInfo pe = new ParallelEnvironmentInfo("some.pe", 100, ParallelEnvironmentInfo.AllocationRule.FILL_UP, 0);
        GridEngineSetup setup = getGridEngineSetup(pe);

        GridEngineUtils.generate(description, null, setup);
    }


    @Test
    public void test03a_generateSerialScriptContent() {
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/executable");
        description.setArguments("some", "arguments");

        Formatter output = new Formatter();

        String expected = "/bin/executable 'some' 'arguments'\n";

        GridEngineUtils.generateSerialScriptContent(description, output);

        assertEquals("serial script content incorrect", expected, output.out().toString());
    }

    @Test
    public void test04a_generateParallelScriptContent() {
        JobDescription description = new JobDescription();
        description.setProcessesPerNode(2);
        description.setExecutable("/bin/executable");
        description.setArguments("some", "arguments");

        Formatter output = new Formatter();

        String expected = "for host in `cat $PE_HOSTFILE | cut -d \" \" -f 1` ; do\n"
                + "  ssh -o StrictHostKeyChecking=false $host \"cd `pwd` && /bin/executable 'some' 'arguments'\"&\n"
                + "  ssh -o StrictHostKeyChecking=false $host \"cd `pwd` && /bin/executable 'some' 'arguments'\"&\n" + "done\n" + "\n" + "wait\n"
                + "exit 0\n\n";

        GridEngineUtils.generateParallelScriptContent(description, output);

        assertEquals("parallel script content incorrect", expected, output.out().toString());
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

        GridEngineUtils.verifyJobDescription(description);
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
        description.addJobOption(GridEngineUtils.JOB_OPTION_JOB_SCRIPT, "some.script");

        GridEngineUtils.verifyJobDescription(description);
    }

    @Test
    public void test01c_verifyJobDescription_JobScriptSet_NoFurtherChecking() throws Exception {
        JobDescription description = new JobDescription();

        // set a job option
        description.addJobOption(GridEngineUtils.JOB_OPTION_JOB_SCRIPT, "some.script");

        // All these settings are wrong. This should not lead to an error
        description.setExecutable(null);
        description.setNodeCount(0);
        description.setProcessesPerNode(0);
        description.setMaxRuntime(0);
        // GridEngine specific info

        GridEngineUtils.verifyJobDescription(description);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test01d_verifyJobDescription_InvalidOptions_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        // set a job option
        description.addJobOption("wrong.setting", "wrong.value");

        GridEngineUtils.verifyJobDescription(description);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test01f_verifyJobDescription_InvalidStandardSetting_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        // verify the standard settings are also checked
        description.setExecutable("bin/bla");
        description.setMaxRuntime(0);

        GridEngineUtils.verifyJobDescription(description);
    }

    @Test
    public void test01g_verifyJobDescription_ValidParallelJobDescriptionWithQueue_NoException() throws Exception {
        JobDescription description = new JobDescription();

        // all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(2);
        description.setProcessesPerNode(2);
        description.setMaxRuntime(1);
        description.setQueueName("some.queue");

        GridEngineUtils.verifyJobDescription(description);
    }

    @Test()
    public void test01l_verifyJobDescription_StringProcessOption_NoException() throws Exception {
        JobDescription description = new JobDescription();

        description.setExecutable("/bin/nothing");
        description.setStartSingleProcess(true);

        GridEngineUtils.verifyJobDescription(description);
    }

    @Test
    public void test03a_getJobStatusFromQacctInfo_doneJob_JobStatus() throws XenonException {
        String jobnumber = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("jobnumber", jobnumber);
        jobInfo.put("jobname", "test");
        jobInfo.put("exit_status", "5");
        jobInfo.put("failed", "0");

        JobStatus result = GridEngineUtils.getJobStatusFromQacctInfo(jobInfo, jobnumber);

        assertEquals(jobnumber, result.getJobIdentifier());
        assertEquals("test", result.getName());
        assertEquals("done", result.getState());
        assertEquals(new Integer(5), result.getExitCode());
        assertFalse(result.hasException());
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test03b_getJobStatusFromQacctInfo_CanceledJob_JobStatusWithException() throws XenonException {
        String jobnumber = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("jobnumber", jobnumber);
        jobInfo.put("jobname", "test");
        jobInfo.put("exit_status", "0");
        jobInfo.put("failed", "100: This job was canceled");

        JobStatus result = GridEngineUtils.getJobStatusFromQacctInfo(jobInfo, jobnumber);

        assertEquals(jobnumber, result.getJobIdentifier());
        assertEquals("test", result.getName());
        assertEquals("done", result.getState());
        assertEquals(new Integer(0), result.getExitCode());
        assertTrue(result.hasException());
        assertTrue(result.getException() instanceof JobCanceledException);
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test03c_getJobStatusFromQacctInfo_JobWithNonZeroexit_status_JobStatusWithNoException() throws XenonException {
        String jobnumber = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("jobnumber", jobnumber);
        jobInfo.put("jobname", "test");
        jobInfo.put("exit_status", "11");
        jobInfo.put("failed", "0");

        JobStatus result = GridEngineUtils.getJobStatusFromQacctInfo(jobInfo, jobnumber);

        assertEquals(jobnumber, result.getJobIdentifier());
        assertEquals("test", result.getName());
        assertEquals("done", result.getState());
        assertEquals(new Integer(11), result.getExitCode());
        assertFalse(result.hasException());
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test03d_getJobStatusFromQacctInfo_FailedJob_JobStatusWithException() throws XenonException {
        String jobnumber = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("jobnumber", jobnumber);
        jobInfo.put("jobname", "test");
        jobInfo.put("exit_status", "4");
        jobInfo.put("failed", "666: SomethingWentWrongNoIdea");

        JobStatus result = GridEngineUtils.getJobStatusFromQacctInfo(jobInfo, jobnumber);

        assertEquals(jobnumber, result.getJobIdentifier());
        assertEquals("test", result.getName());
        assertEquals("done", result.getState());
        assertEquals(new Integer(4), result.getExitCode());
        assertTrue(result.hasException());
        assertTrue(result.getException() instanceof XenonException);
        assertFalse(result.getException() instanceof JobCanceledException);
        assertFalse(result.isRunning());
        assertTrue(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test03e_getJobStatusFromQacctInfo_NullInput_NullReturned() throws XenonException {
        String jobnumber = "555";

        JobStatus result = GridEngineUtils.getJobStatusFromQacctInfo(null, jobnumber);

        assertNull(result);
    }

    @Test(expected = XenonException.class)
    public void test03f_getJobStatusFromQacctInfo_IncompleteJobInfo_ExceptionThrown() throws XenonException {
        String jobnumber = "555";
        // empty job info
        Map<String, String> jobInfo = new HashMap<>();

        GridEngineUtils.getJobStatusFromQacctInfo(jobInfo, jobnumber);
    }

    @Test(expected = XenonException.class)
    public void test03g_getJobStatusFromQacctInfo_ExitCodeNotANumber_ExceptionThrown() throws XenonException {
        String jobnumber = "555";
        // empty job info
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("jobnumber", jobnumber);
        jobInfo.put("jobname", "test");
        jobInfo.put("exit_status", "four");
        jobInfo.put("failed", "0");

        GridEngineUtils.getJobStatusFromQacctInfo(jobInfo, jobnumber);
    }

    @Test
    public void test04a_getJobStatusFromQstatInfo_PendingJob_JobStatus() throws XenonException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("JB_job_number", jobID);
        jobInfo.put("JB_name", "test");
        jobInfo.put("state", "qw");
        jobInfo.put("long_state", "pending");

        Map<String, Map<String, String>> input = new HashMap<>();
        input.put(jobID, jobInfo);

        JobStatus result = GridEngineUtils.getJobStatusFromQstatInfo(input, jobID);

        assertEquals(jobID, result.getJobIdentifier());
        assertEquals("test", result.getName());
        assertEquals("pending", result.getState());
        assertNull(result.getExitCode());
        assertFalse(result.hasException());
        assertFalse(result.isRunning());
        assertFalse(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test04b_getJobStatusFromQstatInfo_RunningJob_JobStatus() throws XenonException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("JB_job_number", jobID);
        jobInfo.put("JB_name", "test");
        jobInfo.put("state", "r");
        jobInfo.put("long_state", "running");

        Map<String, Map<String, String>> input = new HashMap<>();
        input.put(jobID, jobInfo);

        JobStatus result = GridEngineUtils.getJobStatusFromQstatInfo(input, jobID);

        assertEquals(jobID, result.getJobIdentifier());
        assertEquals("test", result.getName());
        assertEquals("running", result.getState());
        assertNull(result.getExitCode());
        assertFalse(result.hasException());
        assertTrue(result.isRunning());
        assertFalse(result.isDone());
        assertEquals(jobInfo, result.getSchedulerSpecificInformation());
    }

    @Test
    public void test04c_getJobStatusFromQstatInfo_ErrorJob_JobStatusWithExcepion() throws XenonException {
        String jobID = "555";
        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("JB_job_number", jobID);
        jobInfo.put("JB_name", "test");
        jobInfo.put("state", "qEw");
        jobInfo.put("long_state", "error");

        Map<String, Map<String, String>> input = new HashMap<>();
        input.put(jobID, jobInfo);

        JobStatus result = GridEngineUtils.getJobStatusFromQstatInfo(input, jobID);

        assertEquals(jobID, result.getJobIdentifier());
        assertEquals("test", result.getName());
        assertEquals("error", result.getState());
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
        String jobID = "555";
        Map<String, Map<String, String>> input = new HashMap<>();
        JobStatus result = GridEngineUtils.getJobStatusFromQstatInfo(input, jobID);

        assertNull(result);
    }

    @Test(expected = XenonException.class)
    public void test04e_getJobStatusFromQstatInfo_IncompleteJobInfo_ExceptionThrown() throws XenonException {
        String jobID = "555";

        // very incomplete job info
        Map<String, String> jobInfo = new HashMap<>();

        Map<String, Map<String, String>> input = new HashMap<>();
        input.put(jobID, jobInfo);

        GridEngineUtils.getJobStatusFromQstatInfo(input, jobID);
    }

}
