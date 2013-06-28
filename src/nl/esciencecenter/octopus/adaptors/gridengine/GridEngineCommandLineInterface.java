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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.QueueStatusImplementation;
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
public class GridEngineCommandLineInterface implements CommandLineInterface {

    private static final Logger logger = LoggerFactory.getLogger(GridEngineCommandLineInterface.class);

    //Tag containing version of xml in qstat -xml output
    public static String SGE62_SCHEMA_ATTRIBUTE = "xmlns:xsd";

    public static String SGE62_SCHEMA_VALUE =
            "http://gridengine.sunsource.net/source/browse/*checkout*/gridengine/source/dist/util/resources/schemas/qstat/qstat.xsd?revision=1.11";

    private final DocumentBuilder documentBuilder;

    private final boolean ignoreVersion;

    public static final int QACCT_GRACE_TIME = 60000; //ms, 1 minute

    /**
     * Map with the last seen time of jobs. There is a short but noticeable delay between jobs disappearing from the qstat queue
     * output, and information about this job appearing in the qacct output. Instead of throwing an exception, we allow for a
     * certain grace time. Jobs will report the status "pending" during this time.
     */
    private final HashMap<String, Long> lastSeenMap;

    GridEngineCommandLineInterface(OctopusProperties properties) throws OctopusIOException {
        this(properties.getBooleanProperty(GridengineAdaptor.IGNORE_VERSION_PROPERTY));
    }

