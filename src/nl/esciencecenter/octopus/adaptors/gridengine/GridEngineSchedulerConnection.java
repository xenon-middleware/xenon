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
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.QueueStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.octopus.exceptions.JobCanceledException;
import nl.esciencecenter.octopus.exceptions.NoSuchQueueException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Interface to the GridEngine command line tools. Will run commands to submit/list/cancel jobs and get the status of queues.
 * 
 * @author Niels Drost
 * 
 */
public class GridEngineSchedulerConnection extends SchedulerConnection {

    private static final Logger logger = LoggerFactory.getLogger(GridEngineSchedulerConnection.class);

    //Tag containing version of xml in qstat -xml output
    public static String SGE62_SCHEMA_ATTRIBUTE = "xmlns:xsd";

    public static String SGE62_SCHEMA_VALUE =
            "http://gridengine.sunsource.net/source/browse/*checkout*/gridengine/source/dist/util/resources/schemas/qstat/qstat.xsd?revision=1.11";

    public static final int QACCT_GRACE_TIME = 60000; //ms, 1 minute

    private static final String QACCT_HEADER = "==============================================================";

    private final DocumentBuilder documentBuilder;

    private final boolean ignoreVersion;

    /**
     * Map with the last seen time of jobs. There is a short but noticeable delay between jobs disappearing from the qstat queue
     * output, and information about this job appearing in the qacct output. Instead of throwing an exception, we allow for a
     * certain grace time. Jobs will report the status "pending" during this time.
     */
    private final HashMap<String, Long> lastSeenMap;

    private final Scheduler scheduler;

    private final String[] queueNames;

