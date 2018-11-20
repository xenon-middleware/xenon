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
package nl.esciencecenter.xenon.adaptors.schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.schedulers.IncompleteJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.NoSuchQueueException;

public class ScriptingUtilsTest {

    public ScriptingUtilsTest() {
        // TODO Auto-generated constructor stub
    }

    @Test
    public void test_create() throws XenonException {
        new ScriptingUtils();
    }

    @Test
    public void test_isLocal_null() throws XenonException {
        assertTrue(ScriptingUtils.isLocal(null));
    }

    @Test
    public void test_isLocal_empty() throws XenonException {
        assertTrue(ScriptingUtils.isLocal(""));
    }

    @Test
    public void test_isLocal_correct() throws XenonException {
        assertTrue(ScriptingUtils.isLocal("local://"));
    }

    @Test
    public void test_isLocal_false() throws XenonException {
        assertFalse(ScriptingUtils.isLocal("ssh://"));
    }

    @Test
    public void test_isSSH_correct() throws XenonException {
        assertTrue(ScriptingUtils.isSSH("ssh://"));
    }

    @Test
    public void test_isSSH_null() throws XenonException {
        assertFalse(ScriptingUtils.isSSH(null));
    }

    @Test
    public void test_isSSH_false() throws XenonException {
        assertFalse(ScriptingUtils.isSSH("local://"));
    }

    @Test
    public void test_mergeValidProperties_nothing() throws XenonException {
        XenonPropertyDescription[] result = ScriptingUtils.mergeValidProperties();
        assertNotNull(result);
        assertTrue(result.length == 0);
    }

    @Test
    public void test_mergeValidProperties_null() throws XenonException {
        XenonPropertyDescription[][] input = null;
        XenonPropertyDescription[] result = ScriptingUtils.mergeValidProperties(input);
        assertNotNull(result);
        assertTrue(result.length == 0);
    }

    @Test
    public void test_mergeValidProperties_empty() throws XenonException {
        XenonPropertyDescription[][] input = new XenonPropertyDescription[0][0];
        XenonPropertyDescription[] result = ScriptingUtils.mergeValidProperties(input);
        assertNotNull(result);
        assertTrue(result.length == 0);
    }

    @Test
    public void test_mergeValidProperties_mergeTwoIdentical() throws XenonException {

        XenonPropertyDescription p1 = new XenonPropertyDescription("p1", Type.SIZE, "42", "test property");
        XenonPropertyDescription p2 = new XenonPropertyDescription("p1", Type.SIZE, "42", "test property");

        XenonPropertyDescription[] input1 = new XenonPropertyDescription[] { p1 };
        XenonPropertyDescription[] input2 = new XenonPropertyDescription[] { p2 };

        XenonPropertyDescription[] result = ScriptingUtils.mergeValidProperties(input1, input2);
        assertNotNull(result);
        assertTrue(result.length == 2);
    }

    @Test
    public void test_mergeValidProperties_mergeTwoDifferent() throws XenonException {

        XenonPropertyDescription p1 = new XenonPropertyDescription("p1", Type.SIZE, "42", "test property");
        XenonPropertyDescription p2 = new XenonPropertyDescription("p2", Type.SIZE, "42", "test property");

        XenonPropertyDescription[] input1 = new XenonPropertyDescription[] { p1 };
        XenonPropertyDescription[] input2 = new XenonPropertyDescription[] { p2 };

        XenonPropertyDescription[] result = ScriptingUtils.mergeValidProperties(input1, input2);
        assertNotNull(result);
        assertTrue(result.length == 2);
    }

    @Test
    public void test_getProperties_local() throws XenonException {

        HashMap<String, String> props = new HashMap<>();

        XenonPropertyDescription p1 = new XenonPropertyDescription("p1", Type.SIZE, "42", "test property");

        XenonProperties result = ScriptingUtils.getProperties(new XenonPropertyDescription[] { p1 }, "local://", props);

        assertNotNull(result);
        assertEquals(42L, result.getSizeProperty("p1"));
        assertEquals(4, result.getIntegerProperty("xenon.adaptors.schedulers.local.queue.multi.maxConcurrentJobs"));
    }

