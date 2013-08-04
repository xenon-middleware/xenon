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
package nl.esciencecenter.octopus.adaptors.scripting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.octopus.adaptors.gridengine.GridEngineAdaptor;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

/**
 * @author Niels Drost
 * 
 */
public class ScriptingParser {

    public static final String WHITESPACE_REGEX = "\\s+";

    public static final String COMMA_REGEX = "\\s*,\\s*";

    public static final String BAR_REGEX = "\\s*\\|\\s*";

    public static final String NEWLINE_REGEX = "\\r?\\n";

    public static final String EQUALS_REGEX = "\\s*=\\s*";

    protected ScriptingParser() {
        //DO NOT USE
    }

    /**
     * Parses a output with key=value pairs separated by whitespace (including newlines). This function fails if there is any
     * whitespace between the key and value.
     * 
     * @param input
     *            the text to parse.
     * @param adaptorName
     *            the adaptor name reported in case an exception occurs.
     * @return a map containing all found key/value pairs.
     * @throws OctopusException
     *             if the input cannot be parsed.
     */
    public static Map<String, String> parseKeyValuePairs(String input, String adaptorName) throws OctopusException {
        Map<String, String> result = new HashMap<String, String>();

        String[] pairs = input.split(WHITESPACE_REGEX);

        for (String pair : pairs) {
            String[] elements = pair.split(EQUALS_REGEX, 2);

            if (elements.length != 2) {
                throw new OctopusException(adaptorName, "Got invalid key/value pair in output: " + pair);
            }

            result.put(elements[0], elements[1]);
        }

        return result;
    }

