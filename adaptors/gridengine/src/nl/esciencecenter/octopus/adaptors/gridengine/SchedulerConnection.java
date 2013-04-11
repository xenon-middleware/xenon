package nl.esciencecenter.octopus.adaptors.gridengine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

public class SchedulerConnection {
    
    private static final Logger logger = LoggerFactory.getLogger(SchedulerConnection.class);
    
    private static int schedulerID = 0;

    private static synchronized int getNextSchedulerID() {
        return schedulerID++;
    }

    private final URI actualLocation;

    private final QstatOutputParser parser;

    private final OctopusProperties properties;
    private final String[] queueNames;

    private String id;

    private Document getDocumentFromServer(String command) throws OctopusException, OctopusIOException {
        try {
            if (actualLocation.getHost() != null) {
                command = "ssh " + actualLocation.getHost() + " " + command;
            } 
            logger.debug("running command " + command);
            Process process = Runtime.getRuntime().exec(command);
            return parser.getDocument(process.getInputStream());
        } catch (IOException e) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "cannot execute remote process", e);
        }
    }
    
    
    public SchedulerConnection(URI location, Credential credential, Properties properties) throws OctopusIOException,
            OctopusException {
        this.properties = new OctopusProperties(properties);

        try {

            id = GridengineAdaptor.ADAPTOR_NAME + "-" + getNextSchedulerID();

            parser = new QstatOutputParser(this.properties);

            if (location.getHost() == null || location.getHost().length() == 0) {
                //FIXME: check if this works for encode uri's, illegal characters, fragments, etc..
                actualLocation = new URI("local:///");
            } else {
                actualLocation = new URI("ssh", location.getSchemeSpecificPart(), location.getFragment());
            }

            Document queueXml = getDocumentFromServer("qstat -xml -g c");
            this.queueNames = parser.getQueues(queueXml);
            
            logger.debug("queues for " + location + " are " + Arrays.toString(this.queueNames));

        } catch (URISyntaxException e) {
            throw new OctopusException(GridengineAdaptor.ADAPTOR_NAME, "cannot create ssh uri from given location", e);
        }

    }

    //local operation
    public String[] getQueueNames() {
        return queueNames;
    }

    public OctopusProperties getProperties() {
        return properties;
    }

    public String getUniqueID() {
        return id;
    }

    public Map<String, Map<String, String>> getQueueStatus() throws OctopusIOException, OctopusException {
        Document document = getDocumentFromServer("qstat -xml -g c");
        return parser.getQueueInfos(document);
    }
}
