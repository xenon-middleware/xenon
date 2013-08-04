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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.esciencecenter.octopus.exceptions.IncompatibleVersionException;
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
public class GridEngineXmlParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(GridEngineXmlParser.class);

    //Tag containing version of xml schema used in qstat -xml output
    public static final String SGE62_SCHEMA_ATTRIBUTE = "xmlns:xsd";

    public static final String SGE62_SCHEMA_VALUE = "http://gridengine.sunsource.net/source/browse/*checkout*/gridengine/source/dist/util/resources/schemas/qstat/qstat.xsd?revision=1.11";

    private final DocumentBuilder documentBuilder;

    private final boolean ignoreVersion;

    GridEngineXmlParser(boolean ignoreVersion) throws OctopusIOException {
        this.ignoreVersion = ignoreVersion;

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new OctopusIOException(GridEngineAdaptor.ADAPTOR_NAME, "could not create parser for xml files", e);
        }
    }

    private void checkVersion(Document document) throws IncompatibleVersionException {
        Element documentElement = document.getDocumentElement();

        if (documentElement == null || !documentElement.hasAttribute(SGE62_SCHEMA_ATTRIBUTE)
                || documentElement.getAttribute(SGE62_SCHEMA_ATTRIBUTE) == null) {

            if (ignoreVersion) {
                LOGGER.warn("cannot determine version, version attribute not found. Ignoring as requested by "
                        + GridEngineAdaptor.IGNORE_VERSION_PROPERTY);
            } else {

                throw new IncompatibleVersionException(GridEngineAdaptor.ADAPTOR_NAME,
                        "cannot determine version, version attribute not found. Use the "
                                + GridEngineAdaptor.IGNORE_VERSION_PROPERTY + " property to ignore this error");
            }
        }

        String schemaValue = documentElement.getAttribute(SGE62_SCHEMA_ATTRIBUTE);

        LOGGER.debug("found schema value " + schemaValue);

        //schemaValue == null checked above
        if (!SGE62_SCHEMA_VALUE.equals(schemaValue)) {
            if (ignoreVersion) {
                LOGGER.warn("cannot determine version, version attribute not found. Ignoring as requested by "
                        + GridEngineAdaptor.IGNORE_VERSION_PROPERTY);
            } else {

                throw new IncompatibleVersionException(GridEngineAdaptor.ADAPTOR_NAME, "schema version reported by server ("
                        + schemaValue + ") incompatible with adaptor. Use the " + GridEngineAdaptor.IGNORE_VERSION_PROPERTY
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
            throw new OctopusIOException(GridEngineAdaptor.ADAPTOR_NAME, "could not parse qstat xml file", e);
        }
    }

    private Document parseDocument(String data) throws OctopusException, OctopusIOException {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
            Document result = documentBuilder.parse(in);
            result.normalize();

            checkVersion(result);

            return result;
        } catch (SAXException | IOException e) {
            throw new OctopusIOException(GridEngineAdaptor.ADAPTOR_NAME, "could not parse qstat xml file", e);
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

        LOGGER.debug("root node of xml file: " + document.getDocumentElement().getNodeName());
        NodeList clusterNodes = document.getElementsByTagName("cluster_queue_summary");

        for (int i = 0; i < clusterNodes.getLength(); i++) {
            Node clusterNode = clusterNodes.item(i);

            if (clusterNode.getNodeType() == Node.ELEMENT_NODE) {
                Map<String, String> queueInfo = mapFromElement((Element) clusterNode);

                String queueName = queueInfo.get("name");

                if (queueName == null || queueName.length() == 0) {
                    throw new OctopusIOException(GridEngineAdaptor.ADAPTOR_NAME, "found queue in queue list with no name");
                }

                result.put(queueName, queueInfo);
            }
        }

        if (result.size() == 0) {
            throw new OctopusIOException(GridEngineAdaptor.ADAPTOR_NAME, "server seems to have no queues");
        }

        return result;
    }

    private Map<String, String> mapFromElement(Element root) throws OctopusIOException {
        Map<String, String> result = new HashMap<String, String>();

        NodeList tagNodes = root.getChildNodes();

        //fetch tags from the list of tag nodes. Ignores empty values
        for (int j = 0; j < tagNodes.getLength(); j++) {
            Node tagNode = tagNodes.item(j);
            if (tagNode.getNodeType() == Node.ELEMENT_NODE) {
                String key = tagNode.getNodeName();
                if (key != null && key.length() > 0) {
                    NodeList children = tagNode.getChildNodes();
                    if (children.getLength() > 0) {
                        String value = tagNode.getChildNodes().item(0).getNodeValue();
                        result.put(key, value);
                    }
                }
            }
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

        LOGGER.debug("root node of xml file: " + document.getDocumentElement().getNodeName());
        NodeList nodes = document.getElementsByTagName("job_list");

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                Map<String, String> jobInfo = mapFromElement(element);

                String state = element.getAttribute("state");

                if (state != null && state.length() > 0) {
                    jobInfo.put("long_state", state);
                }

                String jobID = jobInfo.get("JB_job_number");

                if (jobID == null || jobID.length() == 0) {
                    throw new OctopusIOException(GridEngineAdaptor.ADAPTOR_NAME, "found job in queue with no job number");
                }

                result.put(jobID, jobInfo);
            }
        }

        return result;
    }

}
