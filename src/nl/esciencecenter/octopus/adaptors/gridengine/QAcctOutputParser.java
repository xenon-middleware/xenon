package nl.esciencecenter.octopus.adaptors.gridengine;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.exceptions.OctopusIOException;

public class QAcctOutputParser {
    
    private static final Logger logger = LoggerFactory.getLogger(QAcctOutputParser.class);
    
    public static Map<String, String> getJobAccountingInfo(String output) throws OctopusIOException {
        Map<String, String> result = new HashMap<String, String>();

        String lines[] = output.split("\\r?\\n");
        for (String line : lines) {
            String[] elements = line.split(" ", 2);

            if (elements.length == 2) {
                result.put(elements[0].trim(), elements[1].trim());
            } else if (line.startsWith("================")) {
                //IGNORE first line
            } else {
                logger.debug("found line " + line + " in output");
            }
        }

        return result;

    }
}
