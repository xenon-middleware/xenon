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
package nl.esciencecenter.octopus.adaptors.gridengine;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.exceptions.OctopusIOException;

public class TxtOutputParser {

    private static final Logger logger = LoggerFactory.getLogger(TxtOutputParser.class);

    public static void checkCancelJobResult(String identifier, String output) throws OctopusIOException {
        String[] stdoutLines = output.split("\\r?\\n");

        String serverMessages = "output: " + Arrays.toString(stdoutLines);

        logger.debug("Deleted job. Got back " + serverMessages);

        if (stdoutLines.length == 0 || stdoutLines[0].isEmpty()) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get job delete status from qdel message: "
                    + serverMessages);
        }

        String[] elements = stdoutLines[0].split(" ");
        String[] withoutUser = Arrays.copyOfRange(elements, 1, elements.length);

        //two cases, 1 for running and one for pending jobs
        String[] expected1 = { "has", "registered", "the", "job", identifier, "for", "deletion" };
        String[] expected2 = { "has", "deleted", "job", identifier };

        if (!(Arrays.equals(withoutUser, expected1) || Arrays.equals(withoutUser, expected2))) {
            throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get job delete status from qdel message: \""
                    + serverMessages + "\"");
        }
    }

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
