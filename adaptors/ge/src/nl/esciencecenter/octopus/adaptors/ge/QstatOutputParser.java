package nl.esciencecenter.octopus.adaptors.ge;

import java.io.File;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.files.Path;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class QstatOutputParser {

    public static String[] KNOWN_VERSIONS = { "1.11" };

    QstatOutputParser(Octopus octopus, Path path) {
        

    }
    
    static String[] getQueues() throws Exception {
        
        File qstatoutput = new File("/home/niels/workspace/octopus/adaptors/core/ge/queues.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(qstatoutput);
        doc.getDocumentElement().normalize();

        System.out.println("root of xml file: " + doc.getDocumentElement().getNodeName());
        NodeList nodes = doc.getElementsByTagName("cluster_queue_summary");

        String[] result = new String[nodes.getLength()];
        
        for(int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
              Element element = (Element) node;
              
              System.out.println("Queue name: " + getValue("name", element));

              result[i] = getValue("name", element);
          }
        }
        return result;
    }

    public static void main(String args[]) {
        try {
            
            System.out.println(Arrays.toString(getQueues()));

            File qstatoutput = new File("/home/niels/workspace/octopus/adaptors/core/ge/jobs.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(qstatoutput);
            doc.getDocumentElement().normalize();

            System.out.println("root of xml file: " + doc.getDocumentElement().getNodeName());
            NodeList nodes = doc.getElementsByTagName("job_list");

            for(int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                  Element element = (Element) node;
                  
                  System.out.println("Job Number: " + getValue("JB_job_number", element));
                  System.out.println("Job Owner: " + getValue("JB_owner", element));

                  
              }
                
            }
            
            
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
