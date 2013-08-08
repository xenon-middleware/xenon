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
package nl.esciencecenter.octopus.engine.util;

/**
 * Some simple utilities for handling and creating scripts and command lines.
 * 
 * @author Niels Drost
 * 
 */
public final class CommandLineUtils {

    private CommandLineUtils() { 
        // DO NOT USE
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
        StringBuffer b = new StringBuffer();
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
                    result += "," + value;
                }
            }
        }

        return result;
    }

}
