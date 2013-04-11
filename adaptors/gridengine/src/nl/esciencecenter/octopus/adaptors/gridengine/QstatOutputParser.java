package nl.esciencecenter.octopus.adaptors.gridengine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class QstatOutputParser {

    private static final Logger logger = LoggerFactory.getLogger(QstatOutputParser.class);

    //string in the xmlns:xsd tag of qstat -xml
    public static String SGE62_SCHEMA_ATTRIBUTE = "xmlns:xsd";

    public static String SGE62_SCHEMA_VALUE =
            "http://gridengine.sunsource.net/source/browse/*checkout*/gridengine/source/dist/util/resources/schemas/qstat/qstat.xsd?revision=1.11";

    private final DocumentBuilder documentBuilder;

    private final boolean ignoreVersion;

    void checkVersion(Document document) throws IncompatibleServerException {

        Element de = document.getDocumentElement();

        if (de == null || !de.hasAttribute(SGE62_SCHEMA_ATTRIBUTE) || de.getAttribute(SGE62_SCHEMA_ATTRIBUTE) == null) {

            if (ignoreVersion) {
                logger.warn("cannot determine version, version attribute not found. Ignoring as requested by "
                        + GridEngineAdaptor.IGNORE_VERSION_PROPERTY);
            } else {

                throw new IncompatibleServerException(GridEngineAdaptor.ADAPTOR_NAME,
                        "cannot determine version, version attribute not found. Use the "
                                + GridEngineAdaptor.IGNORE_VERSION_PROPERTY + " property to ignore this error");
            }
        }

        String schemaValue = de.getAttribute(SGE62_SCHEMA_ATTRIBUTE);

        logger.debug("found schema value " + schemaValue);

        //schemaValue == null checked above
        if (!SGE62_SCHEMA_VALUE.equals(schemaValue)) {
            if (ignoreVersion) {
                logger.warn("cannot determine version, version attribute not found. Ignoring as requested by "
                        + GridEngineAdaptor.IGNORE_VERSION_PROPERTY);
            } else {

                throw new IncompatibleServerException(GridEngineAdaptor.ADAPTOR_NAME, "schema version reported by server ("
                        + schemaValue + ") incompatible with adaptor. Use the " + GridEngineAdaptor.IGNORE_VERSION_PROPERTY
                        + " property to ignore this error");
            }
        }

    }

    QstatOutputParser(boolean ignoreVersion) throws OctopusIOException {
        this.ignoreVersion = ignoreVersion;

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new OctopusIOException(GridEngineAdaptor.ADAPTOR_NAME, "could not create parser for qstat xml files", e);
        }
    }

    Document getDocument(Octopus octopus, AbsolutePath path) throws OctopusException, OctopusIOException {
        try (InputStream in = octopus.files().newInputStream(path);) {
            Document result = documentBuilder.parse(in);
            result.normalize();

            checkVersion(result);

            return result;
        } catch (SAXException | IOException e) {
            throw new OctopusIOException(GridEngineAdaptor.ADAPTOR_NAME, "could not parse qstat xml file", e);
        }
    }

    Document getDocument(File file) throws IncompatibleServerException, OctopusIOException {
        try {
            Document result = documentBuilder.parse(file);
            //no we need this?
            result.normalize();

            checkVersion(result);

            return result;
        } catch (SAXException | IOException e) {
            throw new OctopusIOException(GridEngineAdaptor.ADAPTOR_NAME, "could not parse qstat xml file: " + file, e);
        }
    }

    /**
     * Fetches info from "qstat -g c -xml"
     * 
     * @param document
     *            the xml document to fetch info from
     * @return a list containing all queue names found
     * @throws OctopusIOException
     * @throws Exception
     */
    Map<String, Map<String, String>> getQueueInfos(Document document) throws OctopusIOException {
        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();

        logger.debug("root node of xml file: " + document.getDocumentElement().getNodeName());
        NodeList nodes = document.getElementsByTagName("cluster_queue_summary");

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

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

                    throw new OctopusIOException(GridEngineAdaptor.ADAPTOR_NAME, "found queue in queue list with no name");
                }

                result.put(queueName, queueInfo);
            }

            //                System.out.println("Queue name: " + getValue("name", element));
            //
            //                NodeList nameElements = element.getElementsByTagName("name");
            //
            //                if (nameElements.getLength() == 0 || nameElements.item(0).getChildNodes().getLength() == 0) {
            //                    throw new OctopusIOException(GridEngineAdaptor.ADAPTOR_NAME, "found queue in queue list with no name");
            //                }
            //
            //                Node nameNode = nameElements.item(0).getChildNodes().item(0);
            //
            //                result[i] = node.getNodeValue();
            //
            //                if (result[i] == null || result[i].length() == 0) {
            //                    throw new OctopusIOException(GridEngineAdaptor.ADAPTOR_NAME, "found queue in queue list with no name");
            //                }
            //            } else {
            //                throw new OctopusIOException(GridEngineAdaptor.ADAPTOR_NAME, "illegal xml file for queue information");
            //            }

        }

        if (result.size() == 0) {
            throw new OctopusIOException(GridEngineAdaptor.ADAPTOR_NAME, "server seems to have no queues");
        }

        return result;
    }

    /**
     * Fetches list of queues from "qstat -g c -xml"
     * 
     * @param document
     *            the xml document to fetch info from
     * @return a list containing all queue names found
     * @throws OctopusIOException
     * @throws Exception
     */
    String[] getQueues(Document document) throws OctopusIOException {
        return getQueueInfos(document).keySet().toArray(new String[0]);
    }

}
