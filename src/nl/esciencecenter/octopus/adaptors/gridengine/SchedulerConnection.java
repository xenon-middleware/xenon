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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.util.InputWriter;
import nl.esciencecenter.octopus.engine.util.OutputReader;
import nl.esciencecenter.octopus.engine.util.RemoteCommandRunner;
import nl.esciencecenter.octopus.engine.util.StreamForwarder;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.UnknownPropertyException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;
import nl.esciencecenter.octopus.jobs.Streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerConnection {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConnection.class);

    private static int schedulerID = 0;

    private static synchronized int getNextSchedulerID() {
        return schedulerID++;
    }

    private final URI actualLocation;

    private final XmlOutputParser parser;

    private final OctopusProperties properties;
    private final String[] queueNames;

    private String id;

    private final OctopusEngine engine;
    private final Scheduler sshScheduler;

    private String runCommandAtServer(String stdin, String executable, String... arguments) throws OctopusException,
            OctopusIOException {
        JobDescription description = new JobDescription();
        description.setInteractive(true);
        description.setExecutable(executable);
        description.setArguments(arguments);

        RemoteCommandRunner runner = new RemoteCommandRunner(engine, sshScheduler, stdin, description);

        String stderr = runner.getStderr();

        if (runner.getExitCode() != 0 || !stderr.isEmpty()) {
            throw new OctopusException(GridengineAdaptor.ADAPTOR_NAME, "could not run command \"" + executable
                    + "\" at server \"" + actualLocation.getHost() + "\". Error output: " + stderr);
        }

        return runner.getStdout();
    }

    public SchedulerConnection(URI location, Credential credential, Properties properties, OctopusEngine engine)
            throws OctopusIOException, OctopusException {
        this.engine = engine;
        this.properties = new OctopusProperties(properties);

        if (properties != null && properties.size() > 0) {
            throw new UnknownPropertyException(GridengineAdaptor.ADAPTOR_NAME,
                    "grid engine scheduler does not support any property");
        }

        GridengineAdaptor.checkLocation(location);
        try {

            id = GridengineAdaptor.ADAPTOR_NAME + "-" + getNextSchedulerID();

            parser = new XmlOutputParser(this.properties);

            if (location.getHost() == null || location.getHost().length() == 0) {
                //FIXME: check if this works for encode uri's, illegal characters, fragments, etc..
                actualLocation = new URI("local:///");
            } else {
                actualLocation = new URI("ssh", location.getSchemeSpecificPart(), location.getFragment());
            }
        } catch (URISyntaxException e) {
            throw new OctopusException(GridengineAdaptor.ADAPTOR_NAME, "cannot create ssh uri from given location", e);
        }

        logger.debug("creating ssh scheduler for GridEngine adaptor at " + actualLocation);
        sshScheduler = engine.jobs().newScheduler(actualLocation, credential, this.properties);

        //get status of all queues, use names to fill queue name list
        this.queueNames = getQueueStatus().keySet().toArray(new String[0]);

        logger.debug("queues for " + location + " are " + Arrays.toString(this.queueNames));
    }

    //local operation
    public String[] getQueueNames() {
        return queueNames;
    }

    public OctopusProperties getProperties() {
        return properties;
    }

    public String getID() {
        return id;
    }

    public Map<String, Map<String, String>> getQueueStatus() throws OctopusException, OctopusIOException {
        String qstatOutput = runCommandAtServer("qstat", null, "-xml", "-g", "c");

        return parser.parseQueueInfos(qstatOutput);
    }

    public Map<String, Map<String, String>> getJobStatus() throws OctopusException, OctopusIOException {
        String status = runCommandAtServer(null, "qstat", "-xml");

        return parser.parseJobInfos(status);
    }

    public Map<String, String> getJobAccountingInfo(String jobIdentifier) throws OctopusIOException, OctopusException {
        String output = runCommandAtServer(null, "qacct", "-j", jobIdentifier);

        return TxtOutputParser.getJobAccountingInfo(output);
    }

    /**
     * Submit a job to a GridEngine machine. Mostly involves parsing output
     * 
     * @param jobScript
     *            the script to submit
     * @return the id of the job
     * @throws OctopusException
     *             if the qsub command could not be run
     * @throws OctopusIOException
     */
    public String submitJob(String jobScript) throws OctopusException, OctopusIOException {
        String output = runCommandAtServer(jobScript, "qsub");

        return TxtOutputParser.checkSubmitJobResult(output);
    }

    /**
     * Cancel a job.
     * 
     * @param job
     *            the job to cancel
     * @throws OctopusException
     *             if the qsub command could not be run
     * @throws OctopusIOException
     *             if the qsub command could not be run, or the jobID could not be read from the output
     */
    public void cancelJob(Job job) throws OctopusIOException, OctopusException {
        String output = runCommandAtServer(null, "qdel", job.getIdentifier());

        TxtOutputParser.checkCancelJobResult(job.getIdentifier(), output);
    }

    public void close() throws OctopusIOException, OctopusException {
        engine.jobs().close(sshScheduler);
    }

};
