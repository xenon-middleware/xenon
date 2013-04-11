package nl.esciencecenter.octopus.adaptors.gridengine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Path;

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
    public static String SGE62_VERSION_STRING =
            "http://gridengine.sunsource.net/source/browse/*checkout*/gridengine/source/dist/util/resources/schemas/qstat/qstat.xsd?revision=1.11";

    private final DocumentBuilder documentBuilder;

    private static void checkVersion(Document document) throws IncompatibleServerException {
        Element documentElement = document.getDocumentElement();
        
        if (documentElement == null) {
            throw  new IncompatibleServerException(GeAdaptor.ADAPTOR_NAME, "cannot determine version, no document element in xml");
        }
        
        
        
        System.out.println("root of xml file: " + document.getDocumentElement().getNodeName());
        
        NamedNodeMap attributes =  = document.getAttributes().getNamedItem("xmlns:xsd").getNodeValue();       
        
        logger.debug("name space found = ", xsdNamespace);
    }
    
    
    QstatOutputParser() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
    }

    Document getDocument(Octopus octopus, Path path) throws OctopusException, IOException, SAXException {
        try (InputStream in = octopus.files().newInputStream(path);) {
            Document result = documentBuilder.parse(in);
            result.normalize();
            
            checkVersion(result);
            
            return result;
        }
    }

    Document getDocument(File file) throws SAXException, IOException {
        Document result = documentBuilder.parse(file);
        //no we need this?
        result.normalize();
        
        checkVersion(result);

        return result;
    }

    String[] getQueues(Document document) throws Exception {
        System.out.println("root of xml file: " + document.getDocumentElement().getNodeName());
        NodeList nodes = document.getElementsByTagName("cluster_queue_summary");

        String[] result = new String[nodes.getLength()];

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                System.out.println("Queue name: " + getValue("name", element));

                result[i] = getValue("name", element);
            }
        }
        return result;
    }
    
    Map<String, String> getQueueInfo() {
        
        return null;
        
    }

    public static void main(String args[]) {
        try {
            
            QstatOutputParser parser = new QstatOutputParser();
            
            parser.getDocument(new File(args[0]));
            
            
           

//            System.out.println(Arrays.toString(getQueues()));
//
//            File qstatoutput = new File("/home/niels/workspace/octopus/adaptors/core/ge/jobs.xml");
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(qstatoutput);
//            doc.getDocumentElement().normalize();
//
//            System.out.println("root of xml file: " + doc.getDocumentElement().getNodeName());
//            NodeList nodes = doc.getElementsByTagName("job_list");
//
//            for (int i = 0; i < nodes.getLength(); i++) {
//                Node node = nodes.item(i);
//
//                if (node.getNodeType() == Node.ELEMENT_NODE) {
//                    Element element = (Element) node;
//
//                    System.out.println("Job Number: " + getValue("JB_job_number", element));
//                    System.out.println("Job Owner: " + getValue("JB_owner", element));
//
//                }
//
//            }

            //            System.out.println("==========================");
            //
            //            for (int i = 0; i < nodes.getLength(); i++) {
            //                Node node = nodes.item(i);
            //
            //                if (node.getNodeType() == Node.ELEMENT_NODE) {
            //                    Element element = (Element) node;
            //                    System.out.println("Stock Symbol: " + getValue("symbol", element));
            //                    System.out.println("Stock Price: " + getValue("price", element));
            //                    System.out.println("Stock Quantity: " + getValue("quantity", element));
            //                }
            //            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String getValue(String tag, Element element) {
        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodes.item(0);
        return node.getNodeValue();
    }
}
