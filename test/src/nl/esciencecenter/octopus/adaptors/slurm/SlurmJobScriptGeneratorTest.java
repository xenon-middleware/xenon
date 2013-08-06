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
package nl.esciencecenter.octopus.adaptors.slurm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.jobs.JobDescription;

import org.junit.Test;

/**
 * @author Niels Drost
 * 
 */
public class SlurmJobScriptGeneratorTest {
    
    @Test
    public void test00_constructorIsPrivate() throws Throwable {
        Constructor<SlurmJobScriptGenerator> constructor = SlurmJobScriptGenerator.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
        constructor.setAccessible(false);
    }

    @Test
    public void testEmptyDescription() throws OctopusException {
        JobDescription description = new JobDescription();

        String result = SlurmJobScriptGenerator.generate(description, null);

        String expected = "#!/bin/sh\n" + "#SBATCH --job-name octopus\n" + "#SBATCH --nodes=1\n"
                + "#SBATCH --ntasks-per-node=1\n" + "#SBATCH --time=15\n" + "#SBATCH --output=/dev/null\n"
                + "#SBATCH --error=/dev/null\n\n" + "srun null\n";

        assertEquals(expected, result);
    }

    @Test
    /**
     * Check to see if the output is _exactly_ what we expect, and not a single char different.
     * @throws OctopusException
     */
    public void testFilledDescription() throws OctopusException {
        JobDescription description = new JobDescription();
        description.setArguments("some", "arguments");
        description.addEnvironment("some", "environment.value");
        description.addEnvironment("some.more", "environment value with spaces");
        description.addJobOption("job", "option");
        description.setExecutable("/bin/executable");
        description.setMaxTime(100);
        description.setNodeCount(5);
        description.setProcessesPerNode(55);
        description.setQueueName("the.queue");
        description.setStderr("stderr.file");
        description.setStdin("stdin.file");
        description.setStdout("stdout.file");
        description.setWorkingDirectory("/some/working/directory");

        String result = SlurmJobScriptGenerator.generate(description, null);

        String expected = "#!/bin/sh\n" + "#SBATCH --job-name octopus\n" + "#SBATCH --workdir='/some/working/directory'\n"
                + "#SBATCH --partition=the.queue\n" + "#SBATCH --nodes=5\n" + "#SBATCH --ntasks-per-node=55\n"
                + "#SBATCH --time=100\n" + "#SBATCH --input='stdin.file'\n" + "#SBATCH --output='stdout.file'\n"
                + "#SBATCH --error='stderr.file'\n" + "export some.more=\"environment value with spaces\"\n"
                + "export some=\"environment.value\"\n\n" + "srun /bin/executable 'some' 'arguments'\n";

        System.out.println(result);

        assertEquals(expected, result);
    }
}
