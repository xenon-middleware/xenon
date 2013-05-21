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
package nl.esciencecenter.octopus.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.octopus.exceptions.OctopusRuntimeException;
import nl.esciencecenter.octopus.jobs.JobDescription;

/**
 * JobDescription for a Java application. Will use the Java specific information provided by the user to build the command line
 * arguments of the JobDescription.
 * 
 * @author Niels Drost
 * 
 */
public class JavaJobDescription extends JobDescription {

    private final List<String> javaOptions = new ArrayList<String>();

    private final Map<String, String> javaSystemProperties = new HashMap<String, String>();

    private String javaMain = null;

    private final List<String> javaArguments = new ArrayList<String>();

    private final List<String> javaClassPath = new ArrayList<String>();

    /**
     * Create a {@link JavaJobDescription}, which describes the java application.
     */
    public JavaJobDescription() {
        super();
    }

    /**
     * Returns the jvm options.
     * 
     * @return the jvm options.
     */
    public List<String> getJavaOptions() {
        return javaOptions;
    }

    /**
     * Sets the jvm options.
     * 
     * @param options
     *            the jvm options.
     */
    public void setJavaOptions(String... options) {
        this.javaOptions.clear();
        this.javaOptions.addAll(Arrays.asList(options));
    }

    /**
     * Returns the java system properties.
     * 
     * @return the java system properties.
     */
    public Map<String, String> getJavaSystemProperties() {
        return javaSystemProperties;
    }

    /**
     * Sets the system properties. A system property should be passed as a key value pair <"a", "b">, not as <"-Da", "b">, Octopus
     * will add the -D to the property.
     * 
     * @param systemProperties
     *            the system properties.
     */
    public void setJavaSystemProperties(Map<String, String> systemProperties) {
        this.javaSystemProperties.clear();
        this.javaSystemProperties.putAll(systemProperties);
    }

    /**
     * Adds a system property to the current set of system properties. The key of the system property should not start with "-D".
     * 
     * @param key
     *            the key of the system property to be added
     * @param value
     *            the value belonging to the key of the system property to be added
     */
    public void addJavaSystemProperty(String key, String value) {
        javaSystemProperties.put(key, value);
    }

    /**
     * Returns the main class of the java application.
     * 
     * @return the main class.
     */
    public String getJavaMain() {
        return javaMain;
    }

    /**
     * Sets the main class.
     * 
     * @param main
     *            the main class.
     */
    public void setJavaMain(String main) {
        this.javaMain = main;
    }

    /**
     * Returns the arguments for the main class.
     * 
     * @return the arguments for the main class
     */
    public List<String> getJavaArguments() {
        return javaArguments;
    }

    /**
     * Sets the arguments of the java main class.
     * 
     * @param javaArguments
     *            the arguments of the java main class.
     */
    public void setJavaArguments(String... javaArguments) {
        this.javaArguments.clear();
        this.javaArguments.addAll(Arrays.asList(javaArguments));
    }

    /**
     * <b>This method should not be used</b>. This method will ignore all arguments. The methods {@link #setJavaClassPath(String)}
     * , {@link #setJavaOptions(String[])}, {@link #setJavaSystemProperties(Map)}, {@link #setJavaMain(String)} and
     * {@link #setJavaArguments(String[])} should be used to construct the command line arguments.
     * 
     * @param arguments
     */
    public void setArguments(String... arguments) {
        throw new OctopusRuntimeException("Utils", "Setting arguments not supported by the JavaJobDescription");
    }

    /**
     * Constructs the command line arguments from the class path, the jvm options, the system properties, the main and the java
     * arguments.
     * 
     * @return the command line arguments
     */
    public ArrayList<String> getArguments() {
        ArrayList<String> result = new ArrayList<String>();
        if (getJavaOptions() != null) {
            for (String option : getJavaOptions()) {
                result.add(option);
            }
        }
        if (!getJavaClasspath().isEmpty()) {
            result.add("-classpath");
            String classpath = null;

            for (String element : getJavaClasspath()) {
                if (classpath == null) {
                    classpath = element;
                } else {
                    classpath = classpath + ":" + element;
                }
            }
            result.add(classpath);
        }

        if (getJavaSystemProperties() != null) {
            Map<String, String> properties = getJavaSystemProperties();
            for (String key : properties.keySet()) {
                // null values ignored
                if (properties.get(key) != null) {
                    result.add("-D" + key + "=" + properties.get(key));
                }
            }
        }

        if (getJavaMain() != null) {
            result.add(getJavaMain());
        } else {
            return null;
        }

        if (getJavaArguments() != null) {
            for (String javaArgument : getJavaArguments()) {
                result.add(javaArgument);
            }
        }
        return result;
    }

    /**
     * Returns the executable. If no executable is set the default executable will be "java".
     * 
     * @return Returns the executable.
     */
    public String getExecutable() {
        if (super.getExecutable() == null) {
            return "java";
        } else {
            return super.getExecutable();
        }
    }

    /**
     * Returns the java class path.
     * 
     * @return the java class path.
     */
    public List<String> getJavaClasspath() {
        return javaClassPath;
    }

    /**
     * Sets the java class path. Will automatically add separators when multiple elements are given.
     * 
     * @param javaClassPath
     *            the class path to be set.
     */
    public void setJavaClassPath(String... javaClasspath) {
        this.javaClassPath.clear();
        this.javaClassPath.addAll(Arrays.asList(javaClasspath));
    }

}