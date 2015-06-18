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

import static org.junit.Assert.assertTrue;

import java.io.OutputStream;

import nl.esciencecenter.xenon.adaptors.GenericJobAdaptorTestParent;
import nl.esciencecenter.xenon.files.OpenOption;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.Scheduler;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TorqueJobAdaptorTest extends GenericJobAdaptorTestParent {

    private static final Logger logger = LoggerFactory.getLogger(TorqueJobAdaptorTest.class);
    
    @BeforeClass
    public static void prepareGridEngineJobAdaptorTest() throws Exception {
        GenericJobAdaptorTestParent.prepareClass(new TorqueJobTestConfig(null));
    }

    @AfterClass
    public static void cleanupGridEngineJobAdaptorTest() throws Exception {
        GenericJobAdaptorTestParent.cleanupClass();
    }

    @org.junit.Test
    public void ge_test01_jobWithCustomScript() throws Exception {
        String message = "Hello World! test01\n";

        String workingDir = getWorkingDir("ge_test01");

        Scheduler scheduler = config.getDefaultScheduler(jobs, credentials);
        
        Path cwd = config.getWorkingDir(files, credentials);
        Path root = resolve(cwd, workingDir);
        
        files.createDirectories(root);

        Path script = resolve(root, "script");
        Path stdout = resolve(root, "stdout.txt");

        String scriptContent = "#!/bin/bash\n" + "#PBS -o " + stdout.getRelativePath().getAbsolutePath() + "\n" + "#PBS -e /dev/null\n" + "echo " + message;

        OutputStream out = files.newOutputStream(script, OpenOption.CREATE, OpenOption.APPEND, OpenOption.WRITE);
        writeFully(out, scriptContent);

        JobDescription description = new JobDescription();
        description.setInteractive(false);
        description.addJobOption("job.script", script.getRelativePath().getAbsolutePath());

        //the executable should be allowed to be null, as this field is not used at all. Check if this works
        description.setExecutable(null);

        Job job = jobs.submitJob(scheduler, description);
        JobStatus status = jobs.waitUntilDone(job, 60000);

		if (status != null) {
			if (!status.isDone()) {
				throw new Exception("Job exceeded deadline!");
			}

			if (status.hasException()) {
				throw new Exception("Job failed!", status.getException());
			}
		}

        String outputContent = readFully(files.newInputStream(stdout));

        logger.debug("got output " + outputContent);

        files.delete(stdout);
        files.delete(script);
        files.delete(root);

        jobs.close(scheduler);
        files.close(cwd.getFileSystem());

        assertTrue(outputContent.equals(message));
    }

//    @Test
//    public void ge_test02_newScheduler_pathWithSlash() throws Exception {
//
//        URI uriWithSlash = new URI(config.getCorrectURI().toString() + "/");
//
//        Scheduler s = jobs.newScheduler(uriWithSlash, null, null);
//        jobs.close(s);
//    }
//
//    @Test(expected = InvalidLocationException.class)
//    public void ge_test03_newScheduler_pathWithFragment_Exception() throws Exception {
//
//        URI uriWithFragment = new URI(config.getCorrectURI().toString() + "#somefragment");
//
//        Scheduler s = jobs.newScheduler(uriWithFragment, null, null);
//        jobs.close(s);
//    }

}
