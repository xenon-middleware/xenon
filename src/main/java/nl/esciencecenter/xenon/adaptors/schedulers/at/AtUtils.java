package nl.esciencecenter.xenon.adaptors.schedulers.at;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import nl.esciencecenter.xenon.schedulers.JobDescription;

public class AtUtils {

    public AtUtils() {
        // utility class
    }

    private static void parseJobLine(String line, Set<String> queues, HashMap<String, Map<String, String>> result) {

        // Check if there is anything to parse
        if (line == null || line.trim().isEmpty()) {
            return;
        }

        String[] fields = line.trim().split("\\s+");

        // Check if we got the right number of fields
        if (fields.length != 8) {
            throw new IllegalArgumentException("Failed to parse AT job line: " + line);
        }

        // Check if we are interested in this queue. If so, add the result
        if (queues == null || queues.isEmpty() || queues.contains(fields[6])) {
            HashMap<String, String> tmp = new HashMap<>();
            tmp.put("jobID", fields[0]);
            tmp.put("startDate", fields[1] + " " + fields[2] + " " + fields[3] + " " + fields[4] + " " + fields[5]);
            tmp.put("queue", fields[6]);
            tmp.put("user", fields[7]);
            result.put(fields[0], tmp);
        }
    }

    /**
     * Parse one or more lines of queue info as produced by <code>atq</code>.
     * 
     * These lines have the following syntax:
     * 
     * [jobID] [weekday] [month] [dayOfMonth] [time] [year] [queue] [user]
     * 
     * For example:
     * 
     * 11 Mon Jul 2 10:22:00 2018 a jason
     * 
     * The parsed output will be split into "jobID", "startDate", "queue" and "user" data, which is combined in a Map (each using the respective keys). Each of
     * this Maps is stored is a larger Map using the "jobID" as a key. This larger map is returned as the return value this method.
     * 
     * For example, the example line above will result in the following return value: <code> 
     * Map("11":Map("jobID":"11", "startDate":"Mon Jul 2 10:22:00 2018", "queue":"a", "user":"jason"))
     * </code> If a set of queue names is provided in <code>queues</code>, only jobs from a matching queue will be returned in results. If
     * <code>queues<code> is 
     * <code>null</code> or empty, all jobs from all queues will be returned.
     * 
     * @param atqOutput
     *            the output as produced by atq
     * @param queues
     *            the queues to return the jobs for.
     * @return the parsed output
     */
    public static HashMap<String, Map<String, String>> parseJobInfo(String atqOutput, Set<String> queues) {

        HashMap<String, Map<String, String>> result = new HashMap<>();

        if (atqOutput == null || atqOutput.isEmpty()) {
            return result;
        }
        String[] lines = atqOutput.split("\\r?\\n");

        for (String line : lines) {
            parseJobLine(line, queues, result);
        }

        return result;
    }

    public static String[] getJobIDs(Map<String, Map<String, String>> jobs) {

        if (jobs == null || jobs.isEmpty()) {
            return new String[0];
        }

        Set<String> ids = jobs.keySet();
        return ids.toArray(new String[ids.size()]);
    }

    public static String generateJobScript(JobDescription description) {
        // TODO Auto-generated method stub
        return null;
    }
}
