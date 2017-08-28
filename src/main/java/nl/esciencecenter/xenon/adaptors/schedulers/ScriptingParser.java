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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import nl.esciencecenter.xenon.XenonException;
//import nl.esciencecenter.xenon.util.Utils;

/**
 *
 */
public final class ScriptingParser {

    public static final Pattern WHITESPACE_REGEX = Pattern.compile("\\s+");

    public static final Pattern BAR_REGEX = Pattern.compile("\\s*\\|\\s*");

    public static final Pattern NEWLINE_REGEX = Pattern.compile("\\r?\\n");

    public static final Pattern EQUALS_REGEX = Pattern.compile("\\s*=\\s*");

    public static final Pattern HORIZONTAL_LINE_REGEX = Pattern.compile("^\\s*([=_-]{3,}\\s*)+$");

    private ScriptingParser() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * Parses a output with key=value pairs separated by whitespace, on one or more lines. This function fails if there is any
     * whitespace between the key and value, or whitespace inside the values.
     *
     * @param input
     *            the text to parse.
     * @param adaptorName
     *            the adaptor name reported in case an exception occurs.
     * @param ignoredLines
     *            lines exactly matching one of these strings will be ignored.
     * @return a map containing all found key/value pairs.
     * @throws XenonException
     *             if the input cannot be parsed.
     */
    public static Map<String, String> parseKeyValuePairs(String input, String adaptorName, String... ignoredLines)
            throws XenonException {
        String[] lines = NEWLINE_REGEX.split(input);
        Map<String, String> result = new HashMap<>(lines.length * 4 / 3);

        for (String line : lines) {
            if (!line.isEmpty() && !containsAny(line, ignoredLines)) {
                String[] pairs = WHITESPACE_REGEX.split(line.trim());

                for (String pair : pairs) {
                    String[] elements = EQUALS_REGEX.split(pair, 2);

                    if (elements.length != 2) {
                        throw new XenonException(adaptorName, "Got invalid key/value pair in output: \"" + pair + "\"");
                    }

                    result.put(elements[0].trim(), elements[1].trim());
                }
            }
        }

        return result;
    }

