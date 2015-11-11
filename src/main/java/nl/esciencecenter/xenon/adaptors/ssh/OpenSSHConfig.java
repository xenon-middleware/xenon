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
package nl.esciencecenter.xenon.adaptors.ssh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.scripting.ScriptingParser;

import com.jcraft.jsch.ConfigRepository;
import com.jcraft.jsch.ConfigRepository.Config;

/**
 * This class implements ConfigRepository interface, and parses OpenSSH's
 * configuration file.
 * 
 * It does not support the Match argument from OpenSSH config. For more
 * information on the format of the config file, see man page ssh_config(5).
 */
public class OpenSSHConfig implements ConfigRepository {
    private final static Pattern DELIMITER_PATTERN = Pattern.compile("[= \t]+");
    /** Matches text without wildcards, or, individual wildcards. */
    private final static Pattern WILDCARD_PATTERN = Pattern.compile("[^*?]+|(\\*)|(\\?)");

    /**
     * Parses the given string, and returns an instance of ConfigRepository.
     *
     * @param conf string of an OpenSSH config
     * @return OpenSSHConfig
     * @throws XenonException if the configuration file is malformed
     */
    public static OpenSSHConfig parse(String conf) throws XenonException {
        try (Reader in = new StringReader(conf)) {
            return new OpenSSHConfig(in);
        } catch (IOException ex) {
            // we don't do any IO here
            throw new IllegalStateException("String IO should not throw an IOException", ex);
        }
    }

    /**
     * Parses the given file, and returns an instance of ConfigRepository.
     *
     * @param file OpenSSH's config file
     * @return OpenSSHConfig
     * @throws IOException if the file cannot be read
     * @throws XenonException if the configuration file is malformed
     */
    public static OpenSSHConfig parse(File file) throws IOException, XenonException {
        try (Reader in = new FileReader(file)) {
            return new OpenSSHConfig(in);
        }
    }

    protected static Pattern wildcardToPattern(String wildcard) {
        StringBuilder b = new StringBuilder(wildcard.length() + 16);
        Matcher m = WILDCARD_PATTERN.matcher(wildcard);
        while (m.find()) {
            if (m.group(1) != null) {  // text starts with *, replace with regex .*
                b.append(".*");
            } else if (m.group(2) != null) {  // text starts with ?, replace with regex .
                b.append('.');
            } else {  // text starts without wildcards, quote it to ignore any special regex characters
                b.append("\\Q").append(m.group()).append("\\E");
            }
        }
        return Pattern.compile(b.toString());
    }

    private static List<OpenSSHArgument> tokenize(Reader in) throws IOException, XenonException {
        BufferedReader br = new BufferedReader(in);
        String line;
        int lineNumber = 0;
        List<OpenSSHArgument> args = new ArrayList<>();

        while ((line = br.readLine()) != null) {
            lineNumber++;
            // remove empty lines and lines starting with a comment
            line = line.trim();
            if (line.isEmpty() || line.charAt(0) == '#') {
                continue;
            }

            // parse `host = something` or `Host something`
            String[] key_value = DELIMITER_PATTERN.split(line, 2);
            if (key_value.length < 2) {
                throw new XenonException(SshAdaptor.ADAPTOR_NAME, "SSH argument '" + line + "' must have value on line " + lineNumber + ".");
            }
            String key = key_value[0].toLowerCase();
            args.add(new OpenSSHArgument(key, key_value[1], lineNumber));
        }
        return args;
    }

    private final List<OpenSSHArgument> sshArguments;
    
    /**
     * Parses an OpenSSH config from a Reader.
     * @param in Reader of the OpenSSH config
     * @throws IOException if the Reader cannot be read.
     * @throws XenonException if the configuration file is malformed
     */
    public OpenSSHConfig(Reader in) throws IOException, XenonException {
        sshArguments = tokenize(in);
    }

