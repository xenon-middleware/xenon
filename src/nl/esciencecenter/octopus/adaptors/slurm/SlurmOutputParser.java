package nl.esciencecenter.octopus.adaptors.slurm;

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
}
