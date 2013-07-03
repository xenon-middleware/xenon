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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parses output from various grid engine command line tools.
 * 
 * @author Niels Drost
 * 
 */
public class GridEngineParser {

    private static final Logger logger = LoggerFactory.getLogger(GridEngineParser.class);

    //Tag containing version of xml schema used in qstat -xml output
    public static String SGE62_SCHEMA_ATTRIBUTE = "xmlns:xsd";

    public static String SGE62_SCHEMA_VALUE =
            "http://gridengine.sunsource.net/source/browse/*checkout*/gridengine/source/dist/util/resources/schemas/qstat/qstat.xsd?revision=1.11";

    private final DocumentBuilder documentBuilder;

    private static final String QACCT_HEADER = "==============================================================";

    private final boolean ignoreVersion;

    GridEngineParser(boolean ignoreVersion) throws OctopusIOException {
        this.ignoreVersion = ignoreVersion;

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "could not create parser for xml files", e);
        }
    }

    private void checkVersion(Document document) throws IncompatibleServerException {
        Element documentElement = document.getDocumentElement();

        if (documentElement == null || !documentElement.hasAttribute(SGE62_SCHEMA_ATTRIBUTE)
                || documentElement.getAttribute(SGE62_SCHEMA_ATTRIBUTE) == null) {

            if (ignoreVersion) {
                logger.warn("cannot determine version, version attribute not found. Ignoring as requested by "
                        + GridengineAdaptor.IGNORE_VERSION_PROPERTY);
            } else {

                throw new IncompatibleServerException(GridengineAdaptor.ADAPTOR_NAME,
                        "cannot determine version, version attribute not found. Use the "
                                + GridengineAdaptor.IGNORE_VERSION_PROPERTY + " property to ignore this error");
            }
        }

        String schemaValue = documentElement.getAttribute(SGE62_SCHEMA_ATTRIBUTE);

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
     * Testing version of checkVersion function
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

    /**
     * Parse qsub output, find and return the job ID.
     * 
     * @param output
     * @return
     * @throws OctopusIOException
     */
    String parseQsubOutput(String output) throws OctopusIOException {
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

    /**
     * Parses qdel output and check if we get the expected output.
     * 
     * @param identifier
     *            the identifier of the job that was deleted.
     * @param qdelOutput
     *            the output of the qdel command.
     * @return true if the job was deleted while still pending, false if it was/will be killed while running.
     * @throws OctopusIOException
     */
    boolean parseQdelOutput(String identifier, String qdelOutput) throws OctopusIOException {
        String[] stdoutLines = qdelOutput.split("\\r?\\n");

        String serverMessages = "output: " + Arrays.toString(stdoutLines);

        logger.debug("Deleted job. Got back " + serverMessages);

        if (stdoutLines.length == 0 || stdoutLines[0].isEmpty()) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get job delete status from qdel message: "
                    + serverMessages);
        }

        String[] elements = stdoutLines[0].split(" ");
        String[] withoutUser = Arrays.copyOfRange(elements, 1, elements.length);

        //two cases, 1 for running and one for pending jobs
        String[] killedOutput = { "has", "registered", "the", "job", identifier, "for", "deletion" };
        String[] deletedOutput = { "has", "deleted", "job", identifier };

        //this job was deleted. It will thus now disappear completely, forever.
        if (Arrays.equals(withoutUser, deletedOutput)) {
            return true;
        } else if (Arrays.equals(withoutUser, killedOutput)) {
            return false;
        } else {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get job delete status from qdel message: \""
                    + serverMessages + "\"");
        }
    }

    /**
     * Get all key/value data pairs from qacct output.
     * 
     * @param qacctOutput
     *            output of the qacct command.
     * @return key/value pairs with all data contained in the output.
     * @throws OctopusIOException
     */
    Map<String, String> parseQacctOutput(String qacctOutput) throws OctopusIOException {
        Map<String, String> result = new HashMap<String, String>();

        if (!qacctOutput.startsWith(QACCT_HEADER)) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Qacct output is excepted to start with " + QACCT_HEADER);
        }

        String lines[] = qacctOutput.split("\\r?\\n");
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
}
