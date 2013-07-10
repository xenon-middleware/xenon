package nl.esciencecenter.octopus.adaptors.slurm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.octopus.exceptions.OctopusException;

public class SlurmOutputParser {

    static final String WHITESPACE_REGEX = "\\s+";

    static String parseSbatchOutput(String output) throws OctopusException {
        if (!(output.startsWith("Granted job allocation") || output.startsWith("Submitted batch job"))) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME,
                    "Failed to obtain job ID from sbatch output, output format not recognized: " + output);
        }

        String[] elements = output.split(WHITESPACE_REGEX);

        if (elements.length != 4) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME,
                    "Failed to obtain job ID from sbatch output, output format not recognized: " + output);
        }

        String identifier = elements[3];

        //check if the job ID we found is a number.
        try {
            Long.parseLong(identifier);
        } catch (NumberFormatException e) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME,
                    "Failed to obtain job ID from sbatch output, job ID is not a number: " + identifier);
        }

        return identifier;
    }

    public static void parseScancelOutput(String identifier, String scancelOutput) throws OctopusException {
        if (!scancelOutput.contains("scancel: Terminating job " + identifier)) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Did not get expected output on cancelling job. Got: "
                    + scancelOutput);
        }
    }

    /**
     * @param output
     *            the output of an sinfo command, including an header. The fields contained in the output will be dynamically
     *            determined from the header line.
     * @throws OctopusException
     */
    public static List<Map<String, String>> parseSinfoOutput(String output) throws OctopusException {
        ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();

        String[] lines = output.split("\\r?\\n");

        if (lines.length == 0) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME,
                    "Cannot parse sinfo output, Got empty output, expected at least a header");
        }

        String[] fields = lines[0].split(WHITESPACE_REGEX);

        for (int i = 1; i < lines.length; i++) {
            String[] values = lines[i].split(WHITESPACE_REGEX);

            if (fields.length != values.length) {
                throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Expected " + fields.length
                        + " field in sinfo output, got line with " + values.length + " values: " + lines[i]);
            }

            Map<String, String> map = new HashMap<String, String>();
            result.add(map);

            for (int j = 0; j < fields.length; j++) {
                //remove status annotations
                if (values[j].endsWith("*") || values[j].endsWith("~")) {
                    values[j] = values[j].substring(0,  values[j].length() - 1);
                }
                map.put(fields[j], values[j]);
            }
        }

        return result;
    }
}
