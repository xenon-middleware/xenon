package nl.esciencecenter.octopus.adaptors.slurm;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.exceptions.OctopusException;

public class SlurmOutputParser {

    private static final Logger logger = LoggerFactory.getLogger(SlurmOutputParser.class);

    static final String WHITESPACE_REGEX = "\\s+";

    static final String COMMA_REGEX = "\\s*,\\s*";

    static final String BAR_REGEX = "\\|";

    static final String NEW_LINE_REGEX = "\\r?\\n";

    static final String KEY_EQUALS_VALUE_REGEX = "\\s*=\\s*";

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

    /**
     * @param output
     *            The output of an sinfo/squeue/etc command, including an header. The fields contained in the output will be
     *            dynamically determined from the header line. The given "headerField" field is mandatory, and used as the key in
     *            the result map.
     * @throws OctopusException
     */
    public static Map<String, Map<String, String>> parseInfoOutput(String output, String keyField, String seperatorRegEx)
            throws OctopusException {
        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();

        String[] lines = output.split("\\r?\\n");

        if (lines.length == 0) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME,
                    "Cannot parse sinfo output, Got empty output, expected at least a header");
        }

        String[] fields = lines[0].split(seperatorRegEx);

        for (int i = 1; i < lines.length; i++) {
            String[] values = lines[i].split(seperatorRegEx);

            if (fields.length != values.length) {
                logger.debug("fields = {}, values = {}", fields, values);
                throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Expected " + fields.length
                        + " fields in sinfo output, got line with " + values.length + " values: " + lines[i]);
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
            String[] elements = pair.split(KEY_EQUALS_VALUE_REGEX, 2);

            if (elements.length != 2) {
                throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Got invalid key/value pair in scontrol output: " + pair);
            }

            result.put(elements[0], elements[1]);
        }

        return result;
    }

    public static Map<String, String> parseScontrolConfigOutput(String output) throws OctopusException {
        Map<String, String> result = new HashMap<String, String>();
        String[] lines = output.split(NEW_LINE_REGEX);

        if (lines.length == 0) {
            throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Cannot parse sinfo config output, Got empty output.");
        }

        for (String line : lines) {
            if (line.startsWith("Configuration data as of") || line.isEmpty() || line.startsWith("Slurmctld(primary/backup")) {
                //ignore these lines
                continue;
            }
            String[] pair = line.split(KEY_EQUALS_VALUE_REGEX, 2);

            if (pair.length != 2) {
                throw new OctopusException(SlurmAdaptor.ADAPTOR_NAME, "Got invalid key/value pair in scontrol output: " + pair);
            }

            result.put(pair[0], pair[1]);
        }

        return result;
    }
}
