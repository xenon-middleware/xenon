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
package nl.esciencecenter.xenon.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.xenon.XenonRuntimeException;
import nl.esciencecenter.xenon.jobs.JobDescription;

/**
 * A JobDescription specialized in Java applications. 
 * 
 * A JavaJobDescription will use the Java specific information provided by the user to build the command line arguments of the
 * JobDescription.
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @version 1.0 
 * @since 1.0
 */
public class JavaJobDescription extends JobDescription {

    private final List<String> javaOptions = new ArrayList<String>();

    private final Map<String, String> javaSystemProperties = new HashMap<String, String>();

    private String javaMain = null;

    private final List<String> javaArguments = new ArrayList<String>();

    private final List<String> javaClasspath = new ArrayList<String>();

    /**
     * Create a JavaJobDescription which describes the java application.
     */
    public JavaJobDescription() {
        super();
    }

    /**
     * Returns the JVM options.
     * 
     * @return the JVM options.
     */
    public List<String> getJavaOptions() {
        return javaOptions;
    }

    /**
     * Sets the JVM options.
     * 
     * @param options
     *            the JVM options.
     */
    public void setJavaOptions(String... options) {
        this.javaOptions.clear();

        for (String option : options) {
            addJavaOption(option);
        }
    }

    /**
     * Adds a JVM option.
     * 
     * @param option
     *            the JVM option.
     */
    public void addJavaOption(String option) {
        if (option == null || option.length() == 0) {
            throw new IllegalArgumentException("Option may not be null or empty!");
        }

        javaOptions.add(option);
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
     * Sets the system properties. A system property should be passed as a key value pair <"a", "b">, not as <"-Da", "b">, Xenon
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

    public void addJavaArgument(String javaArgument) {
        if (javaArgument == null || javaArgument.length() == 0) {
            throw new IllegalArgumentException("javaArgument may not be null or empty!");
        }

        javaArguments.add(javaArgument);
    }

    
    
    /**
     * <b>This method should not be used</b>. This method will throw an runtime exception when used. The methods
     * {@link #setJavaClasspath(String [])} , {@link #setJavaOptions(String[])}, {@link #setJavaSystemProperties(Map)},
     * {@link #setJavaMain(String)} and {@link #setJavaArguments(String[])} should be used to construct the command line
     * arguments.
     * 
     * @param arguments
     */
    public void setArguments(String... arguments) {
        throw new XenonRuntimeException("Utils", "Setting arguments not supported by the JavaJobDescription");
    }

    /**
     * Constructs the command line arguments from the class path, the JVM options, the system properties, the main and the java
     * arguments.
     * 
     * @return the command line arguments
     */
    @Override
    public List<String> getArguments() {
        return getArguments(File.pathSeparatorChar);
    }

    /**
     * Constructs the command line arguments from the class path, the JVM options, the system properties, the main and the java
     * arguments.
     * 
     * @param pathSeparator
     *            the seperator to use in the classpath. Defaults to the unix path seperator ':'
     * 
     * @return the command line arguments
     */
    public List<String> getArguments(char pathSeparator) {
        List<String> result = new ArrayList<>();
        result.addAll(getJavaOptions());

        if (!getJavaClasspath().isEmpty()) {
            result.add("-classpath");
            String classpath = null;

            for (String element : getJavaClasspath()) {
                if (classpath == null) {
                    classpath = element;
                } else {
                    classpath = classpath + pathSeparator + element;
                }
            }
            result.add(classpath);
        }

        Map<String, String> properties = getJavaSystemProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            result.add("-D" + entry.getKey() + "=" + entry.getValue());
        }

        if (getJavaMain() != null) {
            result.add(getJavaMain());
        }

        result.addAll(getJavaArguments());
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
        return javaClasspath;
    }

    /**
     * Sets the java class path. Will automatically add separators when multiple elements are given.
     * 
     * @param javaClasspath
     *            the class path to be set.
     */
    public void setJavaClasspath(String... javaClasspath) {
        this.javaClasspath.clear();

        for (String element : javaClasspath) {
            addJavaClasspathElement(element);
        }
    }

    public void addJavaClasspathElement(String element) {
        if (element == null || element.length() == 0) {
            throw new IllegalArgumentException("java classpath element may not be null or empty!");
        }

        javaClasspath.add(element);
    }

    @Override
    public String toString() {
        return "JavaJobDescription [javaOptions=" + javaOptions + ", javaSystemProperties=" + javaSystemProperties
                + ", javaMain=" + javaMain + ", javaArguments=" + javaArguments + ", javaClassPath=" + javaClasspath
                + ", queueName=" + getQueueName() + ", executable=" + getExecutable() + ", stdin=" + getStdin() + ", stdout="
                + getStdout() + ", stderr=" + getStderr() + ", workingDirectory=" + getWorkingDirectory() + ", environment="
                + getEnvironment() + ", jobOptions=" + getJobOptions() + ", nodeCount=" + getNodeCount() + ", processesPerNode="
                + getProcessesPerNode() + ", maxTime=" + getMaxTime() + ", interactive=" + isInteractive() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + javaArguments.hashCode();
        result = prime * result + javaClasspath.hashCode();
        result = prime * result + ((javaMain == null) ? 0 : javaMain.hashCode());
        result = prime * result + javaOptions.hashCode();
        result = prime * result + javaSystemProperties.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        if (!super.equals(obj)) {
            return false;
        }

        JavaJobDescription other = (JavaJobDescription) obj;
        if (!javaArguments.equals(other.javaArguments)) {
            return false;
        }
        if (!javaClasspath.equals(other.javaClasspath)) {
            return false;
        }
        if (javaMain == null) {
            if (other.javaMain != null) {
                return false;
            }
        } else if (!javaMain.equals(other.javaMain)) {
            return false;
        }
        if (!javaOptions.equals(other.javaOptions)) {
            return false;
        }
        if (!javaSystemProperties.equals(other.javaSystemProperties)) {
            return false;
        }
        return true;
    }

}