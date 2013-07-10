package nl.esciencecenter.octopus.adaptors.slurm;

import java.util.HashMap;
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
     *            The output of an sinfo/squeue/etc command, including an header. The fields contained in the output will be
     *            dynamically determined from the header line. The given "headerField" field is mandatory, and used as the key in
     *            the result map.
     * @throws OctopusException
     */
    public static Map<String, Map<String, String>> parseInfoOutput(String output, String keyField) throws OctopusException {
        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();

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
            for (int j = 0; j < fields.length; j++) {
                //remove status annotations
                if (values[j].endsWith("*") || values[j].endsWith("~")) {
                    values[j] = values[j].substring(0, values[j].length() - 1);
                }
                map.put(fields[j], values[j]);
            }

            if (!map.containsKey(keyField)) {
                throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "sinfo does not contain required field \"" + keyField
                        + "\"");

            }

            result.put(map.get(keyField), map);
        }

        return result;
    }

    public static Map<String, String> parseScontrolOutput(String output) throws OctopusException {
        Map<String, String> result = new HashMap<String, String>();

        String[] pairs = output.split(WHITESPACE_REGEX);

        for (String pair : pairs) {
            String[] elements = pair.split("=", 2);

            if (elements.length != 2) {
                throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Got unknown key/value pair in scontrol output: " + pair);
            }

            result.put(elements[0], elements[1]);
        }

        return result;
    }
}
