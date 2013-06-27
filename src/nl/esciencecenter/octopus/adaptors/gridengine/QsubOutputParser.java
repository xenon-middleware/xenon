package nl.esciencecenter.octopus.adaptors.gridengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.exceptions.OctopusIOException;

public class QsubOutputParser {
    
    private static final Logger logger = LoggerFactory.getLogger(QsubOutputParser.class);

    public static String checkSubmitJobResult(String output) throws OctopusIOException {
        String lines[] = output.split("\\r?\\n");

        if (lines.length == 0 || !lines[0].startsWith("Your job ") | lines[0].split(" ").length < 3) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get job id from qsub status message: "
                    + output);
        }

        String jobID = lines[0].split(" ")[2];

        try {
            int jobIDInt = Integer.parseInt(jobID);

            logger.debug("found job id: " + jobIDInt);
        } catch (NumberFormatException e) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get job id from qsub status message: \""
                    + output + "\". Returned job id " + jobID + " does not seem to be a number", e);
        }
        return jobID;
    }
    
}