    @Test
    public void test_getProperties_ssh() throws XenonException {

        HashMap<String, String> props = new HashMap<>();

        XenonPropertyDescription p1 = new XenonPropertyDescription("p1", Type.SIZE, "42", "test property");

        XenonProperties result = ScriptingUtils.getProperties(new XenonPropertyDescription[] { p1 }, "ssh://", props);

        assertNotNull(result);
        assertEquals(42L, result.getSizeProperty("p1"));
        assertEquals(false, result.getBooleanProperty("xenon.adaptors.schedulers.ssh.agentForwarding"));
    }

    @Test(expected = XenonException.class)
    public void test_startTime_invalid_null() throws XenonException {
        ScriptingUtils.verifyStartTime(null, "test");
    }

    @Test(expected = XenonException.class)
    public void test_startTime_invalid_empty() throws XenonException {
        ScriptingUtils.verifyStartTime("", "test");
    }

    @Test(expected = XenonException.class)
    public void test_startTime_invalid_whitespace() throws XenonException {
        ScriptingUtils.verifyStartTime(" ", "test");
    }

    @Test(expected = XenonException.class)
    public void test_startTime_invalid_unknown() throws XenonException {
        ScriptingUtils.verifyStartTime("hello", "test");
    }

    @Test
    public void test_startTime_valid_now() throws XenonException {
        ScriptingUtils.verifyStartTime("now", "test");
    }

    @Test
    public void test_startTime_valid_time() throws XenonException {
        ScriptingUtils.verifyStartTime("12:24", "test");
    }

    @Test
    public void test_startTime_valid_time_date() throws XenonException {
        ScriptingUtils.verifyStartTime("12:24 10.07", "test");
    }

    @Test
    public void test_startTime_valid_time_date_year() throws XenonException {
        ScriptingUtils.verifyStartTime("12:24 10.07.2018", "test");
    }

    @Test(expected = XenonException.class)
    public void test_startTime_invalid_time_date_year() throws XenonException {
        ScriptingUtils.verifyStartTime("12:24 10.07.aap", "test");
    }

    @Test(expected = XenonException.class)
    public void test_startTime_invalid_time_date() throws XenonException {
        ScriptingUtils.verifyStartTime("12:24 10.AA", "test");
    }

    @Test(expected = XenonException.class)
    public void test_startTime_invalid_time() throws XenonException {
        ScriptingUtils.verifyStartTime("12:ZZ", "test");
    }

    @Test
    public void test_getWorkingDirPath_null() throws XenonException {
        JobDescription job = new JobDescription();
        Path wd = new Path("/test");
        assertEquals("/test", ScriptingUtils.getWorkingDirPath(job, wd));
    }

    @Test
    public void test_getWorkingDirPath_empty() throws XenonException {
        JobDescription job = new JobDescription();
        job.setWorkingDirectory("");
        Path wd = new Path("/test");
        assertEquals("/test", ScriptingUtils.getWorkingDirPath(job, wd));
    }

    @Test
    public void test_getWorkingDirPath_whitespace() throws XenonException {
        JobDescription job = new JobDescription();
        job.setWorkingDirectory(" ");
        Path wd = new Path("/test");
        assertEquals("/test", ScriptingUtils.getWorkingDirPath(job, wd));
    }

    @Test
    public void test_getWorkingDirPath_absolute() throws XenonException {
        JobDescription job = new JobDescription();
        job.setWorkingDirectory("/wd");

        Path wd = new Path("/test");
        assertEquals("/wd", ScriptingUtils.getWorkingDirPath(job, wd));
    }

    @Test
    public void test_getWorkingDirPath_relative() throws XenonException {
        JobDescription job = new JobDescription();
        job.setWorkingDirectory("wd");

        Path wd = new Path("/test");
        assertEquals("/test/wd", ScriptingUtils.getWorkingDirPath(job, wd));
    }

