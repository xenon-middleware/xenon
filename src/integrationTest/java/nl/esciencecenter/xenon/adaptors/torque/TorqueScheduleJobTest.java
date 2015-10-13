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

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.GenericScheduleJobTestParent;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.jobs.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TorqueScheduleJobTest extends GenericScheduleJobTestParent {
    @BeforeClass
    public static void prepareTorqueScheduleJobTest() throws Exception {
        GenericScheduleJobTestParent.prepareClass(new TorqueJobTestConfig(null));
    }

    @AfterClass
    public static void cleanupTorqueScheduleJobTest() throws Exception {
        GenericScheduleJobTestParent.cleanupClass();
    }

    /**
     * Mocks a job description for Torque.
     *
     * The Torque adaptor does not accept custom stderr, stdout or stdin, so
     * all original tests may run, but only without those values set.
     */
    private static class MockTorqueJobDescription extends JobDescription {
        MockTorqueJobDescription(JobDescription original) {
            super(original);
        }
        @Override
        public void setStderr(String filename) {}
        @Override
        public void setStdout(String filename) {}
        @Override
        public void setStdin(String filename) {}
    }

    @Override
    protected JobDescription printEnvJobDescription(String workDir, String value) {
        return new MockTorqueJobDescription(super.printEnvJobDescription(workDir, value));
    }

    @Override
    protected JobDescription echoJobDescription(String workingDir, String message) {
        return new MockTorqueJobDescription(super.echoJobDescription(workingDir, message));
    }

    @Override
    protected JobDescription timedJobDescription(String workingDir, int seconds) {
        return new MockTorqueJobDescription(super.timedJobDescription(workingDir, seconds));
    }

    @Override
    protected JobDescription catJobDescription(String workingDir, Path stdin, String message) throws XenonException {
        throw new InvalidJobDescriptionException(null, "STDIN not known for Torque");
    }

    @Override
    protected JobDescription nonExistingJobDescription(String workingDir) {
        return new MockTorqueJobDescription(super.nonExistingJobDescription(workingDir));
    }

    @org.junit.Test
    public void torque_test01_jobWithCustomScript() throws Exception {
        String message = "Hello World! test01\n";
        String workingDir = getWorkingDir("ge_test01");
        Path root = initJobDirectory(workingDir);
        Path script = resolve(root, "script");
        
        try {
            String scriptContent = "#!/bin/bash\n" + "\necho " + message;
            writeFully(script, scriptContent);

            JobDescription description = new JobDescription();
            description.addJobOption("job.script", script.getRelativePath().getAbsolutePath());

            //the executable should be allowed to be null, as this field is not used at all. Check if this works
            description.setExecutable(null);

            job = jobs.submitJob(scheduler, description);
            JobStatus status = jobs.waitUntilDone(job, config.getJobTimeout(0));

            checkJobDone(status);
            checkJobOutput(job, root, message);
        } finally {
            cleanupJob(job, root, script);
        }
    }

    @Test(expected=InvalidJobDescriptionException.class) @Override
    public void test36a_batchJobSubmitWithInput() throws Exception {
        super.test36a_batchJobSubmitWithInput();
    }

    @Test(expected=InvalidJobDescriptionException.class) @Override
    public void test36b_batchJobSubmitWithInput() throws Exception {
        super.test36b_batchJobSubmitWithInput();
    }

    @Test(expected=InvalidJobDescriptionException.class) @Override
    public void test44_submit_JobDescriptionShouldBeSame() throws Exception {
        super.test44_submit_JobDescriptionShouldBeSame();
    }
}
