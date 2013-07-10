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

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import nl.esciencecenter.octopus.adaptors.scripting.RemoteCommandRunner;
import nl.esciencecenter.octopus.adaptors.scripting.SchedulerConnection;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.QueueStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.octopus.engine.util.CommandLineUtils;
import nl.esciencecenter.octopus.exceptions.InvalidJobDescriptionException;
import nl.esciencecenter.octopus.exceptions.NoSuchQueueException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface to the GridEngine command line tools. Will run commands to submit/list/cancel jobs and get the status of queues.
 * 
 * @author Niels Drost
 * 
 */
public class SlurmSchedulerConnection extends SchedulerConnection {

    private static final Logger logger = LoggerFactory.getLogger(SlurmSchedulerConnection.class);

    public static final String PROPERTY_PREFIX = OctopusEngine.ADAPTORS + SlurmAdaptor.ADAPTOR_NAME + ".";

    /** List of {NAME, DEFAULT_VALUE, DESCRIPTION} for properties. */
    private static final String[][] validPropertiesList = new String[0][0];

    public static final String JOB_OPTION_JOB_SCRIPT = "job.script";

    public static final String[] VALID_JOB_OPTIONS = new String[] { JOB_OPTION_JOB_SCRIPT };

    private final String[] queueNames;
    private final String defaultQueueName;

    private final Scheduler scheduler;

    private final boolean accountingAvailable;

    SlurmSchedulerConnection(URI location, Credential credential, Properties properties, OctopusEngine engine)
            throws OctopusIOException, OctopusException {
        super(location, credential, properties, engine, validPropertiesList, SlurmAdaptor.ADAPTOR_NAME,
                SlurmAdaptor.ADAPTOR_SCHEMES);

        this.queueNames = fetchQueueNames();
        this.defaultQueueName = findDefaultQueue();

        this.accountingAvailable = checkAccountingAvailable();

        scheduler =
                new SchedulerImplementation(SlurmAdaptor.ADAPTOR_NAME, getID(), location, queueNames, credential,
                        getProperties(), false, false, true);

        logger.debug("new slurm scheduler connection {}", scheduler);
    }

    private String[] fetchQueueNames() throws OctopusIOException, OctopusException {
        String output = runCheckedCommand(null, "sinfo", "--noheader", "--format=%120P");

        String[] queues = output.split(SlurmOutputParser.WHITESPACE_REGEX);

        return queues;
    }
    
    private String findDefaultQueue()  {
        for(int i = 0; i < queueNames.length; i++) {
            String queueName = queueNames[i];
            if (queueName.endsWith("*")) {
                //cut "*" of queue name
                queueNames[i] = queueName.substring(0, queueName.length() - 1);
                return queueNames[i];
            }
        }
        //no default queue found
        return null;
    }