    @Test
    public void test_checkQueue_null() throws XenonException {
        ScriptingUtils.checkQueue(null, null, "test");
    }

    @Test
    public void test_checkQueue_queues_null() throws XenonException {
        ScriptingUtils.checkQueue(null, "aap", "test");
    }

    @Test(expected = NoSuchQueueException.class)
    public void test_checkQueue_queues_empty() throws XenonException {
        ScriptingUtils.checkQueue(new String[] {}, "aap", "test");
    }

    @Test
    public void test_checkQueue_name_empty() throws XenonException {
        ScriptingUtils.checkQueue(new String[] {}, "", "test");
    }

    @Test
    public void test_checkQueue_name_null() throws XenonException {
        ScriptingUtils.checkQueue(new String[] {}, null, "test");
    }

    @Test
    public void test_checkQueue_correct() throws XenonException {
        ScriptingUtils.checkQueue(new String[] { "noot", "aap" }, "aap", "test");
    }

    @Test(expected = NoSuchQueueException.class)
    public void test_checkQueue_not_found() throws XenonException {
        ScriptingUtils.checkQueue(new String[] { "noot", "fiets" }, "aap", "test");
    }

    @Test(expected = IncompleteJobDescriptionException.class)
    public void test_verify_job_description_empty() throws XenonException {

        String[] queueNames = new String[] { "q" };
        JobDescription job = new JobDescription();

        ScriptingUtils.verifyJobDescription(job, queueNames, "test");
    }

