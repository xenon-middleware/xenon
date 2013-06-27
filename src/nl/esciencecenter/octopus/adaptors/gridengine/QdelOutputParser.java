package nl.esciencecenter.octopus.adaptors.gridengine;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.jobs.Job;

public class QdelOutputParser {
    
    private static final Logger logger = LoggerFactory.getLogger(QdelOutputParser.class);
    
    //command to use to delete job(s). append with job ID
    public static String[] QDEL_COMMAND = {"qdel"}; 

    public static void checkCancelJobResult(Job job, String qdelOutput) throws OctopusIOException {
        String[] stdoutLines = qdelOutput.split("\\r?\\n");

        String serverMessages = "output: " + Arrays.toString(stdoutLines);

        logger.debug("Deleted job. Got back " + serverMessages);

        if (stdoutLines.length == 0 || stdoutLines[0].isEmpty()) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get job delete status from qdel message: "
                    + serverMessages);
        }

        String[] elements = stdoutLines[0].split(" ");
        String[] withoutUser = Arrays.copyOfRange(elements, 1, elements.length);

        String identifier = job.getIdentifier();
        
        //two cases, 1 for running and one for pending jobs
        String[] expected1 = { "has", "registered", "the", "job", identifier, "for", "deletion" };
        String[] expected2 = { "has", "deleted", "job", identifier };

        if (!(Arrays.equals(withoutUser, expected1) || Arrays.equals(withoutUser, expected2))) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get job delete status from qdel message: \""
                    + serverMessages + "\"");
        }
    }
    
}