    private boolean checkAccountingAvailable() throws OctopusIOException, OctopusException {
        RemoteCommandRunner runner = runCommand(null, "sacct");

        boolean result = runner.success();

        logger.debug("accounting available {}, command output {}", result, runner);

        return result;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public String[] getQueueNames() {
        return queueNames.clone();
    }
    
    @Override
    public String getDefaultQueueName() {
        return defaultQueueName;
    }

    @Override
    protected void verifyJobDescription(JobDescription description) throws OctopusException {
        //check if all given job options make sense
        //FIXME: this should be build on top of OctopusProperties, see #132
        for (String option : description.getJobOptions().keySet()) {
            boolean found = false;
            for (String validOption : VALID_JOB_OPTIONS) {
                if (validOption.equals(option)) {
                    found = true;
                }
            }
            if (!found) {
                throw new InvalidJobDescriptionException(SlurmAdaptor.ADAPTOR_NAME, "Given Job option \"" + option
                        + "\" not supported");
            }
        }

        if (description.isInteractive()) {
            throw new InvalidJobDescriptionException(SlurmAdaptor.ADAPTOR_NAME, "Adaptor does not support interactive jobs");
        }

        //check for option that overrides job script completely.
        if (description.getJobOptions().get(JOB_OPTION_JOB_SCRIPT) != null) {
            //no remaining settings checked.
            return;
        }

        //perform standard checks.
        super.verifyJobDescription(description);
    }

    @Override
    public Job submitJob(JobDescription description) throws OctopusIOException, OctopusException {
        String output;
        AbsolutePath fsEntryPath = getFsEntryPath();

        verifyJobDescription(description);

        //check for option that overrides job script completely.
        String customScriptFile = description.getJobOptions().get(JOB_OPTION_JOB_SCRIPT);

        if (customScriptFile == null) {
            String jobScript = SlurmJobScriptGenerator.generate(description, fsEntryPath);

            output = runCheckedCommand(jobScript, "sbatch");
        } else {
            //the user gave us a job script. Pass it to sbatch as-is

            //convert to absolute path if needed
            if (!customScriptFile.startsWith("/")) {
                AbsolutePath scriptFile = fsEntryPath.resolve(new RelativePath(customScriptFile));
                customScriptFile = scriptFile.getPath();
            }

            output = runCheckedCommand(null, "sbatch", customScriptFile);
        }

        String identifier = SlurmOutputParser.parseSbatchOutput(output);

        return new JobImplementation(getScheduler(), identifier, description, false, false);
    }

    @Override
    public JobStatus cancelJob(Job job) throws OctopusIOException, OctopusException {
        String identifier = job.getIdentifier();
        String scancelOutput = runCheckedCommand(null, "scancel", identifier);

        SlurmOutputParser.parseScancelOutput(identifier, scancelOutput);

        return getJobStatus(job);
    }

    @Override
    public Job[] getJobs(String... queueNames) throws OctopusIOException, OctopusException {
        String output;

        if (queueNames == null || queueNames.length == 0) {
            output = runCheckedCommand(null, "squeue", "--noheader", "--format=%i");
        } else {
            //add a list of all requested queues
            output =
                    runCheckedCommand(null, "squeue", "--noheader", "--format=%i",
                            "--partitions=" + CommandLineUtils.asCSList(getQueueNames()));
        }

        //Job id's are on separate lines, on their own.
        String[] jobIdentifiers = output.split(SlurmOutputParser.WHITESPACE_REGEX);

        Job[] result = new Job[jobIdentifiers.length];

        for (int i = 0; i < result.length; i++) {
            result[i] = new JobImplementation(scheduler, jobIdentifiers[i], false, false);
        }

        return result;
    }

    @Override
    public JobStatus getJobStatus(Job job) throws OctopusException, OctopusIOException {

        // TODO Auto-generated method stub
        return null;

    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) throws OctopusIOException, OctopusException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QueueStatus getQueueStatus(String queueName) throws OctopusIOException, OctopusException {
        String output = runCheckedCommand(null, "sinfo", "--format=%P %a %l %F %N %C %D", "--partition=" + queueName);

        List<Map<String, String>> infoMaps = SlurmOutputParser.parseSinfoOutput(output);

        if (infoMaps.size() == 0) {
            throw new NoSuchQueueException(SlurmAdaptor.ADAPTOR_NAME, "cannot get status of requested queue: \"" + queueName + "\"");
        }
        
        if (infoMaps.size() != 1) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Tried to get sinfo status of one partition, but got "
                    + infoMaps.size());
        }
        
        Map<String, String> infoMap = infoMaps.get(0);
        
        if (!infoMap.containsKey("PARTITION")) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "sinfo does not contain requested partition field");
        }
        
        if (!infoMap.get("PARTITION").equals(queueName)) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "sinfo does not contain info on requested partition");
        }

        return new QueueStatusImplementation(getScheduler(), queueName, null, infoMap); 
    }

    @Override
    public QueueStatus[] getQueueStatuses(String... queueNames) throws OctopusIOException, OctopusException {
        // TODO Auto-generated method stub
        return null;
    }




}
