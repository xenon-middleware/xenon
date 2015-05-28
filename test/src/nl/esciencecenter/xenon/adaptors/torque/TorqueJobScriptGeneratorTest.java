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
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Formatter;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.jobs.JobDescription;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TorqueJobScriptGeneratorTest {

    @Test
    public void test00_constructorIsPrivate() throws Throwable {
        Constructor<TorqueJobScriptGenerator> constructor = TorqueJobScriptGenerator.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
        constructor.setAccessible(false);
    }

    @Test
    public void test01a_generate_EmptyDescription_Result() throws XenonException {
        JobDescription description = new JobDescription();

        String result = TorqueJobScriptGenerator.generate(description, null);

        String expected =
                  "#!/bin/sh\n"
                + "#PBS -S /bin/sh\n"
                + "#PBS -N xenon\n"
                + "#PBS -l walltime=00:15:00\n"
                + "#PBS -o /dev/null\n"
                + "#PBS -e /dev/null\n\n"
                + "null\n";

        assertEquals(expected, result);
    }

    @Test
    /**
     * Check to see if the output is _exactly_ what we expect, and not a single char different.
     * @throws XenonException
     */
    public void test01b_generate__FilledDescription_Result() throws XenonException {
        JobDescription description = new JobDescription();
        description.setArguments("some", "arguments");
        description.addEnvironment("some", "environment.value");
        description.addEnvironment("some.more", "environment value with spaces");
        description.addJobOption(TorqueSchedulerConnection.JOB_OPTION_RESOURCES, "list-of-resources");
        description.setExecutable("/bin/executable");
        description.setMaxTime(100);
        description.setNodeCount(1);
        description.setProcessesPerNode(1);
        description.setQueueName("the.queue");
        description.setStderr("stderr.file");
        description.setStdin("stdin.file");
        description.setStdout("stdout.file");
        description.setWorkingDirectory("/some/working/directory");

        String result = TorqueJobScriptGenerator.generate(description, null);

        String expected =
                  "#!/bin/sh\n"
                + "#PBS -S /bin/sh\n"
                + "#PBS -N xenon\n"
                + "#PBS -w '/some/working/directory'"
                + "#PBS -q the.queue\n"
                + "#PBS -l list-of-resources\n"
                + "#PBS -l nodes=1,ppn=1\n"
                + "#PBS -l walltime=01:40:00\n"
                + "#PBS -i 'stdin.file'\n"
                + "#PBS -o 'stdout.file'\n"
                + "#PBS -e 'stderr.file'\n"
                + "export some.more=\"environment value with spaces\"\n"
                + "export some=\"environment.value\"\n\n"
                + "/bin/executable 'some' 'arguments'\n";

        assertEquals(expected, result);
    }

    @Test
    /**
     * Check to see if the output is _exactly_ what we expect, and not a single char different.
     * @throws XenonException
     */
    public void test01c_generate__ParallelDescription_Result() throws XenonException {
        JobDescription description = new JobDescription();
        description.setArguments("some", "arguments");
        description.addEnvironment("some", "environment.value");
        description.addEnvironment("some.more", "environment value with spaces");
        description.addJobOption(TorqueSchedulerConnection.JOB_OPTION_RESOURCES, "list-of-resources");
        description.setExecutable("/bin/executable");
        description.setMaxTime(100);
        description.setNodeCount(4);
        description.setProcessesPerNode(10);
        description.setQueueName("the.queue");
        description.setStderr("stderr.file");
        description.setStdin("stdin.file");
        description.setStdout("stdout.file");
        description.setWorkingDirectory("/some/working/directory");

        String result = TorqueJobScriptGenerator.generate(description, null);

        String expected =
                  "#!/bin/sh\n"
                + "#PBS -S /bin/sh\n"
                + "#PBS -N xenon\n"
                + "#PBS -w '/some/working/directory'"
                + "#PBS -q the.queue\n"
                + "#PBS -l list-of-resources\n"
                + "#PBS -l nodes=4,ppn=4\n"
                + "#PBS -l walltime=01:40:00\n"
                + "#PBS -i 'stdin.file'\n"
                + "#PBS -o 'stdout.file'\n"
                + "#PBS -e 'stderr.file'\n"
                + "export some.more=\"environment value with spaces\"\n"
                + "export some=\"environment.value\"\n\n"
                + "/bin/executable 'some' 'arguments'\n";

        assertEquals(expected, result);
    }

    @Test
    public void test01d_generate_CustomContents() throws XenonException {
        JobDescription description = new JobDescription();
        description.addJobOption(TorqueSchedulerConnection.JOB_OPTION_JOB_CONTENTS, "/myscript/or_other");

        String result = TorqueJobScriptGenerator.generate(description, null);

        String expected =
                  "#!/bin/sh\n"
                + "#PBS -S /bin/sh\n"
                + "#PBS -N xenon\n"
                + "#PBS -l walltime=00:15:00\n"
                + "#PBS -o /dev/null\n"
                + "#PBS -e /dev/null\n\n"
                + "/myscript/or_other\n";

        assertEquals(expected, result);
    }

    @Test
    public void test03a_generateSerialScriptContent() {
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/executable");
        description.setArguments("some", "arguments");

        Formatter output = new Formatter();

        String expected = "/bin/executable 'some' 'arguments'\n";

        TorqueJobScriptGenerator.generateScriptContent(description, output);

        assertEquals("serial script content incorrect", expected, output.out().toString());
    }
}
