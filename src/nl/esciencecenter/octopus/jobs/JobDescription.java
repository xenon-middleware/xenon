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
package nl.esciencecenter.octopus.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * JobDescription contains a description of a job that can be submitted to a {@link} Scheduler.
 * 
 * @author Niels Drost <N.Drost@esciencecenter.nl>
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class JobDescription {

    /** The queue to submit to. */
    private String queueName = null;

    /** The exectuable to run. */
    private String executable = null;

    /** The arguments to pass to the executable. */
    private final List<String> arguments = new ArrayList<String>();

    /** The location file from which to redirect stdin. (optional) */
    private String stdin = null;

    /** The location file which to redirect stdout to. (default: stdout.txt) */
    private String stdout = "stdout.txt";

    /** The location file which to redirect stderr to. (default: stderr.txt) */
    private String stderr = "stderr.txt";

    /** The working directory for the job. */
    private String workingDirectory = null;

    /** The environment variables and their values */
    private final Map<String, String> environment = new HashMap<String, String>();

    /** The job options of this job */
    private final Map<String, String> jobOptions = new HashMap<String, String>();

    /** The number of nodes to run the job on. */
    private int nodeCount = 1;

    /** The number of presesses to start per node. */
    private int processesPerNode = 1;

    /** The maximum run time in minutes. */
    private int maxTime = 30;

    /** Is this an interactive job ? */
    private boolean interactive = false;

    /**
     * Create a JobDescription.
     */
    public JobDescription() {
        // nothing
    }

    /**
     * Get the number of nodes.
     * 
     * @return the number of nodes.
     */
    public int getNodeCount() {
        return nodeCount;
    }

    /**
     * Set the number of nodes.
     * 
     * @param nodesCount
     *            the number of nodes;
     */
    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    /**
     * Get the number of processes to start on each node.
     * 
     * @return the number of processesPerNode.
     */
    public int getProcessesPerNode() {
        return processesPerNode;
    }

    /**
     * Set the number of processes started on each node.
     * 
     * @param processesPerNode
     *            the number of processes started on each node.
     */
    public void setProcessesPerNode(int processesPerNode) {
        this.processesPerNode = processesPerNode;
    }

    /**
     * Get the queue name;
     * 
     * @return the queue name;
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Set the queue name;
     * 
     * @param queueName
     *            the queue name;
     */
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    /**
     * Get the maximum job time (in minutes).
     * 
     * @return the queue name;
     */
    public int getMaxTime() {
        return maxTime;
    }

    /**
     * Set the maximum job time (in minutes).
     * 
     * @param maxTime
     *            the maximum job time (in minutes).
     */
    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }

    /**
     * Get the path to the executable.
     * 
     * @return the path to the executable.
     */
    public String getExecutable() {
        return executable;
    }

    /**
     * Sets the path to the executable.
     * 
     * @param executable
     *            the path to the executable.
     */
    public void setExecutable(String executable) {
        this.executable = executable;
    }

    /**
     * Get the command line arguments of the executable.
     * 
     * @return Returns the arguments of the executable.
     */
    public List<String> getArguments() {
        return arguments;
    }

    /**
     * Sets the command line arguments of the executable.
     * 
     * @param arguments
     *            the command line arguments of the executable.
     */
    public void setArguments(String... arguments) {
        this.arguments.clear();

        for (String argument : arguments) {
            addArgument(argument);
        }
    }

    /**
     * Add a command line argument for the executable.
     * 
     * The argument may not be <code>null</code> or empty.
     * 
     * @param argument
     *            the command line argument to add.
     */
    public void addArgument(String argument) {

        if (argument == null || argument.length() == 0) {
            throw new IllegalArgumentException("Argument may not be null or empty!");
        }

        arguments.add(argument);
    }

    /**
     * Get the environment of the executable.
     * 
     * The environment of the executable consists of a {@link Map} of environment variables with their values (for example:
     * "JAVA_HOME", "/path/to/java").
     * 
     * @return the environment of the executable.
     */
    public Map<String, String> getEnvironment() {
        return environment;
    }

    /**
     * Sets the environment of the executable.
     * 
     * The environment of the executable consists of a {@link Map} of environment variables with their values (for example:
     * "JAVA_HOME", "/path/to/java").
     * 
     * @param environment
     *            environment of the executable.
     */
    public void setEnvironment(Map<String, String> environment) {

        this.environment.clear();

        if (environment != null) {
            for (Entry<String, String> entry : environment.entrySet()) {
                addEnvironment(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Add a variable to the environment of the executable.
     * 
     * The environment of the executable consists of a {@link Map} of environment variables with their values (for example:
     * "JAVA_HOME", "/path/to/java").
     * 
     * The name of an environment variable may not be <code>null</code> or empty.
     * 
     * @param environment
     *            environment of the executable.
     */
    public void addEnvironment(String key, String value) {

        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException("Envrionment variable name may not be null or empty!");
        }

        environment.put(key, value);
    }

    /**
     * Get a copy of the job options of this job.
     * 
     * The job options consist of a {@link Map} of options variables with their values (for example: "PE", "MPI").
     * 
     * @return the job options of the job.
     */
    public Map<String, String> getJobOptions() {
        return jobOptions;
    }

    /**
     * Sets the job options of the job.
     * 
     * The job options consist of a {@link Map} of options variables with their values (for example: "PE", "MPI").
     * 
     * @param options
     *            job options of the job.
     */
    public void setJobOptions(Map<String, String> options) {

        jobOptions.clear();

        if (options != null) {
            for (Entry<String, String> entry : options.entrySet()) {
                addJobOptions(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Add a job option to the job.
     * 
     * The job option consist of a key-value pair (for example: "PE", "MPI").
     * 
     * Neither the key or value of a job option may be <code>null</code> or empty.
     * 
     * @param options
     *            job options of the job.
     */
    public void addJobOptions(String key, String value) {

        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException("Job option key may not be null or empty!");
        }

        if (value == null || value.length() == 0) {
            throw new IllegalArgumentException("Job option value may not be null or empty!");
        }

        jobOptions.put(key, value);
    }

    /**
     * Sets the path to the file from which the executable must redirect stdin.
     * 
     * @param stdin
     *            the path.
     */
    public void setStdin(String stdin) {
        this.stdin = stdin;
    }

    /**
     * Sets the path to the file to which the executable must redirect stdout.
     * 
     * @param stdout
     *            the path.
     */
    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    /**
     * Sets the path to the file to which the executable must redirect stderr.
     * 
     * @param stderr
     *            the path.
     */
    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    /**
     * Sets the path of the working directory for the executable.
     * 
     * @param workingDirectory
     *            path of the working directory.
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * Gets the path to the file from which the executable must redirect stdin.
     * 
     * @return the path.
     */
    public String getStdin() {
        return stdin;
    }

    /**
     * Gets the path to the file to which the executable must redirect stdout.
     * 
     * @return the path.
     */
    public String getStdout() {
        return stdout;
    }

    /**
     * Gets the path to the file to which the executable must redirect stderr.
     * 
     * @return the path.
     */
    public String getStderr() {
        return stderr;
    }

    /**
     * Gets the path of the working directory for the executable.
     * 
     * @return workingDirectory path of the working directory.
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Is this an interactive job ?
     * 
     * @return if this an interactive job.
     */
    public boolean isInteractive() {
        return interactive;
    }

    /**
     * Set if this is an interactive job.
     * 
     * @param interactive
     *            if this is an interactive job.
     */
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    /* Generated */
    @Override
    public String toString() {
        return "JobDescription [queueName=" + queueName + ", " + "executable=" + executable + ", arguments=" + arguments
                + ", nodeCount=" + nodeCount + ", processesPerNode=" + processesPerNode + ", maxTime=" + maxTime
                + ", interactive=" + interactive + ", stdin=" + stdin + ", stdout=" + stdout + ", stderr=" + stderr
                + ", workingDirectory=" + workingDirectory + ", environment=" + environment + ", jobOptions=" + jobOptions + "]";
    }

    /* Generated */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + arguments.hashCode();
        result = prime * result + environment.hashCode();
        result = prime * result + jobOptions.hashCode();
        result = prime * result + ((executable == null) ? 0 : executable.hashCode());
        result = prime * result + (interactive ? 1231 : 1237);
        result = prime * result + maxTime;
        result = prime * result + nodeCount;
        result = prime * result + processesPerNode;
        result = prime * result + ((queueName == null) ? 0 : queueName.hashCode());
        result = prime * result + ((stderr == null) ? 0 : stderr.hashCode());
        result = prime * result + ((stdin == null) ? 0 : stdin.hashCode());
        result = prime * result + ((stdout == null) ? 0 : stdout.hashCode());
        result = prime * result + ((workingDirectory == null) ? 0 : workingDirectory.hashCode());
        return result;
    }

    private boolean compare(Object a, Object b) {

        if (a == null) {
            if (b == null) {
                return true;
            } else {
                return false;
            }
        }

        if (b == null) {
            return false;
        }

        return a.equals(b);
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

        JobDescription other = (JobDescription) obj;

        if (interactive != other.interactive || maxTime != other.maxTime || nodeCount != other.nodeCount
                || processesPerNode != other.processesPerNode) {
            return false;
        }

        return compare(executable, other.executable) && compare(workingDirectory, other.workingDirectory)
                && compare(queueName, other.queueName) && compare(stdin, other.stdin) && compare(stdout, other.stdout)
                && compare(stderr, other.stderr) && compare(arguments, other.arguments)
                && compare(environment, other.environment) && compare(jobOptions, other.jobOptions);
    }
}
