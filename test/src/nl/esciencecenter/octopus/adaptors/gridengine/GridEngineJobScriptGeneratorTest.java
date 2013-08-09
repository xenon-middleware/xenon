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
package nl.esciencecenter.octopus.adaptors.gridengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import nl.esciencecenter.octopus.exceptions.InvalidJobDescriptionException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.JobDescription;

import org.junit.Test;

public class GridEngineJobScriptGeneratorTest {

    @Test
    public void test00_constructorIsPrivate() throws Throwable {
        Constructor<GridEngineJobScriptGenerator> constructor = GridEngineJobScriptGenerator.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
        constructor.setAccessible(false);
    }

    @Test
    public void test01a_generate_EmptyDescription_Result() throws OctopusException {
        JobDescription description = new JobDescription();

        String result = GridEngineJobScriptGenerator.generate(description, null, null);

        String expected = "#!/bin/sh\n" + "#$ -S /bin/sh\n" + "#$ -N octopus\n" + "#$ -l h_rt=00:15:00\n" + "#$ -o /dev/null\n"
                + "#$ -e /dev/null\n" + "\n" + "null\n";

        assertEquals(expected, result);
    }

    @Test
    /**
     * Check to see if the output is _exactly_ what we expect, and not a single char different.
     * @throws OctopusException
     */
    public void test01b_generate__FilledDescription_Result() throws OctopusException {
        JobDescription description = new JobDescription();
        description.setArguments("some", "arguments");
        description.addEnvironment("some", "environment.value");
        description.addEnvironment("some.more", "environment value with spaces");
        description.addJobOption("job", "option");
        description.setExecutable("/bin/executable");
        description.setMaxTime(100);
        description.setNodeCount(1);
        description.setProcessesPerNode(1);
        description.setQueueName("the.queue");
        description.setStderr("stderr.file");
        description.setStdin("stdin.file");
        description.setStdout("stdout.file");
        description.setWorkingDirectory("/some/working/directory");

        String result = GridEngineJobScriptGenerator.generate(description, null, null);

        String expected = "#!/bin/sh\n" + "#$ -S /bin/sh\n" + "#$ -N octopus\n" + "#$ -wd '/some/working/directory'\n"
                + "#$ -q the.queue\n" + "#$ -l h_rt=01:40:00\n" + "#$ -i 'stdin.file'\n" + "#$ -o 'stdout.file'\n"
                + "#$ -e 'stderr.file'\n" + "export some.more=\"environment value with spaces\"\n"
                + "export some=\"environment.value\"\n" + "\n" + "/bin/executable 'some' 'arguments'\n";

        System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    /**
     * Check to see if the output is _exactly_ what we expect, and not a single char different.
     * @throws OctopusException
     */
    public void test01c_generate__ParallelDescription_Result() throws OctopusException {
        JobDescription description = new JobDescription();
        description.setExecutable("/bin/executable");
        description.setArguments("some", "arguments");
        description.setMaxTime(100);
        description.setNodeCount(4);
        description.setProcessesPerNode(10);
        description.setQueueName("the.queue");
        description.setStderr("stderr.file");
        description.setStdin("stdin.file");
        description.setStdout("stdout.file");
        description.setWorkingDirectory("/some/working/directory");

        //set pe and slots explicitly. We test the setup class used to automatically get these values separately.
        description.addJobOption(GridEngineSchedulerConnection.JOB_OPTION_PARALLEL_ENVIRONMENT, "some.pe");
        description.addJobOption(GridEngineSchedulerConnection.JOB_OPTION_PARALLEL_SLOTS, "5");

        String result = GridEngineJobScriptGenerator.generate(description, null, null);

        String expected = "#!/bin/sh\n" + "#$ -S /bin/sh\n" + "#$ -N octopus\n" + "#$ -wd '/some/working/directory'\n"
                + "#$ -q the.queue\n" + "#$ -pe some.pe 5\n" + "#$ -l h_rt=01:40:00\n" + "#$ -i 'stdin.file'\n"
                + "#$ -o 'stdout.file'\n" + "#$ -e 'stderr.file'\n" + "\n"
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
                + "  ssh -o StrictHostKeyChecking=false $host \"cd `pwd` && /bin/executable 'some' 'arguments'\"&\n"
                + "done\n" + "\n" + "wait\n" + "exit 0\n\n";

        System.out.println(result);

        assertEquals(expected, result);
    }

    @Test(expected = InvalidJobDescriptionException.class)
    public void test01d_InvalidParallelSlotsOption_ExceptionThrown() throws OctopusException {
        JobDescription description = new JobDescription();
        description.setNodeCount(2);

        description.addJobOption(GridEngineSchedulerConnection.JOB_OPTION_PARALLEL_SLOTS, "five");

        GridEngineJobScriptGenerator.generate(description, null, null);
    }
    
    
    
    @Test
    public void test02a_parseIntOption() {
        fail("implement");
    }

    
    @Test
    public void test03a_generateParallelEnvironmentSpecification() {
        fail("implement");
    }

    @Test
    public void test04a_generateSerialScriptContent() {
        fail("implement");
    }

    @Test
    public void test05a_generateParallelScriptContent() {
        fail("implement");
    }

}
