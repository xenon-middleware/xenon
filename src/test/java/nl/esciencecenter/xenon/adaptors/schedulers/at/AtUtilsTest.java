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
package nl.esciencecenter.xenon.adaptors.schedulers.at;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.schedulers.IncompleteJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.JobDescription;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AtUtilsTest {

    @Test
    public void test_constructor() {
        // Dummy for coverage
        new AtUtils();
    }

    @Test
    public void test_parser_jobLine_null() {
        HashMap<String, Map<String, String>> result = new HashMap<>();
        AtUtils.parseJobLine(null, null, result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void test_parser_jobLine_empty() {
        HashMap<String, Map<String, String>> result = new HashMap<>();
        AtUtils.parseJobLine(null, null, result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void test_parser_jobLine_queues_null() {
        String line = "11 Mon Jul 2 10:22:00 2018 = jason";
        HashMap<String, Map<String, String>> result = new HashMap<>();
        AtUtils.parseJobLine(line, null, result);
        assertTrue(result.size() == 1);
    }

    @Test
    public void test_parser_jobLine_queues_empty() {
        String line = "11 Mon Jul 2 10:22:00 2018 = jason";
        HashMap<String, Map<String, String>> result = new HashMap<>();
        Set<String> queues = new HashSet<>();
        AtUtils.parseJobLine(line, queues, result);
        assertTrue(result.size() == 1);
    }

    @Test
    public void test_parser_jobLine_queues_wrongQ() {
        String line = "11 Mon Jul 2 10:22:00 2018 = jason";
        HashMap<String, Map<String, String>> result = new HashMap<>();
        Set<String> queues = new HashSet<>();
        queues.add("a");
        AtUtils.parseJobLine(line, queues, result);
        assertTrue(result.size() == 0);
    }

    @Test
    public void test_parser_jobLine_queues_rightQ() {
        String line = "11 Mon Jul 2 10:22:00 2018 = jason";
        HashMap<String, Map<String, String>> result = new HashMap<>();
        Set<String> queues = new HashSet<>();
        queues.add("=");
        AtUtils.parseJobLine(line, queues, result);
        assertTrue(result.size() == 1);
    }

    @Test
    public void test_atq_parser_null() {
        Map<String, Map<String, String>> result = AtUtils.parseJobInfo(null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void test_atq_parser_empty() {
        Map<String, Map<String, String>> result = AtUtils.parseJobInfo("", null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void test_atq_parser_whiteSpace() {
        Map<String, Map<String, String>> result = AtUtils.parseJobInfo("  ", null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void test_atq_parser_single_line() {
        String tmp = "11 Mon Jul 2 10:22:00 2018 = jason";

        Map<String, Map<String, String>> result = AtUtils.parseJobInfo(tmp, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("11"));

        Map<String, String> v = result.get("11");

        assertEquals("=", v.get("queue"));
        assertEquals("jason", v.get("user"));
        assertEquals("11", v.get("jobID"));
        assertEquals("Mon Jul 2 10:22:00 2018", v.get("startDate"));
    }

    @Test
    public void test_atq_parser_multi_line() {
        String tmp = "11 Mon Jul 2 10:22:00 2018 = jason\n16    Wed Jul  4 16:00:00 2018 a jason\n";

        Map<String, Map<String, String>> result = AtUtils.parseJobInfo(tmp, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("11"));
        assertTrue(result.containsKey("16"));
        assertEquals("=", result.get("11").get("queue"));
        assertEquals("a", result.get("16").get("queue"));
    }

    @Test
    public void test_atq_parser_multi_line_empty() {
        String tmp = "11 Mon Jul 2 10:22:00 2018 = jason\n\n\n16    Wed Jul  4 16:00:00 2018 a jason";

        Map<String, Map<String, String>> result = AtUtils.parseJobInfo(tmp, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("11"));
        assertTrue(result.containsKey("16"));
        assertEquals("=", result.get("11").get("queue"));
        assertEquals("a", result.get("16").get("queue"));
    }

    @Test
    public void test_atq_parser_multi_line_whitespace() {
        String tmp = "  \n11 Mon Jul 2 10:22:00 2018 = jason\n  \n  \n16    Wed Jul  4 16:00:00 2018 a jason\n  \n  \n";

        Map<String, Map<String, String>> result = AtUtils.parseJobInfo(tmp, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("11"));
        assertTrue(result.containsKey("16"));
        assertEquals("=", result.get("11").get("queue"));
        assertEquals("a", result.get("16").get("queue"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_atq_parser_invalid() {
        AtUtils.parseJobInfo("Hello World!", null);
    }

    @Test
    public void test_atq_parser_select_queue_empty() {
        String tmp = "11 Mon Jul 2 10:22:00 2018 = jason\n16    Wed Jul  4 16:00:00 2018 a jason\n";

        Set<String> queues = new HashSet<String>();

        Map<String, Map<String, String>> result = AtUtils.parseJobInfo(tmp, queues);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void test_atq_parser_select_queue_one() {
        String tmp = "11 Mon Jul 2 10:22:00 2018 = jason\n16    Wed Jul  4 16:00:00 2018 a jason\n";

        Set<String> queues = new HashSet<String>();
        queues.add("a");

        Map<String, Map<String, String>> result = AtUtils.parseJobInfo(tmp, queues);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("16"));
        assertEquals("a", result.get("16").get("queue"));
    }

    @Test
    public void test_atq_parser_select_queue_none() {
        String tmp = "11 Mon Jul 2 10:22:00 2018 = jason\n16    Wed Jul  4 16:00:00 2018 a jason\n";

        Set<String> queues = new HashSet<String>();
        queues.add("b");

        Map<String, Map<String, String>> result = AtUtils.parseJobInfo(tmp, queues);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void test_getJobIDs_null() {
        String[] result = AtUtils.getJobIDs(null);
        assertNotNull(result);
        assertTrue(result.length == 0);
    }

    @Test
    public void test_getJobIDs_empty() {
        Map<String, Map<String, String>> m = new HashMap<>();

        String[] result = AtUtils.getJobIDs(m);
        assertNotNull(result);
        assertTrue(result.length == 0);
    }

    @Test
    public void test_getJobIDs() {
        Map<String, Map<String, String>> m = new HashMap<>();

        m.put("key1", null);
        m.put("key2", null);

        String[] result = AtUtils.getJobIDs(m);
        assertNotNull(result);
        assertTrue(result.length == 2);
        assertTrue(result[0].equals("key1") || result[1].equals("key1"));
        assertTrue(result[0].equals("key2") || result[1].equals("key2"));
    }

    @Test(expected = IncompleteJobDescriptionException.class)
    public void test_verifyJobDescription_fails_noexecutable() throws XenonException {
        JobDescription job = new JobDescription();
        AtUtils.verifyJobDescription(job, null);
    }

    @Test
    public void test_verifyJobDescription() throws XenonException {
        JobDescription job = new JobDescription();
        job.setExecutable("text.exe");
        job.setMaxRuntime(0);
        AtUtils.verifyJobDescription(job, null);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_verifyJobDescription_fail_maxRuntime() throws XenonException {
        JobDescription job = new JobDescription();
        job.setExecutable("text.exe");
        job.setMaxRuntime(15);
        AtUtils.verifyJobDescription(job, null);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_verifyJobDescription_fail_nodes() throws XenonException {
        JobDescription job = new JobDescription();
        job.setExecutable("text.exe");
        job.setMaxRuntime(0);
        job.setNodeCount(2);

        AtUtils.verifyJobDescription(job, null);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test_verifyJobDescription_fail_processes() throws XenonException {
        JobDescription job = new JobDescription();
        job.setExecutable("text.exe");
        job.setMaxRuntime(0);
        job.setProcessesPerNode(2);

        AtUtils.verifyJobDescription(job, null);
    }

    @Test
    public void test_generateJob() throws XenonException {
        JobDescription job = new JobDescription();
        job.setExecutable("text.exe");
        job.setMaxRuntime(0);

        String script = AtUtils.generateJobScript(job, new Path("/test"));

        String expected = "#AT_JOBNAME xenon\n" + "#AT_WORKDIR /test\n" + "#AT_STARTTIME now\n" + "#AT_INPUT '/dev/null'\n" + "#AT_OUTPUT '/dev/null'\n"
                + "#AT_ERROR '/dev/null'\n\n" + "cd '/test' && text.exe < '/dev/null' > '/dev/null' 2> '/dev/null'\n";

        assertNotNull(script);
        assertEquals(expected, script);
    }

    @Test
    public void test_generateJob_with_name() throws XenonException {
        JobDescription job = new JobDescription();
        job.setExecutable("text.exe");
        job.setMaxRuntime(0);
        job.setName("NAME");

        String script = AtUtils.generateJobScript(job, new Path("/test"));

        String expected = "#AT_JOBNAME NAME\n" + "#AT_WORKDIR /test\n" + "#AT_STARTTIME now\n" + "#AT_INPUT '/dev/null'\n" + "#AT_OUTPUT '/dev/null'\n"
                + "#AT_ERROR '/dev/null'\n\n" + "cd '/test' && text.exe < '/dev/null' > '/dev/null' 2> '/dev/null'\n";

        assertNotNull(script);
        assertEquals(expected, script);
    }

    @Test
    public void test_generateJob_with_name_empty() throws XenonException {
        JobDescription job = new JobDescription();
        job.setExecutable("text.exe");
        job.setMaxRuntime(0);
        job.setName("");

        String script = AtUtils.generateJobScript(job, new Path("/test"));

        String expected = "#AT_JOBNAME xenon\n" + "#AT_WORKDIR /test\n" + "#AT_STARTTIME now\n" + "#AT_INPUT '/dev/null'\n" + "#AT_OUTPUT '/dev/null'\n"
                + "#AT_ERROR '/dev/null'\n\n" + "cd '/test' && text.exe < '/dev/null' > '/dev/null' 2> '/dev/null'\n";

        assertNotNull(script);
        assertEquals(expected, script);
    }

    @Test
    public void test_generateJob_with_streams() throws XenonException {
        JobDescription job = new JobDescription();
        job.setExecutable("text.exe");
        job.setMaxRuntime(0);
        job.setStderr("stderr.txt");
        job.setStdout("stdout.txt");
        job.setStdin("stdin.txt");

        String script = AtUtils.generateJobScript(job, new Path("/test"));

        String expected = "#AT_JOBNAME xenon\n" + "#AT_WORKDIR /test\n" + "#AT_STARTTIME now\n" + "#AT_INPUT 'stdin.txt'\n" + "#AT_OUTPUT 'stdout.txt'\n"
                + "#AT_ERROR 'stderr.txt'\n\n" + "cd '/test' && text.exe < 'stdin.txt' > 'stdout.txt' 2> 'stderr.txt'\n";

        assertNotNull(script);
        assertEquals(expected, script);
    }

    @Test
    public void test_generateJob_with_arguments() throws XenonException {
        JobDescription job = new JobDescription();
        job.setExecutable("text.exe");
        job.setMaxRuntime(0);
        job.addArgument("a");
        job.addArgument("b");
        job.addArgument("c");

        String script = AtUtils.generateJobScript(job, new Path("/test"));

        String expected = "#AT_JOBNAME xenon\n" + "#AT_WORKDIR /test\n" + "#AT_STARTTIME now\n" + "#AT_INPUT '/dev/null'\n" + "#AT_OUTPUT '/dev/null'\n"
                + "#AT_ERROR '/dev/null'\n\n" + "cd '/test' && text.exe 'a' 'b' 'c' < '/dev/null' > '/dev/null' 2> '/dev/null'\n";

        assertNotNull(script);
        assertEquals(expected, script);
    }

    @Test
    public void test_generateJob_with_queue() throws XenonException {
        JobDescription job = new JobDescription();
        job.setExecutable("text.exe");
        job.setMaxRuntime(0);
        job.setQueueName("Z");

        String script = AtUtils.generateJobScript(job, new Path("/test"));

        String expected = "#AT_JOBNAME xenon\n" + "#AT_WORKDIR /test\n" + "#AT_QUEUE Z\n" + "#AT_STARTTIME now\n" + "#AT_INPUT '/dev/null'\n"
                + "#AT_OUTPUT '/dev/null'\n" + "#AT_ERROR '/dev/null'\n\n" + "cd '/test' && text.exe < '/dev/null' > '/dev/null' 2> '/dev/null'\n";

        assertNotNull(script);
        assertEquals(expected, script);
    }

    @Test
    public void test_generateJob_with_env() throws XenonException {
        JobDescription job = new JobDescription();
        job.setExecutable("text.exe");
        job.setMaxRuntime(0);
        job.addEnvironment("aap", "noot");
        job.addEnvironment("noot", "aap");

        String script = AtUtils.generateJobScript(job, new Path("/test"));

        System.err.println(script);

        String expected = "#AT_JOBNAME xenon\n" + "#AT_WORKDIR /test\n" + "#AT_STARTTIME now\n" + "#AT_INPUT '/dev/null'\n" + "#AT_OUTPUT '/dev/null'\n"
                + "#AT_ERROR '/dev/null'\n\n" + "export aap=\"noot\"\n" + "export noot=\"aap\"\n\n"
                + "cd '/test' && text.exe < '/dev/null' > '/dev/null' 2> '/dev/null'\n";

        System.err.println(script);

        System.err.println("expected");
        System.err.println(expected);

        assertNotNull(script);
        assertEquals(expected, script);
    }

}
