package nl.esciencecenter.octopus.adaptors.gridengine;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.util.StreamForwarder;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

public class SchedulerConnection {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConnection.class);

    private static int schedulerID = 0;

    private static synchronized int getNextSchedulerID() {
        return schedulerID++;
    }

    private final URI actualLocation;

    private final XmlOutputParser parser;

    private final OctopusProperties properties;
    private final String[] queueNames;

    private String id;

    private InputStream runCommandAtServer(String command, String stdin) throws OctopusException, OctopusIOException {
        try {
            if (actualLocation.getHost() != null) {
                command = "ssh " + actualLocation.getHost() + " " + command;
            }
            logger.debug("running command " + command);
            Process process = Runtime.getRuntime().exec(command);

            if (stdin != null) {
                byte[] bytes = stdin.getBytes();
                InputStream in = new ByteArrayInputStream(bytes);

                new StreamForwarder(in, process.getOutputStream());
            }

            return process.getInputStream();
        } catch (IOException e) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "cannot execute remote process", e);
        }
    }

    public SchedulerConnection(URI location, Credential credential, Properties properties) throws OctopusIOException,
            OctopusException {
        this.properties = new OctopusProperties(properties);

        try {

            id = GridengineAdaptor.ADAPTOR_NAME + "-" + getNextSchedulerID();

            parser = new XmlOutputParser(this.properties);

            if (location.getHost() == null || location.getHost().length() == 0) {
                //FIXME: check if this works for encode uri's, illegal characters, fragments, etc..
                actualLocation = new URI("local:///");
            } else {
                actualLocation = new URI("ssh", location.getSchemeSpecificPart(), location.getFragment());
            }
        } catch (URISyntaxException e) {
            throw new OctopusException(GridengineAdaptor.ADAPTOR_NAME, "cannot create ssh uri from given location", e);
        }

        //get status of all queues, use names to fill queue name list
        this.queueNames = getQueueStatus().keySet().toArray(new String[0]);

        logger.debug("queues for " + location + " are " + Arrays.toString(this.queueNames));
    }

    //local operation
    public String[] getQueueNames() {
        return queueNames;
    }

    public OctopusProperties getProperties() {
        return properties;
    }

    public String getID() {
        return id;
    }

    public Map<String, Map<String, String>> getQueueStatus() throws OctopusException, OctopusIOException {
        try (InputStream in = runCommandAtServer("qstat -xml -g c", null)) {
            return parser.parseQueueInfos(in);
        } catch (IOException e) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "could not get queue status from server", e);
        }
    }

    public Map<String, Map<String, String>> getJobStatus() throws OctopusException, OctopusIOException {
        try (InputStream in = runCommandAtServer("qstat -xml", null)) {
            return parser.parseJobInfos(in);
        } catch (IOException e) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "could not get job status from server", e);
        }
    }

    /**
     * Submit a job to a sge machine
     * 
     * @param jobScript the script to submit
     * @return the id of the job
     * @throws OctopusException if the qsub command could not be run
     * @throws OctopusIOException  if the qsub command could not be run, or the jobID could not be read from the output
     */
    public String submitJob(String jobScript) throws OctopusException, OctopusIOException {
        try (InputStream in = runCommandAtServer("qsub", jobScript);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));) {
            //new StreamForwarder(in, System.err);

            String jobLine = reader.readLine();

            logger.debug("Submitted script. Got back line: \"" + jobLine + "\"");

            if (!jobLine.startsWith("Your job ") | jobLine.split(" ").length < 3) {
                throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get job id from qsub status message: \""
                        + jobLine + "\"");
            }

            String jobID = jobLine.split(" ")[2];

            try {
                int jobIDInt = Integer.parseInt(jobID);
                
                logger.debug("found job id: " + jobIDInt);
            } catch (NumberFormatException e) {
                throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get job id from qsub status message: \""
                        + jobLine + "\". Returned job id " + jobID + " does not seem to be a number", e);
            }
            return jobID;

        } catch (OctopusIOException e) {
            throw e;
        } catch (IOException e) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "could not submit job to server", e);
        }
    }

    public void close() {
        //FIXME: close ssh connection to server here

    }

};
