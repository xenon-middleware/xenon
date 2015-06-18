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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.scripting.ScriptingParser;
import nl.esciencecenter.xenon.util.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parses xml output from TORQUE batch system.
 * 
 * @author Joris Borgdorff
 * 
 */
final class TorqueXmlParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(TorqueXmlParser.class);

    private final DocumentBuilder documentBuilder;

    TorqueXmlParser() throws XenonException {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new XenonException(TorqueAdaptor.ADAPTOR_NAME, "could not create parser for xml files", e);
        }
    }

    Document parseDocument(String data) throws XenonException {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
            Document result = documentBuilder.parse(in);
            result.normalize();
            return result;
        } catch (SAXException e) {
            throw new XenonException(TorqueAdaptor.ADAPTOR_NAME, "could not parse qstat xml file", e);
        } catch (IOException e) {
            throw new XenonException(TorqueAdaptor.ADAPTOR_NAME, "could not read xml file", e);
        }
    }

    /**
     * Create a Map from the tag names and text values of child nodes.
     * If a child node also has tags, those tags and their text values will also
     * be added. If multiple of the same tag names occur, the value of the last
     * occurrence will be stored.
     * 
     * @param root XML element of which the children will be added to the map
     * @param result a mutable map that will have added to it tag names as keys and text values as values
     * @throws IllegalArgumentException if root is not an XML element
     */
    // not private for testing purposes
    void recursiveMapFromElement(Node root, Map<String, String> result) {
        if (root.getNodeType() != Node.ELEMENT_NODE) {
            throw new IllegalArgumentException("Node " + root + " is not an XML element.");
        }
        NodeList tagNodes = root.getChildNodes();

        //fetch tags from the list of tag nodes. Ignores empty values
        for (int i = 0; i < tagNodes.getLength(); i++) {
            Node tagNode = tagNodes.item(i);
            if (tagNode.getNodeType() == Node.ELEMENT_NODE) {
                recursiveMapFromElement(tagNode, result);
            } else if (tagNode.getNodeType() == Node.TEXT_NODE) {
                String value = tagNode.getNodeValue().trim();
                if (!value.isEmpty()) {
                    result.put(root.getNodeName(), value);
                }
            }
        }
    }

    /**
     * Parses job info from "qstat -x"
     * 
     * @param in
     *            the stream to get the xml data from
     * @return a list containing all queue names found
     * @throws XenonException
     *             if the file could not be parsed
     * @throws XenonException
     *             if the server version is not compatible with this adaptor
     * @throws Exception
     */
    Map<String, Map<String, String>> parseJobInfos(String data) throws XenonException {
        if (data.trim().isEmpty()) {
            return Utils.emptyMap(0);
        }
        Document document = parseDocument(data);

        LOGGER.debug("root node of xml file: " + document.getDocumentElement().getNodeName());
        NodeList nodes = document.getDocumentElement().getChildNodes();
        int numNodes = nodes.getLength();
        Map<String, Map<String, String>> result = Utils.emptyMap(numNodes);

        for (int i = 0; i < numNodes; i++) {
            Node node = nodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Map<String, String> jobInfo = Utils.emptyMap(20);
                recursiveMapFromElement(node, jobInfo);

                String jobID = String.valueOf(ScriptingParser.parseJobIDFromLine(jobInfo.get("Job_Id"), TorqueAdaptor.ADAPTOR_NAME, ""));

                if (jobID == null || jobID.isEmpty()) {
                    throw new XenonException(TorqueAdaptor.ADAPTOR_NAME, "found job in queue with no job number");
                }

                jobInfo.put("Job_Id_Number", jobID);

                result.put(jobID, jobInfo);
            }
        }

        return result;
    }
}
