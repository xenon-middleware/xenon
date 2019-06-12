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

    @Test
    public void test_setters_getters() throws Exception {
        JobDescription j = new JobDescription();

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

        j.setMaxRuntime(500);
        p = j.getMaxRuntime();
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
    public void test_addSchedulerArgumentNull() throws Exception {
        JobDescription j = new JobDescription();
        j.addSchedulerArgument(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_addSchedulerArgumentEmpty() throws Exception {
        JobDescription j = new JobDescription();
        j.addSchedulerArgument("");
    }

    @Test
    public void test_addSchedulerArgument() throws Exception {
        JobDescription j = new JobDescription();
        j.addSchedulerArgument("ARG");
    }

    private int doHash(String queueName, String executable, String name, String[] arguments, String[] schedulerArguments, String stdin, String stdout,
            String stderr, String workingDirectory, Map<String, String> environment, int nodeCount, int processesPerNode,
            int threadsPerProcess, int maxMemory, int tempSpace, boolean startSingleProcess, int maxRuntime) {

        List<String> tmp = new ArrayList<>(10);

        if (arguments != null && arguments.length > 0) {
            for (String s : arguments) {
                tmp.add(s);
            }
        }

        List<String> tmp2 = new ArrayList<>(10);

        if (schedulerArguments != null && schedulerArguments.length > 0) {
            for (String s : schedulerArguments) {
                tmp2.add(s);
            }
        }

        final int prime = 31;
        int result = 1;
        result = prime * result + tmp.hashCode();
        result = prime * result + tmp2.hashCode();
        result = prime * result + environment.hashCode();
        result = prime * result + ((executable == null) ? 0 : executable.hashCode());
        result = prime * result + maxMemory;
        result = prime * result + tempSpace;
        result = prime * result + maxRuntime;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + nodeCount;
        result = prime * result + processesPerNode;
        result = prime * result + ((queueName == null) ? 0 : queueName.hashCode());
        result = prime * result + (startSingleProcess ? 1231 : 1237);
        result = prime * result + ((stderr == null) ? 0 : stderr.hashCode());
        result = prime * result + ((stdin == null) ? 0 : stdin.hashCode());
        result = prime * result + ((stdout == null) ? 0 : stdout.hashCode());
        result = prime * result + threadsPerProcess;
        result = prime * result + ((workingDirectory == null) ? 0 : workingDirectory.hashCode());
        return result;
    }

    @Test
    public void test_hashCode() throws Exception {
        JobDescription j = new JobDescription();

        int expected = doHash(null, null, null, new String[0], new String[0], null, null, null, null, new HashMap<>(5), 1, 1, -1, -1, -1,
                false, -1);
        int hash = j.hashCode();

        assertEquals(expected, hash);
    }

    @Test
    public void test_hashCode2() throws Exception {

        JobDescription j = new JobDescription();
        j.setWorkingDirectory("workdir");
        j.setName("name");
        j.setQueueName("queue");
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

        int expected = doHash("queue", "exec", "name", args, new String[0], "stdin", null, null, "workdir", env, 1, 1, -1, -1, -1, true, -1);
        int hash = j.hashCode();

        assertEquals(expected, hash);
    }

    @Test
    public void test_hashCode3() throws Exception {

        JobDescription j = new JobDescription();
        j.setWorkingDirectory("aap");
        j.setQueueName("noot");
        j.setStdout("stdout");
        j.setStdin("stdin");
        j.setStderr("stderr");
        j.setExecutable("exec");
        j.setStartSingleProcess(true);
        j.setThreadsPerProcess(4);
        j.setMaxMemory(1024);
        j.setTempSpace(512);
        j.setMaxRuntime(15);

        String[] args = new String[] { "a", "b", "c" };
        j.setArguments(args);

        String[] schedArgs = new String[] { "1", "2", "3" };
        j.setSchedulerArguments(schedArgs);

        Map<String, String> env = new HashMap<>(3);
        env.put("ENV1", "ARG1");
        env.put("ENV2", "ARG2");
        j.setEnvironment(env);

        int expected = doHash("noot", "exec", null, args, schedArgs, "stdin", "stdout", "stderr", "aap", env, 1, 1, 4, 1024, 512, true, 15);
        int hash = j.hashCode();
        assertEquals(expected, hash);
    }

    @Test
    public void test_equals() throws Exception {

        JobDescription j = new JobDescription();

        assertTrue(j.equals(j));
        assertFalse(j.equals(null));
        assertFalse(j.equals("AAP"));

        JobDescription other = new JobDescription();
        assertTrue(j.equals(other));

        other.setMaxRuntime(42);
        assertFalse(j.equals(other));
        other.setMaxRuntime(-1);
        assertTrue(j.equals(other));

        other.setMaxMemory(1024);
        assertFalse(j.equals(other));
        other.setMaxMemory(-1);
        assertTrue(j.equals(other));

        other.setTempSpace(512);
        assertFalse(j.equals(other));
        other.setTempSpace(-1);
        assertTrue(j.equals(other));

        other.setThreadsPerProcess(4);
        assertFalse(j.equals(other));
        other.setThreadsPerProcess(-1);
        assertTrue(j.equals(other));

        other.setName("test");
        assertFalse(j.equals(other));
        other.setName(null);
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

        String[] schedArgs = new String[] { "1", "2", "3" };
        other.setSchedulerArguments(schedArgs);
        assertFalse(j.equals(other));
        j.setSchedulerArguments(schedArgs);

        Map<String, String> env = new HashMap<>(3);
        env.put("ENV1", "ARG1");
        env.put("ENV2", "ARG2");
        other.setEnvironment(env);
        assertFalse(j.equals(other));
        other.setEnvironment(null);
        assertTrue(j.equals(other));
    }

    @Test
    public void test_toString() throws Exception {

        String expected = "JobDescription [name=job, queueName=noot, executable=exec, arguments=[a, b, c], schedulerArguments=[1, 2, 3], stdin=stdin.txt,"
                + " stdout=stdout.txt, stderr=stderr.txt, workingDirectory=aap, environment={ENV1=ARG1},"
                + " nodeCount=1, processesPerNode=1, threadsPerProcess=4, maxMemory=1024, tempSpace=512, startSingleProcess=false, maxTime=-1]";

        JobDescription j = new JobDescription();
        j.setName("job");
        j.setWorkingDirectory("aap");
        j.setQueueName("noot");
        j.setStdout("stdout.txt");
        j.setStderr("stderr.txt");
        j.setStdin("stdin.txt");
        j.setExecutable("exec");
        j.setThreadsPerProcess(4);
        j.setMaxMemory(1024);
        j.setTempSpace(512);
        j.setArguments(new String[] { "a", "b", "c" });
        j.setSchedulerArguments(new String[] { "1", "2", "3" });

        Map<String, String> env = new HashMap<>(2);
        env.put("ENV1", "ARG1");
        j.setEnvironment(env);

        String tmp = j.toString();

        assertEquals(expected, tmp);
    }
}
