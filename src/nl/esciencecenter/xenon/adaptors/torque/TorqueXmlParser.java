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
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.util.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses xml output from TORQUE batch system.
 * 
 * @author Joris Borgdorff
 * 
 */
public class TorqueXmlParser extends DefaultHandler {

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

    private Map<String, String> recursiveMapFromElement(Element root) {
        Map<String, String> result = new HashMap<>();

        NodeList tagNodes = root.getChildNodes();

        //fetch tags from the list of tag nodes. Ignores empty values
        for (int j = 0; j < tagNodes.getLength(); j++) {
            Node tagNode = tagNodes.item(j);
            if (tagNode.getNodeType() == Node.ELEMENT_NODE) {
                String key = tagNode.getNodeName();
                if (key != null && key.length() > 0) {
                    NodeList children = tagNode.getChildNodes();
                    if (children.getLength() == 1 && children.item(0).getNodeType() == Node.TEXT_NODE) {
                        String value = children.item(0).getNodeValue();
                        result.put(key, value);
                    } else if (children.getLength() > 0) {
                        Map<String, String> childResult = recursiveMapFromElement((Element) tagNode);
                        result.putAll(childResult);
                    }
                }
            }
        }
        return result;
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
        Document document = parseDocument(data);

        LOGGER.debug("root node of xml file: " + document.getDocumentElement().getNodeName());
        NodeList nodes = document.getElementsByTagName("Data");
        Map<String, Map<String, String>> result = Utils.emptyMap(nodes.getLength());

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                Map<String, String> jobInfo = recursiveMapFromElement(element);

                String jobID = jobInfo.get("Job_Name");

                if (jobID == null || jobID.length() == 0) {
                    throw new XenonException(TorqueAdaptor.ADAPTOR_NAME, "found job in queue with no job number");
                }

                result.put(jobID, jobInfo);
            }
        }

        return result;
    }

}