    GridEngineCommandLineInterface(boolean ignoreVersion) throws OctopusIOException {
        this.ignoreVersion = ignoreVersion;

        lastSeenMap = new HashMap<String, Long>();

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "could not create parser for qstat xml files", e);
        }
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

    private QueueStatus getQueueStatusFromMap(Map<String, Map<String, String>> allMap, Scheduler scheduler, String queue) {
        if (allMap == null || allMap.isEmpty()) {
            Exception exception =
                    new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Failed to get status of queues on server");
            return new QueueStatusImplementation(scheduler, queue, exception, null);
        }

        //state for only the requested  queue
        Map<String, String> map = allMap.get(queue);

        if (map == null || map.isEmpty()) {
            Exception exception =
                    new NoSuchQueueException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get status of queue " + queue
                            + " from server");
            return new QueueStatusImplementation(scheduler, queue, exception, null);
        }

        return new QueueStatusImplementation(scheduler, queue, null, map);
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
        Document document = parseDocument(data);

        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();

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
                    jobInfo.put("state", state);
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

    private JobStatus getJobStatusFromMap(Map<String, Map<String, String>> allMap, Job job) {
        if (allMap == null || allMap.isEmpty()) {
            Exception exception =
                    new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Failed to get status of jobs on server");
            return new JobStatusImplementation(job, null, null, exception, false, false, null);
        }

        //state for only the requested job
        Map<String, String> map = allMap.get(job.getIdentifier());

        if (map == null || map.isEmpty()) {
            Exception exception =
                    new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Job " + job.getIdentifier() + " not found on server");
            return new JobStatusImplementation(job, null, null, exception, false, false, null);
        }

        String state = map.get("state");

        if (state == null || state.length() == 0) {
            Exception exception =
                    new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "State for job " + job.getIdentifier()
                            + " not found on server");
            return new JobStatusImplementation(job, null, null, exception, false, false, map);
        }

        //FIXME: add isDone and exitcode for job
        return new JobStatusImplementation(job, state, null, null, state.equals("running"), false, map);
    }

    private static String checkSubmitJobResult(String output) throws OctopusIOException {
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

    private static void checkCancelJobResult(Job job, String qdelOutput) throws OctopusIOException {
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

    private static Map<String, String> getJobAccountingInfo(String output) throws OctopusIOException {
        Map<String, String> result = new HashMap<String, String>();

        String lines[] = output.split("\\r?\\n");
        for (String line : lines) {
            String[] elements = line.split(" ", 2);

            if (elements.length == 2) {
                result.put(elements[0].trim(), elements[1].trim());
            } else if (line.startsWith("================")) {
                //IGNORE first line
            } else {
                logger.debug("found line " + line + " in output");
            }
        }

        return result;

    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.octopus.adaptors.gridengine.CommandLineInterface#getJobs(nl.esciencecenter.octopus.adaptors.gridengine.SchedulerConnection, java.lang.String[])
     */
    @Override
    public Job[] getJobs(SchedulerConnection connection, String[] queueNames) throws OctopusIOException, OctopusException {
        String statusOutput = connection.runCommand(null, "qstat", "-xml");

        Map<String, Map<String, String>> status = parseJobInfos(statusOutput);

        String[] jobIDs = status.keySet().toArray(new String[0]);

        Job[] result = new Job[jobIDs.length];

        for (int i = 0; i < result.length; i++) {
            markJobSeen(jobIDs[i]);
            result[i] = new JobImplementation(connection.getScheduler(), jobIDs[i], null, false, false);
        }

        return result;
    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.octopus.adaptors.gridengine.CommandLineInterface#getQueueNames(nl.esciencecenter.octopus.adaptors.gridengine.SchedulerConnection)
     */
    @Override
    public String[] getQueueNames(SchedulerConnection connection) throws OctopusIOException, OctopusException {
        String qstatOutput = connection.runCommand(null, "qstat", "-xml", "-g", "c");

        Map<String, Map<String, String>> allMap = parseQueueInfos(qstatOutput);

        return allMap.keySet().toArray(new String[0]);
    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.octopus.adaptors.gridengine.CommandLineInterface#getQueueStatuses(nl.esciencecenter.octopus.adaptors.gridengine.SchedulerConnection, java.lang.String[])
     */
    @Override
    public QueueStatus[] getQueueStatuses(SchedulerConnection connection, String[] queueNames) throws OctopusIOException,
            OctopusException {
        QueueStatus[] result = new QueueStatus[queueNames.length];

        String qstatOutput = connection.runCommand(null, "qstat", "-xml", "-g", "c");

        Map<String, Map<String, String>> allMap = parseQueueInfos(qstatOutput);

        for (int i = 0; i < queueNames.length; i++) {
            if (allMap == null || allMap.isEmpty()) {
                Exception exception =
                        new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Failed to get status of queues on server");
                result[i] = new QueueStatusImplementation(connection.getScheduler(), queueNames[i], exception, null);
            }

            //state for only the requested job
            Map<String, String> map = allMap.get(queueNames[i]);

            if (map == null || map.isEmpty()) {
                Exception exception =
                        new NoSuchQueueException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get status of queue " + queueNames[i]
                                + " from server");
                result[i] = new QueueStatusImplementation(connection.getScheduler(), queueNames[i], exception, null);
            }

            result[i] = new QueueStatusImplementation(connection.getScheduler(), queueNames[i], null, map);
        }

        return result;

    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.octopus.adaptors.gridengine.CommandLineInterface#submitJob(nl.esciencecenter.octopus.adaptors.gridengine.SchedulerConnection, nl.esciencecenter.octopus.jobs.JobDescription)
     */
    @Override
    public Job submitJob(SchedulerConnection connection, JobDescription description) throws OctopusIOException,
            OctopusException {
        String jobScript = JobScriptGenerator.generate(description);

        String output = connection.runCommand(jobScript, "qsub");

        String identifier = checkSubmitJobResult(output);

        markJobSeen(identifier);
        return new JobImplementation(connection.getScheduler(), identifier, description, false, false);
    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.octopus.adaptors.gridengine.CommandLineInterface#cancelJob(nl.esciencecenter.octopus.adaptors.gridengine.SchedulerConnection, nl.esciencecenter.octopus.jobs.Job)
     */
    @Override
    public JobStatus cancelJob(SchedulerConnection connection, Job job) throws OctopusIOException, OctopusException {
        String output = connection.runCommand(null, "qdel", job.getIdentifier());

        checkCancelJobResult(job, output);

        return getJobStatus(connection, job);
    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.octopus.adaptors.gridengine.CommandLineInterface#getJobStatus(nl.esciencecenter.octopus.adaptors.gridengine.SchedulerConnection, nl.esciencecenter.octopus.jobs.Job)
     */
    @Override
    public JobStatus getJobStatus(SchedulerConnection connection, Job job) throws OctopusException, OctopusIOException {

        String statusOutput = connection.runCommand(null, "qstat", "-xml");

        Map<String, Map<String, String>> allMap = parseJobInfos(statusOutput);

        markJobsSeen(allMap.keySet());

        Map<String, String> map = allMap.get(job.getIdentifier());

        if (map == null || map.isEmpty()) {
            //perhaps the job is already finished?
            Map<String, String> accountingInfo = null;
            try {
                String output = connection.runCommand(null, "qacct", "-j", job.getIdentifier());

                accountingInfo = getJobAccountingInfo(output);
            } catch (CommandFailedException e) {
                logger.debug("qacct command failed", e);
            }

            if (accountingInfo != null) {
                Integer exitCode = null;

                String exitCodeString = accountingInfo.get("exit_status");

                try {
                    if (exitCodeString != null) {
                        exitCode = Integer.parseInt(exitCodeString);
                    }
                } catch (NumberFormatException e) {
                    throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "cannot parse exit code of job "
                            + job.getIdentifier() + " from string " + exitCodeString, e);

                }

                clearJobSeen(job.getIdentifier());
                return new JobStatusImplementation(job, "done", exitCode, null, false, true, accountingInfo);
            } else if (haveRecentlySeen(job.getIdentifier())) {
                return new JobStatusImplementation(job, "pending", null, null, false, true, accountingInfo);
            } else {
                Exception exception =
                        new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Job " + job.getIdentifier()
                                + " not found on server");
                return new JobStatusImplementation(job, null, null, exception, false, false, null);
            }
        }

        String state = map.get("state");

        if (state == null || state.length() == 0) {
            Exception exception =
                    new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "State for job " + job.getIdentifier()
                            + " not found on server");
            return new JobStatusImplementation(job, null, null, exception, false, false, map);
        }

        markJobSeen(job.getIdentifier());
        return new JobStatusImplementation(job, state, null, null, state.equals("running"), false, map);
    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.octopus.adaptors.gridengine.CommandLineInterface#getJobStatuses(nl.esciencecenter.octopus.adaptors.gridengine.SchedulerConnection, nl.esciencecenter.octopus.jobs.Job[])
     */
    @Override
    public JobStatus[] getJobStatuses(SchedulerConnection connection, Job[] jobs) throws OctopusIOException,
            OctopusException {
        JobStatus[] result = new JobStatus[jobs.length];

        for (int i = 0; i < result.length; i++) {
            if (jobs[i] == null) {
                result[i] = null;
            } else {
                result[i] = getJobStatus(connection, jobs[i]);
            }
        }
        return result;
    }

}
