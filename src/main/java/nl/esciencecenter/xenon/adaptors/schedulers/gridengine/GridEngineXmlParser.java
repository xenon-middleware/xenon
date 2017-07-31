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
package nl.esciencecenter.xenon.adaptors.schedulers.gridengine;

import static nl.esciencecenter.xenon.adaptors.schedulers.gridengine.GridEngineSchedulerAdaptor.ADAPTOR_NAME;
import static nl.esciencecenter.xenon.adaptors.schedulers.gridengine.GridEngineSchedulerAdaptor.IGNORE_VERSION_PROPERTY;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.IncompatibleVersionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parses xml output from various grid engine command line tools. For more info on the output, see the
 * "N1 Grid Engine 6 User's Guide". Retrieved from: http://docs.oracle.com/cd/E19080-01/n1.grid.eng6/817-6117/chp11-1/index.html
 * 
 */
public class GridEngineXmlParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(GridEngineXmlParser.class);

    //Tag containing version of xml schema used in qstat -xml output
    private static final String SGE62_SCHEMA_ATTRIBUTE = "xmlns:xsd";

    private static final String SGE62_SCHEMA_VALUE = "http://gridengine.sunsource.net/source/browse/*checkout*/gridengine/source/dist/util/resources/schemas/qstat/qstat.xsd?revision=1.11";

    private final DocumentBuilder documentBuilder;

    private final boolean ignoreVersion;

    GridEngineXmlParser(boolean ignoreVersion) throws XenonException {
        this.ignoreVersion = ignoreVersion;

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new XenonException(ADAPTOR_NAME, "could not create parser for xml files", e);
        }
    }

    private void checkVersion(Document document) throws IncompatibleVersionException {
        Element documentElement = document.getDocumentElement();

        if (!documentElement.getAttribute(SGE62_SCHEMA_ATTRIBUTE).equals(SGE62_SCHEMA_VALUE)) {
            if (ignoreVersion) {
                LOGGER.warn("cannot determine version, version attribute found: \""
                        + documentElement.getAttribute(SGE62_SCHEMA_ATTRIBUTE) + "\". Ignoring as requested by "
                        + IGNORE_VERSION_PROPERTY);
            } else {
                throw new IncompatibleVersionException(ADAPTOR_NAME,
                        "cannot determine version, version attribute found: \""
                                + documentElement.getAttribute(SGE62_SCHEMA_ATTRIBUTE) + "\". Use the "
                                + IGNORE_VERSION_PROPERTY + " property to ignore this error");
            }

        }
    }

    protected Document parseDocument(String data) throws XenonException {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
            Document result = documentBuilder.parse(in);
            result.normalize();

            checkVersion(result);

            return result;
        } catch (SAXException e) {
            throw new XenonException(ADAPTOR_NAME, "could not parse qstat xml file", e);
        } catch (IOException e) {
            throw new XenonException(ADAPTOR_NAME, "could not read xml file", e);
        }
    }

    private Map<String, String> mapFromElement(Element root) {
        Map<String, String> result = new HashMap<>();

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
     * Parses queue info from "qstat -g c -xml"
     * 
     * @param input
     *            the stream to get the xml data from
     * @return a list containing all queue names found
     * @throws XenonException
     *             if the file could not be parsed
     * @throws XenonException
     *             if the server version is not compatible with this adaptor
     */
    protected Map<String, Map<String, String>> parseQueueInfos(String input) throws XenonException {
        Document document = parseDocument(input);

        Map<String, Map<String, String>> result = new HashMap<>();

        LOGGER.debug("root node of xml file: " + document.getDocumentElement().getNodeName());
        NodeList clusterNodes = document.getElementsByTagName("cluster_queue_summary");

        for (int i = 0; i < clusterNodes.getLength(); i++) {
            Node clusterNode = clusterNodes.item(i);

            if (clusterNode.getNodeType() == Node.ELEMENT_NODE) {
                Map<String, String> queueInfo = mapFromElement((Element) clusterNode);

                String queueName = queueInfo.get("name");

                if (queueName == null || queueName.length() == 0) {
                    throw new XenonException(ADAPTOR_NAME, "found queue in queue list with no name");
                }

                result.put(queueName, queueInfo);
            }
        }

        if (result.size() == 0) {
            throw new XenonException(ADAPTOR_NAME, "server seems to have no queues");
        }

        return result;
    }

    /**
     * Parses job info from "qstat -xml"
     * 
     * @param data
     *            the stream to get the xml data from
     * @return a list containing all queue names found
     * @throws XenonException
     *             if the file could not be parsed
     * @throws XenonException
     *             if the server version is not compatible with this adaptor
     */
    protected Map<String, Map<String, String>> parseJobInfos(String data) throws XenonException {
        Map<String, Map<String, String>> result = new HashMap<>();

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
                    throw new XenonException(ADAPTOR_NAME, "found job in queue with no job number");
                }

                result.put(jobID, jobInfo);
            }
        }

        return result;
    }

}
