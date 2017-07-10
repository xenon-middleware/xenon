/**
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
package nl.esciencecenter.xenon.schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import nl.esciencecenter.xenon.schedulers.JobDescription;

/**
 * 
 */
public class JobDescriptionTest {

    @Test
    public void test_new() throws Exception {
        new JobDescription();
    }

    @Test
    public void test_copy() throws Exception {
        JobDescription jd = new JobDescription();
        jd.setExecutable("aap");

        JobDescription jd2 = new JobDescription(jd);
        assertEquals("aap", jd2.getExecutable());
    }
    
    // TODO: split into seperate test
    
    @Test
    public void test_setters_getters() throws Exception {
        JobDescription j = new JobDescription();

        j.setInteractive(true);
        boolean b = j.isInteractive();
        assertTrue(b);

        j.setWorkingDirectory("aap");
        String tmp = j.getWorkingDirectory();
        assertTrue(tmp.equals("aap"));

        j.setQueueName("noot");
        tmp = j.getQueueName();
        assertTrue(tmp.equals("noot"));

        j.setStdout("stdout");
        tmp = j.getStdout();
        assertTrue(tmp.equals("stdout"));

        j.setStdin("stdin");
        tmp = j.getStdin();
        assertTrue(tmp.equals("stdin"));

        j.setStderr("stderr");
        tmp = j.getStderr();
        assertTrue(tmp.equals("stderr"));

        j.setExecutable("exec");
        tmp = j.getExecutable();
        assertTrue(tmp.equals("exec"));

        j.setProcessesPerNode(42);
        int p = j.getProcessesPerNode();
        assertTrue(p == 42);

        j.setNodeCount(33);
        p = j.getNodeCount();
        assertTrue(p == 33);

        j.setMaxTime(500);
        p = j.getMaxTime();
        assertTrue(p == 500);

        j.setArguments("a", "b", "c");
        List<String> list = j.getArguments();
        assertTrue(list != null);
        assertTrue(list.size() == 3);
        assertTrue(Arrays.equals(list.toArray(new String[3]), new String[] { "a", "b", "c" }));

        Map<String, String> env = new HashMap<>(3);
        env.put("ENV1", "ARG1");
        env.put("ENV2", "ARG2");

        j.setEnvironment(env);
        Map<String, String> env2 = j.getEnvironment();
        assertTrue(env.equals(env2));

        Map<String, String> opt = new HashMap<>(3);
        opt.put("OPT1", "ARG1");
        opt.put("OPT2", "ARG2");

        j.setJobOptions(opt);
        Map<String, String> opt2 = j.getJobOptions();
        assertTrue(opt.equals(opt2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_argumentNull() throws Exception {
        JobDescription j = new JobDescription();
        j.addArgument(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_argumentEmpty() throws Exception {
        JobDescription j = new JobDescription();
        j.addArgument("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_environmentKeyEmpty() throws Exception {
        JobDescription j = new JobDescription();
        j.addEnvironment("", "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_environmentKeyNull() throws Exception {
        JobDescription j = new JobDescription();
        j.addEnvironment(null, "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_jobOptionKeyEmpty() throws Exception {
        JobDescription j = new JobDescription();
        j.addJobOption("", "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_jobOptionKeyNull() throws Exception {
        JobDescription j = new JobDescription();
        j.addJobOption(null, "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_jobOptionValueEmpty() throws Exception {
        JobDescription j = new JobDescription();
        j.addJobOption("key", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_jobOptionValueNull() throws Exception {
        JobDescription j = new JobDescription();
        j.addJobOption("key", null);
    }

    @Test
    public void test_hashCode() throws Exception {
        JobDescription j = new JobDescription();

        int hash = j.hashCode();

        final int prime = 31;
        int result = 1;

        result = prime * result + new ArrayList<String>(0).hashCode();
        result = prime * result + new HashMap<String, String>(0).hashCode();
        result = prime * result + new HashMap<String, String>(0).hashCode();
        //noinspection PointlessArithmeticExpression
        result = prime * result + 0;
        result = prime * result + 1237;
        result = prime * result + 15;
        result = prime * result + 1;
        result = prime * result + 1;
        //noinspection PointlessArithmeticExpression
        result = prime * result + 0;
        result = prime * result + 1237;
        //noinspection PointlessArithmeticExpression
        result = prime * result + 0;
        //noinspection PointlessArithmeticExpression
        result = prime * result + 0;
        //noinspection PointlessArithmeticExpression
        result = prime * result + 0;
        //noinspection PointlessArithmeticExpression
        result = prime * result + 0;

        assertEquals(result, hash);
    }

    @Test
    public void test_hashCode2() throws Exception {

        JobDescription j = new JobDescription();
        j.setInteractive(true);
        j.setWorkingDirectory("aap");
        j.setQueueName("noot");
        j.setStdout(null);
        j.setStdin("stdin");
        j.setStderr(null);
        j.setExecutable("exec");
        j.setStartSingleProcess(true);

        String[] args = new String[] { "a", "b", "c" };
        j.setArguments(args);

        Map<String, String> env = new HashMap<>(3);
        env.put("ENV1", "ARG1");
        env.put("ENV2", "ARG2");
        j.setEnvironment(env);

        Map<String, String> opt = new HashMap<>(3);
        opt.put("OPT1", "ARG1");
        opt.put("OPT2", "ARG2");
        j.setJobOptions(opt);

        int hash = j.hashCode();

        final int prime = 31;
        int result = 1;

        result = prime * result + Arrays.asList(args).hashCode();
        result = prime * result + env.hashCode();
        result = prime * result + "exec".hashCode();
        result = prime * result + opt.hashCode();
        result = prime * result + 1231;
        result = prime * result + 15;
        result = prime * result + 1;
        result = prime * result + 1;
        result = prime * result + "noot".hashCode();
        result = prime * result + 1231;
        //noinspection PointlessArithmeticExpression
        result = prime * result + 0;
        result = prime * result + "stdin".hashCode();
        //noinspection PointlessArithmeticExpression
        result = prime * result + 0;
        result = prime * result + "aap".hashCode();

        assertEquals(result, hash);
    }

    @Test
    public void test_hashCode3() throws Exception {

        JobDescription j = new JobDescription();
        j.setInteractive(true);
        j.setWorkingDirectory("aap");
        j.setQueueName("noot");
        j.setStdout("stdout");
        j.setStdin("stdin");
        j.setStderr("stderr");
        j.setExecutable("exec");
        j.setStartSingleProcess(true);

        String[] args = new String[] { "a", "b", "c" };
        j.setArguments(args);

        Map<String, String> env = new HashMap<>(3);
        env.put("ENV1", "ARG1");
        env.put("ENV2", "ARG2");
        j.setEnvironment(env);

        Map<String, String> opt = new HashMap<>(3);
        opt.put("OPT1", "ARG1");
        opt.put("OPT2", "ARG2");
        j.setJobOptions(opt);

        int hash = j.hashCode();

        final int prime = 31;
        int result = 1;

        result = prime * result + Arrays.asList(args).hashCode();
        result = prime * result + env.hashCode();
        result = prime * result + "exec".hashCode();
        result = prime * result + opt.hashCode();
        result = prime * result + 1231;
        result = prime * result + 15;
        result = prime * result + 1;
        result = prime * result + 1;
        result = prime * result + "noot".hashCode();
        result = prime * result + 1231;
        //noinspection PointlessArithmeticExpression
        result = prime * result + "stderr".hashCode();
        result = prime * result + "stdin".hashCode();
        //noinspection PointlessArithmeticExpression
        result = prime * result + "stdout".hashCode();
        result = prime * result + "aap".hashCode();

        assertEquals(result, hash);
    }

    
    @Test
    public void test_equals() throws Exception {

        JobDescription j = new JobDescription();

        assertTrue(j.equals(j));
        assertFalse(j.equals(null));
        assertFalse(j.equals("AAP"));

        JobDescription other = new JobDescription();
        assertTrue(j.equals(other));

        other.setInteractive(true);
        assertFalse(j.equals(other));
        assertFalse(other.equals(j));
        
        other.setInteractive(false);
        assertTrue(j.equals(other));
        assertTrue(other.equals(j));
        
        other.setMaxTime(42);
        assertFalse(j.equals(other));
        other.setMaxTime(15);
        assertTrue(j.equals(other));

        other.setNodeCount(2);
        assertFalse(j.equals(other));
        other.setNodeCount(1);
        assertTrue(j.equals(other));

        other.setProcessesPerNode(2);
        assertFalse(j.equals(other));
        other.setProcessesPerNode(1);
        assertTrue(j.equals(other));

        other.setStartSingleProcess(true);
        assertFalse(j.equals(other));
        other.setStartSingleProcess(false);
        assertTrue(j.equals(other));
        
        other.setExecutable("aap");
        assertFalse(j.equals(other));
        other.setExecutable(null);
        assertTrue(j.equals(other));

        j.setExecutable("noot");
        assertFalse(j.equals(other));
        other.setExecutable("aap");
        assertFalse(j.equals(other));
        j.setExecutable("aap");
        assertTrue(j.equals(other));

        other.setWorkingDirectory("noot");
        assertFalse(j.equals(other));
        j.setWorkingDirectory("noot");
        assertTrue(j.equals(other));

        other.setQueueName("noot");
        assertFalse(j.equals(other));
        j.setQueueName("noot");
        assertTrue(j.equals(other));

        other.setStdin("noot");
        assertFalse(j.equals(other));
        j.setStdin("noot");

        other.setStdout("stdout.txt");
        assertFalse(j.equals(other));
        j.setStdout("stdout.txt");
        assertTrue(j.equals(other));
        j.setStdout("stdout.txt");

        other.setStderr("stderr.txt");
        assertFalse(j.equals(other));
        j.setStderr("stderr.txt");

        String[] args = new String[] { "a", "b", "c" };
        other.setArguments(args);
        assertFalse(j.equals(other));
        j.setArguments(args);

        Map<String, String> env = new HashMap<>(3);
        env.put("ENV1", "ARG1");
        env.put("ENV2", "ARG2");
        other.setEnvironment(env);
        assertFalse(j.equals(other));
        other.setEnvironment(null);
        assertTrue(j.equals(other));

        Map<String, String> opt = new HashMap<>(3);
        opt.put("OPT1", "ARG1");
        opt.put("OPT2", "ARG2");
        other.setJobOptions(opt);
        assertFalse(j.equals(other));
        other.setJobOptions(null);

        assertTrue(j.equals(other));
    }

    @Test
    public void test_toString() throws Exception {

        String expected = "JobDescription [queueName=noot, executable=exec, arguments=[a, b, c], stdin=stdin.txt, stdout=stdout.txt,"
                + " stderr=stderr.txt, workingDirectory=aap, environment={ENV1=ARG1}, jobOptions={OPT1=ARG1},"
                + " nodeCount=1, processesPerNode=1, startSingleProcess=false, maxTime=15, interactive=true]";

        JobDescription j = new JobDescription();
        j.setInteractive(true);
        j.setWorkingDirectory("aap");
        j.setQueueName("noot");
        j.setStdout("stdout.txt");
        j.setStderr("stderr.txt");
        j.setStdin("stdin.txt");
        j.setExecutable("exec");

        String[] args = new String[] { "a", "b", "c" };
        j.setArguments(args);

        Map<String, String> env = new HashMap<>(2);
        env.put("ENV1", "ARG1");
        j.setEnvironment(env);

        Map<String, String> opt = new HashMap<>(2);
        opt.put("OPT1", "ARG1");
        j.setJobOptions(opt);

        String tmp = j.toString();

        System.out.println(tmp);

        assertEquals(expected, tmp);
    }
}