    @Test
    public void test_verify_job_description_correct() throws XenonException {

        String[] queueNames = new String[] { "q" };
        JobDescription job = new JobDescription();
        job.setExecutable("test.exe");

        ScriptingUtils.verifyJobDescription(job, queueNames, "test");
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_verify_job_description_invalid_nodes() throws XenonException {

        String[] queueNames = new String[] { "q" };
        JobDescription job = new JobDescription();
        job.setExecutable("test.exe");
        job.setNodeCount(-2);

        ScriptingUtils.verifyJobDescription(job, queueNames, "test");
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_verify_job_description_invalid_processesPerNode() throws XenonException {

        String[] queueNames = new String[] { "q" };
        JobDescription job = new JobDescription();
        job.setExecutable("test.exe");
        job.setProcessesPerNode(-2);

        ScriptingUtils.verifyJobDescription(job, queueNames, "test");
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_verify_job_description_invalid_maxRuntime() throws XenonException {

        String[] queueNames = new String[] { "q" };
        JobDescription job = new JobDescription();
        job.setExecutable("test.exe");
        job.setMaxRuntime(-3);

        ScriptingUtils.verifyJobDescription(job, queueNames, "test");
    }

    @Test(expected = NoSuchQueueException.class)
    public void test_verify_job_description_invalid_queue() throws XenonException {

        String[] queueNames = new String[] { "q" };
        JobDescription job = new JobDescription();
        job.setExecutable("test.exe");
        job.setQueueName("b");

        ScriptingUtils.verifyJobDescription(job, queueNames, "test");
    }

    @Test(expected = XenonException.class)
    public void test_verify_job_description_invalid_startTime() throws XenonException {

        String[] queueNames = new String[] { "q" };
        JobDescription job = new JobDescription();
        job.setExecutable("test.exe");
        job.setStartTime("hello");

        ScriptingUtils.verifyJobDescription(job, queueNames, "test");
    }

    @Test
    public void test_verify_job_options_empty() throws XenonException {

        Map<String, String> options = new HashMap<>();
        String[] valid = new String[0];

        ScriptingUtils.verifyJobOptions(options, valid, "test");
    }

    @Test
    public void test_verify_job_options_found() throws XenonException {

        Map<String, String> options = new HashMap<>();
        options.put("opt1", "value1");
        String[] valid = new String[] { "opt1" };

        ScriptingUtils.verifyJobOptions(options, valid, "test");
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_verify_job_options_not_found() throws XenonException {

        Map<String, String> options = new HashMap<>();
        options.put("opt1", "value1");
        String[] valid = new String[] { "opt2" };

        ScriptingUtils.verifyJobOptions(options, valid, "test");
    }

    @Test(expected = XenonException.class)
    public void test_verify_job_info_null() throws XenonException {
        ScriptingUtils.verifyJobInfo(null, "42", "test", "jobID");
    }

    @Test(expected = XenonException.class)
    public void test_verify_job_info_empty() throws XenonException {

        Map<String, String> jobInfo = new HashMap<>();

        ScriptingUtils.verifyJobInfo(jobInfo, "42", "test", "jobID");
    }

    @Test
    public void test_verify_job_info_correct() throws XenonException {

        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("jobID", "42");

        ScriptingUtils.verifyJobInfo(jobInfo, "42", "test", "jobID");
    }

    @Test(expected = XenonException.class)
    public void test_verify_job_info_wrong_id() throws XenonException {

        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("jobID", "44");

        ScriptingUtils.verifyJobInfo(jobInfo, "42", "test", "jobID");
    }

    @Test(expected = XenonException.class)
    public void test_verify_job_info_missing_extras() throws XenonException {

        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("jobID", "42");

        ScriptingUtils.verifyJobInfo(jobInfo, "42", "test", "jobID", "test");
    }

    @Test
    public void test_verify_job_info_correct_with_extras() throws XenonException {

        Map<String, String> jobInfo = new HashMap<>();
        jobInfo.put("jobID", "42");
        jobInfo.put("test", "test");

        ScriptingUtils.verifyJobInfo(jobInfo, "42", "test", "jobID", "test");
    }

    @Test
    public void test_concat() throws XenonException {
        String result = ScriptingUtils.concat();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void test_concat_null() throws XenonException {
        String[] input = null;
        String result = ScriptingUtils.concat(input);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void test_concat_empty() throws XenonException {
        String[] input = new String[0];
        String result = ScriptingUtils.concat(input);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void test_concat_values() throws XenonException {
        String[] input = new String[] { "Hello", "World" };
        String result = ScriptingUtils.concat(input);
        assertNotNull(result);
        assertEquals("HelloWorld", result);
    }

    @Test
    public void test_concat_values_with_null() throws XenonException {
        String[] input = new String[] { "Hello", null, "World" };
        String result = ScriptingUtils.concat(input);
        assertNotNull(result);
        assertEquals("HelloWorld", result);
    }

    @Test
    public void test_concat_values_with_empty() throws XenonException {
        String[] input = new String[] { "Hello", "", "World" };
        String result = ScriptingUtils.concat(input);
        assertNotNull(result);
        assertEquals("HelloWorld", result);
    }

    @Test
    public void test_asCSList_empty() throws XenonException {
        String[] input = new String[0];
        String result = ScriptingUtils.asCSList(input);
        assertNull(result);
    }

    @Test
    public void test_asCSList_correct() throws XenonException {
        String[] input = new String[] { "Hello", "World" };
        String result = ScriptingUtils.asCSList(input);
        assertNotNull(result);
        assertEquals("Hello,World", result);
    }

    @Test
    public void test_asCSList_correct_with_null() throws XenonException {
        String[] input = new String[] { "Hello", null, "World" };
        String result = ScriptingUtils.asCSList(input);
        assertNotNull(result);
        assertEquals("Hello,World", result);
    }

    @Test
    public void test_protectAgainstShell() throws XenonException {
        String result = ScriptingUtils.protectAgainstShellMetas("hello");
        assertNotNull(result);
        assertEquals("'hello'", result);
    }

    @Test
    public void test_protectAgainstShellWithMeta() throws XenonException {
        String result = ScriptingUtils.protectAgainstShellMetas("hello's");
        assertNotNull(result);
        assertEquals("'hello'\\''s'", result);
    }

}
