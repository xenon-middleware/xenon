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
package nl.esciencecenter.xenon.adaptors.schedulers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.filesystems.Path;
import nl.esciencecenter.xenon.schedulers.IncompleteJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.InvalidJobDescriptionException;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.NoSuchQueueException;
import nl.esciencecenter.xenon.schedulers.Scheduler;

public class ScriptingUtils {

    public ScriptingUtils() {
        // empty as this is a utility class
    }

    public static boolean isLocal(String location) {
        return (location == null || location.length() == 0 || location.startsWith("local://"));
    }

    public static boolean isSSH(String location) {
        return (location != null && location.startsWith("ssh://"));
    }

    public static XenonPropertyDescription[] mergeValidProperties(XenonPropertyDescription[]... prop) {

        if (prop == null || prop.length == 0) {
            return new XenonPropertyDescription[0];
        }

        ArrayList<XenonPropertyDescription> tmp = new ArrayList<>();

        for (XenonPropertyDescription[] pa : prop) {
            if (pa != null) {
                tmp.addAll(Arrays.asList(pa));
            }
        }

        return tmp.toArray(new XenonPropertyDescription[tmp.size()]);
    }

    public static XenonProperties getProperties(XenonPropertyDescription[] validProperties, String location, Map<String, String> properties)
            throws XenonException {

        if (isLocal(location)) {
            return new XenonProperties(mergeValidProperties(validProperties, Scheduler.getAdaptorDescription("local").getSupportedProperties()), properties);
        } else {
            return new XenonProperties(mergeValidProperties(validProperties, Scheduler.getAdaptorDescription("ssh").getSupportedProperties()), properties);
        }
    }

    /**
     * Verify a String containing a start time.
     * 
     * Currently supported values are "now", or an explicit time and date in the format "HH:mm[ dd.MM[.YYYY]]"
     *
     * @param startTime
     *            the start time to parse
     * @throws XenonException
     *             if the startTime does not have an accepted format
     */
    public static void verifyStartTime(String startTime, String adaptorName) throws XenonException {

        if (startTime == null || startTime.isEmpty()) {
            throw new XenonException(adaptorName, "Start time must not be null or empty!");
        }

        if (startTime.equals("now")) {
            return;
        }

        try {
            // If the parsing succeeds, we fall through and return.
            if (startTime.length() == 16) {
                new SimpleDateFormat("HH:mm dd.MM.yyyy").parse(startTime);
            } else if (startTime.length() == 11) {
                new SimpleDateFormat("HH:mm dd.MM").parse(startTime);
            } else if (startTime.length() == 5) {
                new SimpleDateFormat("HH:mm").parse(startTime);
            } else {
                throw new XenonException(adaptorName, "Failed to parse start time: " + startTime);
            }
        } catch (ParseException e) {
            throw new XenonException(adaptorName, "Failed to parse start time: " + startTime, e);
        }
    }

    /**
     * Retrieve a working directory from a <code>JobDescription</code> and, if necessary, resolve it against a current working directory.
     * 
     * This method retrieves the working directory from a <code>JobDescription</code>. If it is not specified, the <code>currentWorkingDir</code> will be
     * returned. If it is specified and an absolute path, it will be returned directly. Otherwise, it will first be resolved against the provided
     * <code>currentWorkingDir</code> and the resulting path will be returned.
     * 
     * @param description
     *            the JobDescription containing the workingDirectory
     * @param currentWorkingDir
     *            the current working directory of the adaptor.
     */
    public static String getWorkingDirPath(JobDescription description, Path currentWorkingDir) {

        String dir = description.getWorkingDirectory();

        if (dir == null || dir.trim().isEmpty()) {
            // return the current working directory
            return currentWorkingDir.toString();
        } else if (dir.startsWith("/")) {
            // return the absolute directory specified by the user
            return dir;
        } else {
            // make the relative directory absolute
            return currentWorkingDir.resolve(dir).toString();
        }
    }

    /**
     * Check if the given <code>queueName</code> is presents in <code>queueNames</code>.
     *
     * If <code>queueName</code> is <code>null</code> or <code>queueName</code> is present in <code>queueNames</code> this method will return. Otherwise it will
     * throw a <code>NoSuchQueueException</code>.
     *
     * @param queueNames
     *            the valid queue names.
     * @param queueName
     *            the queueName to check.
     * @throws NoSuchQueueException
     *             if workingDirectory does not exist, or an error occurred.
     */
    public static void checkQueue(String[] queueNames, String queueName, String adaptorName) throws NoSuchQueueException {
        if (queueName == null || queueNames == null || queueName.length() == 0) {
            return;
        }

        for (String q : queueNames) {
            if (queueName.equals(q)) {
                return;
            }
        }

        throw new NoSuchQueueException(adaptorName, "Queue does not exist: " + queueName);
    }