    public Config getConfig(String host) {
        HostConfig config = new HostConfig();
        boolean isApplicable = true;

        for (OpenSSHArgument arg : sshArguments) {
            if ("host".equals(arg.getKey())) {
                isApplicable = false;
                for (String hostRegex : arg.getValues()) {
                    // ! means that matching hosts will be unmatched
                    if (hostRegex.charAt(0) == '!') {
                        String negated = hostRegex.substring(1);
                        if (wildcardToPattern(negated).matcher(host).matches()) {
                            isApplicable = false;
                            break;
                        }
                    } else if (wildcardToPattern(hostRegex).matcher(host).matches()) {
                        isApplicable = true;
                    }
                }
            } else if (isApplicable) {
                config.add(arg);
            }
        }

        return config;
    }

    private static class HostConfig implements Config {
        private final Map<String, OpenSSHArgument> properties;

        HostConfig() {
            this.properties = new HashMap<>();
        }

        // add argument only if not already present (as per OpenSSH config semantics)
        private void add(OpenSSHArgument arg) {
            if (!properties.containsKey(arg.getKey())) {
                properties.put(arg.getKey(), arg);
            }
        }

        private String getSingleValue(String name) {
            OpenSSHArgument arg = this.properties.get(name.toLowerCase());
            if (arg == null) {
                return null;
            } else {
                return arg.getValue();
            }
        }

        @Override
        public String getHostname() {
            return getSingleValue("hostname");
        }

        @Override
        public String getUser() {
            return getSingleValue("user");
        }

        @Override
        public int getPort() {
            String portStr = getSingleValue("port");
            if (portStr == null) {
                return -1;
            } else {
                return Integer.parseInt(portStr);
            }
        }

        /** 
         * Get the value of the OpenSSH argument without any splitting.
         */
        @Override
        public String getValue(String property) {
            String value = getSingleValue(property);

            if (property.equalsIgnoreCase("compression")) {
                if (value == null || value.equalsIgnoreCase("no")) {
                    return "none,zlib@openssh.com,zlib";
                } else {
                    return "zlib@openssh.com,zlib,none";
                }
            }

            return value;
        }

        /** 
         * Split the value of the argument on the same line according to
         * OpenSSH specification.
         */
        @Override
        public String[] getValues(String property) {
            OpenSSHArgument arg = this.properties.get(property.toLowerCase());
            if (arg == null) {
                return null;
            } else {
                return arg.getValues();
            }
        }
    }

    /**
     * Parses and stores an argument of an OpenSSH config file.
     */
    private static class OpenSSHArgument {
        /**
         * Matches all text before a quote, or if
         * the text starts with a quote, all text between quotes.
         */
        private final static Pattern quotesPattern = Pattern.compile("^([^\"]+)|^\"([^\"]*)\"");
        private final static Pattern commaPattern = Pattern.compile(",");
        private final String key;
        private final String value;
        private final String[] values;
        private final int lineNumber;

        OpenSSHArgument(String key, String value, int lineNumber) throws XenonException {
            this.key = key;
            this.value = value;
            this.lineNumber = lineNumber;
            this.values = split(value);
        }

        private String[] split(String value) throws XenonException {
            List<String> parsedTokens = new ArrayList<>();
            String[] tokens;
            Matcher m = quotesPattern.matcher(value);

            int matchedOffset = -1;
            while (matchedOffset < value.length()) {
                if (!m.find()) {
                    throw new XenonException(SshAdaptor.ADAPTOR_NAME, "The quote sequence of OpenSSH config value '" + value + "' on line " + lineNumber + " is invalid");
                }

                // text outside quotes is split by whitespace, text inside
                // quotes is split by commas.
                if (m.group(1) != null) {
                    tokens = ScriptingParser.WHITESPACE_REGEX.split(m.group(1).trim());
                } else {
                    tokens = commaPattern.split(m.group(2));
                }
                parsedTokens.addAll(Arrays.asList(tokens));

                // update start of string
                matchedOffset = m.end();
                m.region(matchedOffset, value.length());
            }
            return parsedTokens.toArray(new String[parsedTokens.size()]);
        }

        public String getKey() { return key; }
        public String getValue() { return value; }
        public int getLineNumber() { return lineNumber; }

        public String[] getValues() {
            return Arrays.copyOf(values, values.length);
        }
    }
}