    /**
     * Returns if the given input String contains any of the option Strings given.
     *
     * @param input String to check on
     * @param options Strings to check for
     *
     * @return is any of the Strings in options is contain in the input string
     */
    public static boolean containsAny(String input, String... options) {
        for (String string : options) {
            if (input.contains(string)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses lines containing single key/value pairs separated by the given separator, possibly surrounded by whitespace. Will
     * ignore empty lines.
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
     * @throws XenonException
     *             if the input cannot be parsed
     */
    public static Map<String, String> parseKeyValueLines(String input, Pattern separatorRegEx, String adaptorName,
            String... ignoredLines) throws XenonException {
        String[] lines = NEWLINE_REGEX.split(input);
        Map<String, String> result = new HashMap<>(lines.length * 4 / 3);

        for (String line : lines) {
            if (!line.isEmpty() && !containsAny(line, ignoredLines)) {
                String[] elements = separatorRegEx.split(line, 2);

                if (elements.length != 2) {
                    throw new XenonException(adaptorName, "Got invalid key/value pair in output: " + line);
                }

                result.put(elements[0].trim(), elements[1].trim());
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
     * @return the job ID found on the input line.
     * @throws XenonException
     *          if the input could not be parsed.
     */
    public static String parseJobIDFromLine(String input, String adaptorName, String... possiblePrefixes) throws XenonException {
        for (String prefix : possiblePrefixes) {
            if (input.startsWith(prefix)) {
                //cut off prefix
                String jobId = input.substring(prefix.length()).trim();

                //trim leading and trailing whitespace
                jobId = jobId.trim();

                //see if anything remains
                if (jobId.length() == 0) {
                    throw new XenonException(adaptorName, "failed to get jobID from line: \"" + input
                            + "\" Line did not contain job ID.");
                }

                //cut of anything after the job id
                return WHITESPACE_REGEX.split(jobId)[0];
            }
        }
        throw new XenonException(adaptorName, "Failed to get jobID from line: \"" + input
                + "\" Line does not match expected prefixes: " + Arrays.toString(possiblePrefixes));
    }

    /**
     * Remove suffix from a string if present.
     *
     * Although more than one possible suffix can be provided, only the first suffix encountered will be removed.
     *
     * @param value
     *          the text to clean
     * @param suffixes
     *          the possible suffixes to remove
     * @return
     *          the cleaned text
     */
    public static String cleanValue(String value, String... suffixes) {
        String trimmed = value.trim();

        for (String suffix : suffixes) {
            if (trimmed.endsWith(suffix)) {
                return trimmed.substring(0, trimmed.length() - suffix.length());
            }
        }
        return trimmed;
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
     * @throws XenonException when parsing fails
     */
    @SuppressWarnings("PMD.NPathComplexity")
    public static Map<String, Map<String, String>> parseTable(String input, String keyField, Pattern fieldSeparatorRegEx,
            String adaptorName, String... valueSuffixes) throws XenonException {
        if (input.isEmpty()) {
            throw new XenonException(adaptorName, "Cannot parse table, Got no input, expected at least a header");
        }

        String[] lines = NEWLINE_REGEX.split(input);

        int headerLine = 0;
        String[] fields;
        //the first line will contain the fields (unless it is a separator)
        while (headerLine < lines.length && HORIZONTAL_LINE_REGEX.matcher(lines[headerLine]).find()) {
            headerLine++;
        }
        if (headerLine == lines.length) {
            throw new XenonException(adaptorName, "No table header encountered");
        }

        fields = fieldSeparatorRegEx.split(lines[headerLine]);

        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();

            if (fields[i].isEmpty()) {
                throw new XenonException(adaptorName, "Output contains empty field name in line \"" + lines[0] + "\"");
            }
        }

        Map<String, Map<String, String>> result = new HashMap<>(lines.length * 4 / 3);
        int rowSize = (int)Math.ceil(fields.length / 0.75);

        for (int i = headerLine + 1; i < lines.length; i++) {
            if (HORIZONTAL_LINE_REGEX.matcher(lines[i]).find()) {
                // do not parse separators
                continue;
            }

            String[] values = mergeTuples(fieldSeparatorRegEx.split(lines[i]));

            if (fields.length != values.length) {
                throw new XenonException(adaptorName, "Expected " + fields.length + " fields in output " + Arrays.toString(fields)
                    + ", got line with " + values.length + " values: " + lines[i] + "parsed to: " + Arrays.toString(values) + " original input\n\n" + input + "\n\n");
            }

            Map<String, String> map = new HashMap<>(rowSize);
            for (int j = 0; j < fields.length; j++) {
                map.put(fields[j], cleanValue(values[j], valueSuffixes));
            }

            if (!map.containsKey(keyField)) {
                throw new XenonException(adaptorName, "Output does not contain required field \"" + keyField + "\"");
            }

            result.put(map.get(keyField), map);
        }

        return result;
    }

    /*
     * Attempt to support simple tuples in the output. The splitter will typically split these into two elements. For example:
     *
     *     "(bla, bla)"
     *
     * will be split into
     *
     *     "(bla,"
     *     "bla)
     *
     * while we typically expect these to remain as one string.
     */
    private static String [] mergeTuples(String [] values) {

        boolean inTuple = false;

        ArrayList<String> tmp = new ArrayList<>();

        String current = null;

        for (String v : values) {

            if (!inTuple) {
                if (v.startsWith("(") && !v.endsWith(")")) {
                    inTuple = true;
                    current = v;
                } else {
                    tmp.add(v);
                }
            } else {
                current = CommandLineUtils.concat(current, " ", v); // Damn.. no clue which whitespace to use here :-(

                if (v.endsWith(")") && !v.startsWith("(")) {
                    inTuple = false;
                    tmp.add(current);
                    current = null;
                }
            }
        }

        if (inTuple) {
            // Our tuple merging has gone pear shaped... resort to the original output!
            return values;
        } else {
            return tmp.toArray(new String[tmp.size()]);
        }
    }

    /**
     * Checks if the given text contains any of the given options. Returns which option it contains, throws an exception if it
     * doesn't.
     *
     * @param input
     *            the input text to check
     * @param adaptorName
     *            the adaptor name to report in case no match was found
     * @param options
     *            all possible options the input could contain
     * @return the index of the matching option
     * @throws XenonException
     *             in case the input does not contain any of the options given.
     */
    public static int checkIfContains(String input, String adaptorName, String... options) throws XenonException {
        for (int i = 0; i < options.length; i++) {
            if (input.contains(options[i])) {
                return i;
            }
        }
        throw new XenonException(adaptorName, "Output does not contain expected string: " + Arrays.toString(options));
    }

    /**
     * Parses a list of strings, separated by whitespace (including newlines)
     * Trailing empty strings are not included.
     *
     * @param input
     *            the input to parse
     * @return an array of strings with no whitespace
     */
    public static String[] parseList(String input) {
        String[] out = WHITESPACE_REGEX.split(input);
        if (out.length == 1 && "".equals(out[0])) {
            return new String[0];
        }
        return out;
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
     * @throws XenonException in case the output does not match the expected format
     */
    public static Map<String, Map<String, String>> parseKeyValueRecords(String input, String keyField, Pattern separatorRegEx,
            String adaptorName, String... ignoredLines) throws XenonException {
        String[] lines = NEWLINE_REGEX.split(input);
        Map<String, Map<String, String>> result = new HashMap<>();

        Map<String, String> currentMap = null;

        for (String line : lines) {
            if (!line.isEmpty() && !containsAny(line, ignoredLines)) {
                String[] elements = separatorRegEx.split(line, 2);

                if (elements.length != 2) {
                    throw new XenonException(adaptorName, "Expected two columns in output, got \"" + line + "\"");
                }

                String key = elements[0].trim();
                String value = elements[1].trim();

                if (key.equals(keyField)) {
                    //listing of a new item starts
                    currentMap = new HashMap<>(2);
                    result.put(value, currentMap);
                } else if (currentMap == null) {
                    throw new XenonException(adaptorName, "Expecting \"" + keyField
                            + "\" on first line, got \"" + line + "\"");
                }
                currentMap.put(key, value);
            }
        }
        return result;
    }

}