    //returns if the given input matches ant of the expressions given
    private static boolean containsAny(String input, String[] ignoredSequences) {
        for (String string : ignoredSequences) {
            if (input.contains(string)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses lines containing key/value pairs separated by the given separator possibly surrounded by whitespace. Will ignore
     * empty lines.
     * 
     * @param input
     *            the input to parse
     * @param separatorRegEx
     *            a regular expression for the separator between key and value
     * @param adaptorName
     *            the adaptor name to report in case parsing failed
     * @param ignoredLines
     *            lines containing any of the given strings will be ignored.
     * @return a map containing all found key/value pairs.
     * @throws OctopusException
     *             if the input cannot be parsed
     */
    public static Map<String, String> parseKeyValueLines(String input, String separatorRegEx, String adaptorName,
            String... ignoredLines) throws OctopusException {
        Map<String, String> result = new HashMap<String, String>();

        String[] lines = input.split(NEWLINE_REGEX);

        for (String line : lines) {
            if (!line.isEmpty() && !containsAny(line, ignoredLines)) {
                String[] elements = line.split(separatorRegEx, 2);

                if (elements.length != 2) {
                    throw new OctopusException(adaptorName, "Got invalid key/value pair in output: " + line);
                }

                result.put(elements[0], elements[1]);
            }
        }

        return result;
    }

    /**
     * Get a JobID (number) from a line of input.
     * 
     * @param input
     *            the line containing the jobID
     * @param adaptorName
     *            the adaptor name to report in case parsing failed
     * @param possiblePrefixes
     *            a number of possible prefixes before the job ID.
     * @return
     * @throws OctopusException
     */
    public static long parseJobIDFromLine(String input, String adaptorName, String... possiblePrefixes) throws OctopusException {
        for (String prefix : possiblePrefixes) {
            if (input.startsWith(prefix)) {
                //cut of prefix
                String jobId = input.substring(prefix.length()).trim();

                //trim leading and trailing whitespace
                jobId = jobId.trim();

                //see if anything remains
                if (jobId.length() == 0) {
                    throw new OctopusException(adaptorName, "failed to get jobID from line: \"" + input + "\"");
                }

                //cut of anything after the job id
                jobId = jobId.split(WHITESPACE_REGEX)[0];

                try {
                    return Long.parseLong(jobId);
                } catch (NumberFormatException e) {
                    throw new OctopusException(adaptorName, "failed to get jobID from line: \"" + input + "\" Job ID found \""
                            + jobId + "\" is not a number");
                }
            }
        }
        throw new OctopusException(adaptorName, "Failed to get jobID from line: \"" + input
                + "\" Line does not match expected prefixes: " + Arrays.toString(possiblePrefixes));
    }

    private static String cleanValue(String value, String[] suffixes) {
        for (String suffix : suffixes) {
            if (value.endsWith(suffix)) {
                return value.substring(0, value.length() - suffix.length());
            }
        }
        return value;
    }

    /**
     * Parses lines containing multiple values. The first line of the output must contain a header with the field names.
     * 
     * @param input
     *            the input to parse
     * 
     * @param keyField
     *            the field to use as the key in the result map. This field is mandatory in the output.
     * 
     * @param fieldSeparatorRegEx
     *            a regular expression of the separator between fields. Usually whitespace.
     * 
     * @param adaptorName
     *            the adaptor name to report in case parsing failed
     * 
     * @param valueSuffixes
     *            suffixes to be removed from values in the table. Useful if the output contains special markers for defaults,
     *            disabled queues, broken nodes, etc
     * 
     * @return a map containing key/value maps of all records.
     */
    public static Map<String, Map<String, String>> parseTable(String input, String keyField, String fieldSeparatorRegEx,
            String adaptorName, String... valueSuffixes) throws OctopusException {
        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();

        String[] lines = input.split(NEWLINE_REGEX);

        if (lines.length == 0) {
            throw new OctopusException(adaptorName, "Cannot parse table, Got no input, expected at least a header");
        }

        //the first line will contain the fields
        String[] fields = lines[0].split(fieldSeparatorRegEx);

        for (int i = 1; i < lines.length; i++) {
            String[] values = lines[i].split(fieldSeparatorRegEx);

            if (fields.length != values.length) {
                throw new OctopusException(adaptorName, "Expected " + fields.length + " fields in output, got line with "
                        + values.length + " values: " + lines[i]);
            }

            Map<String, String> map = new HashMap<String, String>();
            for (int j = 0; j < fields.length; j++) {
                map.put(fields[j], cleanValue(values[j], valueSuffixes));
            }

            if (!map.containsKey(keyField)) {
                throw new OctopusException(adaptorName, "Output does not contain required field \"" + keyField + "\"");

            }

            result.put(map.get(keyField), map);
        }

        return result;
    }

    /**
     * Checks if the given text contains any of the given options. Returns which option it contains.
     * 
     * @param input
     *            the input text to check
     * @param adaptorName
     *            the adaptor name to report in case no match was found
     * @param options
     *            all possible options the input could contain
     * @return the index of the matching option
     * @throws OctopusIOException
     *             in case the input does not contain any of the options given.
     */
    public static int contains(String input, String adaptorName, String... options) throws OctopusIOException {
        for (int i = 0; i < options.length; i++) {
            if (input.contains(options[i])) {
                return i;
            }
        }
        throw new OctopusIOException(adaptorName, "Output does not contain expected string: " + Arrays.toString(options));
    }

    /**
     * Parses a list of strings, separated by whitespace (including newlines)
     * 
     * @param input
     *            the input to parse
     */
    public static String[] parseList(String input) {
        return input.split(WHITESPACE_REGEX);
    }

    /**
     * Parses multiple key value records. A new record begins when the given key field is found. Each line contains a single
     * key/value pair, separated by the given separator.
     * 
     * @param input
     *            the input to parse.
     * @param separatorRegEx
     *            a regular expression for the separator between key and value
     * @param adaptorName
     *            the adaptor name to report in case parsing failed
     * @param ignoredLines
     *            lines containing any of the given strings will be ignored.
     * @param keyField
     *            the header field which triggers a new record. the first line of the output must contain this key
     * @return a map with all records found. The value of the key field is used as a key.
     * @throws OctopusIOException
     */
    public static Map<String, Map<String, String>> parseKeyValueRecords(String input, String keyField, String separatorRegEx,
            String adaptorName, String... ignoredLines) throws OctopusIOException {
        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();

        String[] lines = input.split(NEWLINE_REGEX);

        Map<String, String> currentMap = null;

        for (String line : lines) {
            if (!line.isEmpty() && !containsAny(line, ignoredLines)) {
                String[] elements = line.split(separatorRegEx, 2);

                if (elements.length != 2) {
                    throw new OctopusIOException(GridEngineAdaptor.ADAPTOR_NAME, "Expected two columns in qconf output, got \""
                            + line + "\" in qconf output");
                }

                String key = elements[0];
                String value = elements[1];

                if (key.equals(keyField)) {
                    //listing of a new item starts
                    currentMap = new HashMap<String, String>();
                    result.put(value, currentMap);
                } else if (currentMap == null) {
                    throw new OctopusIOException(GridEngineAdaptor.ADAPTOR_NAME, "Expecting \"" + keyField
                            + "\" on first line, got \"" + line + "\"");
                }
                currentMap.put(key, value);
            }
        }
        return result;
    }

}