    /**
     * Do some checks on a job description.
     *
     * @param description
     *            the job description to check
     * @param adaptorName
     *            the name of the adaptor. Used when an exception is thrown
     * @throws IncompleteJobDescriptionException
     *             if the description is missing a mandatory value.
     * @throws InvalidJobDescriptionException
     *             if the description contains illegal values.
     */
    public static void verifyJobDescription(JobDescription description, String[] queueNames, String adaptorName) throws XenonException {
        String executable = description.getExecutable();

        if (executable == null) {
            throw new IncompleteJobDescriptionException(adaptorName, "Executable missing in JobDescription!");
        }

        int nodeCount = description.getNodeCount();

        if (nodeCount < 1) {
            throw new InvalidJobDescriptionException(adaptorName, "Illegal node count: " + nodeCount);
        }

        int processesPerNode = description.getProcessesPerNode();

        if (processesPerNode < 1) {
            throw new InvalidJobDescriptionException(adaptorName, "Illegal processes per node count: " + processesPerNode);
        }

        int maxTime = description.getMaxRuntime();

        if (maxTime <= 0) {
            throw new InvalidJobDescriptionException(adaptorName, "Illegal maximum runtime: " + maxTime);
        }

        checkQueue(queueNames, description.getQueueName(), adaptorName);

        verifyStartTime(description.getStartTime(), adaptorName);
    }

    public static void verifyJobOptions(Map<String, String> options, String[] validOptions, String adaptorName) throws InvalidJobDescriptionException {

        // check if all given job options are valid
        for (String option : options.keySet()) {
            boolean found = false;
            for (String validOption : validOptions) {
                if (validOption.equals(option)) {
                    found = true;
                }
            }
            if (!found) {
                throw new InvalidJobDescriptionException(adaptorName, "Given Job option \"" + option + "\" not supported");
            }
        }
    }

    /**
     * Check if the info map for a job exists, contains the expected job ID, and contains the given additional fields
     *
     * @param jobInfo
     *            the info map to check.
     * @param jobIdentifier
     *            the unique identifier of the job.
     * @param adaptorName
     *            name of the current adaptor for error reporting.
     * @param jobIDField
     *            the field which contains the job id.
     * @param additionalFields
     *            any additional fields to check the presence of.
     * @throws XenonException
     *             if any fields are missing or incorrect
     */
    public static void verifyJobInfo(Map<String, String> jobInfo, String jobIdentifier, String adaptorName, String jobIDField, String... additionalFields)
            throws XenonException {

        if (jobInfo == null) {
            // redundant check, calling functions usually already check for this and return null.
            throw new XenonException(adaptorName, "Job " + jobIdentifier + " not found in job info");
        }

        String jobID = jobInfo.get(jobIDField);

        if (jobID == null) {
            throw new XenonException(adaptorName, "Invalid job info. Info does not contain job id");
        }

        if (!jobID.equals(jobIdentifier)) {
            throw new XenonException(adaptorName, "Invalid job info. Found job id \"" + jobID + "\" does not match " + jobIdentifier);
        }

        for (String field : additionalFields) {
            if (!jobInfo.containsKey(field)) {
                throw new XenonException(adaptorName, "Invalid job info. Info does not contain mandatory field \"" + field + "\"");
            }
        }
    }

    /**
     * Concatinate a series of <code>String</code>s using a <code>StringBuilder</code>.
     *
     * @param strings
     *            Strings to concatinate. Any Strings that are <code>null</code> will be ignored.
     *
     * @return the concatination of the provided strings, or the empty string is no strings where provided.
     */
    public static String concat(String... strings) {

        if (strings == null || strings.length == 0) {
            return "";
        }

        StringBuilder b = new StringBuilder("");

        for (String s : strings) {
            if (s != null && !s.isEmpty()) {
                b.append(s);
            }
        }

        return b.toString();
    }

    /**
     * Create a single comma separated string out of a list of strings. Will ignore null values
     *
     * @param values
     *            an array of values.
     * @return the given values as a single comma separated list (no spaces between elements, no trailing comma)
     */
    public static String asCSList(String[] values) {
        String result = null;
        for (String value : values) {
            if (value != null) {
                if (result == null) {
                    result = value;
                } else {
                    result = concat(result, ",", value);
                }
            }
        }

        return result;
    }

    /**
     * Escapes and quotes command line arguments to keep shells from expanding/interpreting them.
     *
     * @param argument
     *            the argument to protect.
     * @return an argument with quotes, and escaped characters where needed.
     */
    public static String protectAgainstShellMetas(String argument) {
        char[] chars = argument.toCharArray();
        StringBuilder b = new StringBuilder(chars.length + 10);
        b.append('\'');
        for (char c : chars) {
            if (c == '\'') {
                b.append('\'');
                b.append('\\');
                b.append('\'');
            }
            b.append(c);
        }
        b.append('\'');
        return b.toString();
    }
}