    static DocumentBuilder createDocumentBuilder() throws OctopusIOException {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            return documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "could not create parser for qstat xml files", e);
        }
    }

    /**
     * Testing Constructor
     * 
     * @throws OctopusIOException
     */
    GridEngineSchedulerConnection() throws OctopusIOException {
        super();
        ignoreVersion = false;
        scheduler = null;
        queueNames = new String[] {};
        lastSeenMap = null;

        documentBuilder = createDocumentBuilder();
    }

    GridEngineSchedulerConnection(URI location, Credential credential, Properties properties, OctopusEngine engine)
            throws OctopusIOException, OctopusException {
        super(location, credential, properties, engine, GridengineAdaptor.ADAPTOR_NAME, GridengineAdaptor.ADAPTOR_SCHEMES);
        this.ignoreVersion = getProperties().getBooleanProperty(GridengineAdaptor.IGNORE_VERSION_PROPERTY);

        lastSeenMap = new HashMap<String, Long>();

        documentBuilder = createDocumentBuilder();

        this.queueNames = getQueueNamesFromServer();

        this.scheduler =
                new SchedulerImplementation(GridengineAdaptor.ADAPTOR_NAME, getID(), location, this.queueNames.clone(),
                        credential, getProperties(), false, false, true);
    }

    @Override
    Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    String[] getQueueNames() {
        return queueNames;
    }

    private synchronized void markJobSeen(String identifier) {
        long current = System.currentTimeMillis();

        lastSeenMap.put(identifier, current);
    }

    private synchronized void markJobsSeen(Set<String> identifiers) {
        long current = System.currentTimeMillis();

        for (String identifier : identifiers) {
            lastSeenMap.put(identifier, current);
        }
    }

    private synchronized void clearJobSeen(String identifier) {
        lastSeenMap.remove(identifier);
    }

    private synchronized boolean haveRecentlySeen(String identifier) {
        if (!lastSeenMap.containsKey(identifier)) {
            return false;
        }
        return System.currentTimeMillis() < (lastSeenMap.get(identifier) + QACCT_GRACE_TIME);
    }

    private void checkVersion(Document document) throws IncompatibleServerException {

        Element de = document.getDocumentElement();

        if (de == null || !de.hasAttribute(SGE62_SCHEMA_ATTRIBUTE) || de.getAttribute(SGE62_SCHEMA_ATTRIBUTE) == null) {

            if (ignoreVersion) {
                logger.warn("cannot determine version, version attribute not found. Ignoring as requested by "
                        + GridengineAdaptor.IGNORE_VERSION_PROPERTY);
            } else {

                throw new IncompatibleServerException(GridengineAdaptor.ADAPTOR_NAME,
                        "cannot determine version, version attribute not found. Use the "
                                + GridengineAdaptor.IGNORE_VERSION_PROPERTY + " property to ignore this error");
            }
        }

        String schemaValue = de.getAttribute(SGE62_SCHEMA_ATTRIBUTE);

        logger.debug("found schema value " + schemaValue);

        //schemaValue == null checked above
        if (!SGE62_SCHEMA_VALUE.equals(schemaValue)) {
            if (ignoreVersion) {
                logger.warn("cannot determine version, version attribute not found. Ignoring as requested by "
                        + GridengineAdaptor.IGNORE_VERSION_PROPERTY);
            } else {

                throw new IncompatibleServerException(GridengineAdaptor.ADAPTOR_NAME, "schema version reported by server ("
                        + schemaValue + ") incompatible with adaptor. Use the " + GridengineAdaptor.IGNORE_VERSION_PROPERTY
                        + " property to ignore this error");
            }
        }

    }

    /**
     * Debugging version of checkVersion function
     * 
     * @param file
     *            the file to check
     * @throws OctopusException
     *             if the version is incorrect
     * @throws OctopusIOException
     *             if the file cannot be read or parsed
     */
    void checkVersion(File file) throws OctopusException, OctopusIOException {
        try {
            Document result = documentBuilder.parse(file);
            result.normalize();

            checkVersion(result);
        } catch (SAXException | IOException e) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "could not parse qstat xml file", e);
        }
    }

    private Document parseDocument(String data) throws OctopusException, OctopusIOException {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes());
            Document result = documentBuilder.parse(in);
            result.normalize();

            checkVersion(result);

            return result;
        } catch (SAXException | IOException e) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "could not parse qstat xml file", e);
        }
    }

    /**
     * Parses queue info from "qstat -g c -xml"
     * 
     * @param in
     *            the stream to get the xml data from
     * @return a list containing all queue names found
     * @throws OctopusIOException
     *             if the file could not be parsed
     * @throws OctopusException
     *             if the server version is not compatible with this adaptor
     * @throws Exception
     */
    Map<String, Map<String, String>> parseQueueInfos(String data) throws OctopusIOException, OctopusException {
        Document document = parseDocument(data);

        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();

        logger.debug("root node of xml file: " + document.getDocumentElement().getNodeName());
        NodeList clusterNodes = document.getElementsByTagName("cluster_queue_summary");

        for (int i = 0; i < clusterNodes.getLength(); i++) {
            Node clusterNode = clusterNodes.item(i);

            if (clusterNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) clusterNode;

                NodeList tagNodes = element.getChildNodes();

                Map<String, String> queueInfo = new HashMap<String, String>();

                //fetch tags from the list of tag nodes. Ignores empty values
                for (int j = 0; j < tagNodes.getLength(); j++) {
                    Node tagNode = tagNodes.item(j);
                    if (tagNode.getNodeType() == Node.ELEMENT_NODE) {
                        String key = tagNode.getNodeName();
                        if (key != null && key.length() > 0) {
                            NodeList children = tagNode.getChildNodes();
                            if (children.getLength() > 0) {
                                String value = tagNode.getChildNodes().item(0).getNodeValue();
                                queueInfo.put(key, value);
                            }
                        }
                    }
                }

                String queueName = queueInfo.get("name");

                if (queueName == null || queueName.length() == 0) {
                    throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "found queue in queue list with no name");
                }

                result.put(queueName, queueInfo);
            }
        }

        if (result.size() == 0) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "server seems to have no queues");
        }

        return result;
    }

    /**
     * Parses job info from "qstat -xml"
     * 
     * @param in
     *            the stream to get the xml data from
     * @return a list containing all queue names found
     * @throws OctopusIOException
     *             if the file could not be parsed
     * @throws OctopusException
     *             if the server version is not compatible with this adaptor
     * @throws Exception
     */
    Map<String, Map<String, String>> parseJobInfos(String data) throws OctopusIOException, OctopusException {
        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
        
        Document document = parseDocument(data);

        logger.debug("root node of xml file: " + document.getDocumentElement().getNodeName());
        NodeList nodes = document.getElementsByTagName("job_list");

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                NodeList tagNodes = element.getChildNodes();

                Map<String, String> jobInfo = new HashMap<String, String>();

                //fetch tags from the list of tag nodes. Ignores empty values
                for (int j = 0; j < tagNodes.getLength(); j++) {
                    Node tagNode = tagNodes.item(j);
                    if (tagNode.getNodeType() == Node.ELEMENT_NODE) {
                        String key = tagNode.getNodeName();
                        if (key != null && key.length() > 0) {
                            NodeList children = tagNode.getChildNodes();
                            if (children.getLength() > 0) {
                                String value = tagNode.getChildNodes().item(0).getNodeValue();
                                jobInfo.put(key, value);
                            }
                        }
                    }
                }

                String state = element.getAttribute("state");

                if (state != null && state.length() > 0) {
                    jobInfo.put("long_state", state);
                }

                String jobID = jobInfo.get("JB_job_number");

                if (jobID == null || jobID.length() == 0) {
                    throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "found job in queue with no job number");
                }

                result.put(jobID, jobInfo);
            }
        }

        return result;
    }

    //parse qsub output, find and return the job ID
    static String parseQsubOutput(String output) throws OctopusIOException {
        String lines[] = output.split("\\r?\\n");

        if (lines.length == 0 || !lines[0].startsWith("Your job ") | lines[0].split(" ").length < 3) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get job id from qsub status message: " + output);
        }

        String jobID = lines[0].split(" ")[2];

        try {
            int jobIDInt = Integer.parseInt(jobID);

            logger.debug("found job id: " + jobIDInt);
        } catch (NumberFormatException e) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get job id from qsub status message: \""
                    + output + "\". Returned job id " + jobID + " does not seem to be a number", e);
        }
        return jobID;
    }

    //parse qdel output and check if we get the expected output
    static void parseQdelOutput(Job job, String qdelOutput) throws OctopusIOException {
        String[] stdoutLines = qdelOutput.split("\\r?\\n");

        String serverMessages = "output: " + Arrays.toString(stdoutLines);

        logger.debug("Deleted job. Got back " + serverMessages);

        if (stdoutLines.length == 0 || stdoutLines[0].isEmpty()) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get job delete status from qdel message: "
                    + serverMessages);
        }

        String[] elements = stdoutLines[0].split(" ");
        String[] withoutUser = Arrays.copyOfRange(elements, 1, elements.length);

        String identifier = job.getIdentifier();

        //two cases, 1 for running and one for pending jobs
        String[] expected1 = { "has", "registered", "the", "job", identifier, "for", "deletion" };
        String[] expected2 = { "has", "deleted", "job", identifier };

        if (!(Arrays.equals(withoutUser, expected1) || Arrays.equals(withoutUser, expected2))) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get job delete status from qdel message: \""
                    + serverMessages + "\"");
        }
    }

    //get all key/value pairs from the qacct output
    private static Map<String, String> parseQacctOutput(String output) throws OctopusIOException {
        Map<String, String> result = new HashMap<String, String>();

        if (!output.startsWith(QACCT_HEADER)) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Qacct output is excepted to start with " + QACCT_HEADER);
        }

        String lines[] = output.split("\\r?\\n");
        for (String line : lines) {
            String[] elements = line.split(" ", 2);

            if (elements.length == 2) {
                result.put(elements[0].trim(), elements[1].trim());
            } else if (line.equals(QACCT_HEADER)) {
                //IGNORE first line
            } else {
                logger.debug("found line " + line + " in output");
            }
        }

        return result;

    }

    private void jobsFromStatus(String statusOutput, Scheduler scheduler, List<Job> result) throws OctopusIOException,
            OctopusException {
        Map<String, Map<String, String>> status = parseJobInfos(statusOutput);

        String[] jobIDs = status.keySet().toArray(new String[0]);

        for (String jobID : jobIDs) {
            markJobSeen(jobID);

            result.add(new JobImplementation(scheduler, jobID, false, false));
        }

    }

    @Override
    public Job[] getJobs(String... queueNames) throws OctopusIOException, OctopusException {
        if (queueNames.length == 0) {
            queueNames = getQueueNames();
        } else {
            checkQueueNames(queueNames);
        }

        ArrayList<Job> result = new ArrayList<Job>();

        if (queueNames == null || queueNames.length == 0) {
            String statusOutput = runCommand(null, "qstat", "-xml");

            jobsFromStatus(statusOutput, getScheduler(), result);
        } else {
            for (String queueName : queueNames) {
                try {
                    String statusOutput = runCommand(null, "qstat", "-xml", "-q", queueName);

                    jobsFromStatus(statusOutput, getScheduler(), result);
                } catch (CommandFailedException e) {
                    //sge returns "1" as the exit code if there is something wrong with the queue, ignore
                    if (e.getExitCode() != 1) {
                        throw new OctopusException(GridengineAdaptor.ADAPTOR_NAME, "Failed to get queue status for queue "
                                + queueName, e);
                    } else {
                        logger.debug("Failed to get queue status for queue " + queueName, e);
                    }
                }
            }
        }

        return result.toArray(new Job[result.size()]);
    }

    private String[] getQueueNamesFromServer() throws OctopusIOException, OctopusException {
        String qstatOutput = runCommand(null, "qstat", "-xml", "-g", "c");

        Map<String, Map<String, String>> allMap = parseQueueInfos(qstatOutput);

        return allMap.keySet().toArray(new String[0]);
    }

    @Override
    public QueueStatus getQueueStatus(String queueName) throws OctopusIOException, OctopusException {
        String qstatOutput = runCommand(null, "qstat", "-xml", "-g", "c");

        Map<String, Map<String, String>> allMap = parseQueueInfos(qstatOutput);

        Map<String, String> map = allMap.get(queueName);

        if (map == null || map.isEmpty()) {
            throw new NoSuchQueueException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get status of queue " + queueName
                    + " from server, perhaps it does not exist?");
        }

        return new QueueStatusImplementation(getScheduler(), queueName, null, map);
    }

    @Override
    public QueueStatus[] getQueueStatuses(String... queueNames) throws OctopusIOException, OctopusException {
        if (queueNames.length == 0) {
            queueNames = getQueueNames();
        }

        QueueStatus[] result = new QueueStatus[queueNames.length];

        String qstatOutput = runCommand(null, "qstat", "-xml", "-g", "c");

        Map<String, Map<String, String>> allMap = parseQueueInfos(qstatOutput);

        for (int i = 0; i < queueNames.length; i++) {
            if (allMap == null || allMap.isEmpty()) {
                Exception exception =
                        new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Failed to get status of queues on server");
                result[i] = new QueueStatusImplementation(getScheduler(), queueNames[i], exception, null);
            } else {
                //state for only the requested queue
                Map<String, String> map = allMap.get(queueNames[i]);

                if (map == null || map.isEmpty()) {
                    Exception exception =
                            new NoSuchQueueException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get status of queue "
                                    + queueNames[i] + " from server");
                    result[i] = new QueueStatusImplementation(getScheduler(), queueNames[i], exception, null);
                } else {
                    result[i] = new QueueStatusImplementation(getScheduler(), queueNames[i], null, map);
                }
            }
        }

        return result;

    }

    @Override
    public Job submitJob(JobDescription description) throws OctopusIOException, OctopusException {
        verifyJobDescription(description);

        String jobScript = JobScriptGenerator.generate(description, getFsEntryPath());

        String output = runCommand(jobScript, "qsub");

        String identifier = parseQsubOutput(output);

        markJobSeen(identifier);
        return new JobImplementation(getScheduler(), identifier, description, false, false);
    }

    @Override
    public JobStatus cancelJob(Job job) throws OctopusIOException, OctopusException {
        String output = runCommand(null, "qdel", job.getIdentifier());

        parseQdelOutput(job, output);

        return getJobStatus(job);
    }

    private JobStatus qstatJobStatus(Map<String, String> info, Job job) {
        Exception exception = null;

        if (info == null || info.isEmpty()) {
            logger.debug("job state not in map");
            return null;
        }

        String longState = info.get("long_state");
        if (longState == null || longState.length() == 0) {
            exception =
                    new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "State for job " + job.getIdentifier()
                            + " not found on server");
        }

        String stateCode = info.get("state");
        if (stateCode != null && stateCode.contains("E")) {
            logger.debug("job is in error state, try to cancel job, pick up status from qacct");
            try {
                runCommand(null, "qdel", job.getIdentifier());
            } catch (OctopusIOException | OctopusException e) {
                logger.error("Failed to cancel job in error state", e);
            }
            return null;
        }

        return new JobStatusImplementation(job, longState, null, exception, longState.equals("running"), false, info);
    }

    /**
     * Creates a JobStatus from qacct output. For more info on the output, see the "N1 Grid Engine 6 User's Guide". Retrieved
     * from: http://docs.oracle.com/cd/E19080-01/n1.grid.eng6/817-6117/chp11-1/index.html
     * 
     * @param info
     *            a map containing key/value pairs parsed from the qacct output.
     * @param job
     *            the job to get the info for.
     * @return the current status of the job.
     */
    private JobStatus qacctJobStatus(Map<String, String> info, Job job) {
        Integer exitCode = null;
        Exception exception = null;
        String state = "done";
        String exitCodeString = info.get("exit_status");

        try {
            if (exitCodeString != null) {
                exitCode = Integer.parseInt(exitCodeString);
            }
        } catch (NumberFormatException e) {
            exception =
                    new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "cannot parse exit code of job " + job.getIdentifier()
                            + " from string " + exitCodeString, e);

        }

        String failedString = info.get("failed");

        if (failedString != null && !failedString.equals("0")) {
            if (failedString.startsWith("100")) {
                //error code for killed jobs
                exception = new JobCanceledException(GridengineAdaptor.ADAPTOR_NAME, "Job killed by signal");
            } else {
                exception = new OctopusException(GridengineAdaptor.ADAPTOR_NAME, "Job reports error: " + failedString);
            }
        }

        return new JobStatusImplementation(job, state, exitCode, exception, false, true, info);
    }

    @Override
    public JobStatus getJobStatus(Job job) throws OctopusException, OctopusIOException {

        String statusOutput = runCommand(null, "qstat", "-xml");

        Map<String, Map<String, String>> allMap = parseJobInfos(statusOutput);

        //mark all jobs in this map as recently seen
        markJobsSeen(allMap.keySet());

        JobStatus status = qstatJobStatus(allMap.get(job.getIdentifier()), job);

        Exception exception = null;
        if (status == null) {
            //this job cannot be found in the queue, or is reporting some sort of error.
            //to get some more information, run qacct
            try {
                String output = runCommand(null, "qacct", "-j", job.getIdentifier());
                clearJobSeen(job.getIdentifier());

                Map<String, String> accountingInfo = parseQacctOutput(output);

                status = qacctJobStatus(accountingInfo, job);
            } catch (CommandFailedException e) {
                logger.debug("qacct command failed", e);
                exception = e;
            }
        }

        //this job is neither in qstat nor qacct output. we assume it is "in between" for a certain grace time.
        if (status == null && haveRecentlySeen(job.getIdentifier())) {
            status = new JobStatusImplementation(job, "unknown", null, null, false, false, new HashMap<String, String>());
        }

        //this job really does not exist. set it to an error state. List qacct exception as cause (if set)
        if (status == null) {

            exception =
                    new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Job " + job.getIdentifier() + " not found on server",
                            exception);
            status = new JobStatusImplementation(job, null, null, exception, false, false, null);
        }

        logger.debug("got job status {}", status);

        return status;
    }

    @Override
    public JobStatus[] getJobStatuses(Job... jobs) throws OctopusIOException, OctopusException {
        JobStatus[] result = new JobStatus[jobs.length];

        for (int i = 0; i < result.length; i++) {
            if (jobs[i] == null) {
                result[i] = null;
            } else {
                result[i] = getJobStatus(jobs[i]);
            }
        }
        return result;
    }

}
