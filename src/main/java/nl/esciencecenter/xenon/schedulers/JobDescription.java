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
package nl.esciencecenter.xenon.schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * JobDescription contains a description of a job that can be submitted to a {@link Scheduler}.
 *
 * @version 1.0
 * @since 1.0
 */
public class JobDescription {

    /** The default start time */
    public static final String DEFAULT_START_TIME = "now";

    /** The queue to submit to. */
    private String queueName = null;

    /** The executable to run. */
    private String executable = null;

    /** The name of the job. */
    private String name = null;

    /** The arguments to pass to the executable. */
    private final List<String> arguments = new ArrayList<>(10);

    /** The arguments to pass to the scheduler. */
    private final List<String> schedulerArguments = new ArrayList<>(10);

    /** The location file from which to redirect stdin. (optional) */
    private String stdin = null;

    /** The location file which to redirect stdout to. (optional) */
    private String stdout = null;

    /** The location file which to redirect stderr to. (optional) */
    private String stderr = null;

    /** The working directory for the job. */
    private String workingDirectory = null;

    /** The environment variables and their values */
    private final Map<String, String> environment = new HashMap<>(5);

    /** The number of tasks the jobs consists of. */
    private int tasks = 1;

    /** The number of cores needed per tasks. */
    private int coresPerTask = 1;

    /** The number of tasks per node */
    private int tasksPerNode = -1;

    /** The maximum amount of memory needed (in MB) on each node/process. */
    private int maxMemory = -1;

    /** The tempspace needed (in MB) on each node/process. */
    private int tempSpace = -1;

    /** Start the executable once per task instead of once per job? */
    private boolean startPerTask = false;

    /** The maximum run time in minutes. */
    private int maxRuntime = -1;

    /** The requested start time */
    private String startTime = DEFAULT_START_TIME;

    /**
     * Create a JobDescription.
     */
    public JobDescription() {
        // nothing
    }

    /**
     * Create a JobDescription by copying an existing one.
     *
     * @param original
     *            JobDescription to copy
     */
    public JobDescription(JobDescription original) {
        queueName = original.getQueueName();
        executable = original.getExecutable();
        name = original.getName();
        arguments.addAll(original.getArguments());
        schedulerArguments.addAll(original.getSchedulerArguments());
        stdin = original.getStdin();
        stdout = original.getStdout();
        stderr = original.getStderr();
        workingDirectory = original.getWorkingDirectory();
        environment.putAll(original.getEnvironment());
        tasks = original.getTasks();
        coresPerTask = original.getCoresPerTask();
        tasksPerNode = original.getTasksPerNode();
        maxMemory = original.getMaxMemory();
        tempSpace = original.getTempSpace();
        startPerTask = original.isStartPerTask();
        maxRuntime = original.getMaxRuntime();
        startTime = original.getStartTime();
    }

    /**
     * Get the job name.
     *
     * @return the job name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the job.
     *
     * @param name
     *            the name of the job;
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the number of tasks in this job.
     *
     * @return the number of tasks.
     */
    public int getTasks() {
        return tasks;
    }

    /**
     * Set the number of tasks in this job.
     *
     * @param tasks
     *            the number of tasks;
     */
    public void setTasks(int tasks) {
        this.tasks = tasks;
    }

    /**
     * Get the number of cores needed for each task.
     *
     * @return the number of cores needed for each task.
     */
    public int getCoresPerTask() {
        return coresPerTask;
    }

    /**
     * Set the number of cores needed for each task.
     *
     * @param coresPerTask
     *            the number of cores needed for each task.
     */
    public void setCoresPerTask(int coresPerTask) {
        this.coresPerTask = coresPerTask;
    }

    /**
     * Get the number of tasks per node.
     *
     * @return the number of tasks per node.
     */
    public int getTasksPerNode() {
        return tasksPerNode;
    }

    /**
     * Set the number of tasks allowed per node.
     *
     * @param tasksPerNode
     *            the number of tasks allowed per node.
     */
    public void setTasksPerNode(int tasksPerNode) {
        this.tasksPerNode = tasksPerNode;
    }

    /**
     * Get the amount of memory needed for process (in MBytes).
     *
     * @return the amount of memory needed.
     */
    public int getMaxMemory() {
        return maxMemory;
    }

    /**
     * Set the amount of memory needed for process (in MBytes).
     *
     * @param maxMemoryInMB
     *            the amount of memory needed per node/process.
     */
    public void setMaxMemory(int maxMemoryInMB) {
        this.maxMemory = maxMemoryInMB;
    }

    /**
     * Get the amount of temp space needed for process (in MBytes).
     *
     * @return the amount of temp space needed.
     */
    public int getTempSpace() {
        return tempSpace;
    }

    /**
     * Set the amount of memory needed for process (in MBytes).
     *
     * @param tempSpaceInMB
     *            the amount of temp space needed per node/process.
     */
    public void setTempSpace(int tempSpaceInMB) {
        this.tempSpace = tempSpaceInMB;
    }

    /**
     * Will the executable be started per task?
     *
     * <code>false</code> by default.
     *
     * @return if the executable is started per task.
     */
    public boolean isStartPerTask() {
        return startPerTask;
    }

    /**
     * Will the executable be started per job?
     *
     * <code>true</code> by default.
     *
     * @return if the executable is started per job.
     */
    public boolean isStartPerJob() {
        return !startPerTask;
    }

    /**
     * Set if the executable must be started for each task instead of once per job.
     */
    public void setStartPerTask() {
        this.startPerTask = true;
    }

    /**
     * Set if the executable must be started for once per job instead of for each task.
     */
    public void setStartPerJob() {
        this.startPerTask = false;
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
     * Get the maximum job duration time in minutes.
     *
     * @return the maximum job duration.
     */
    public int getMaxRuntime() {
        return maxRuntime;
    }

    /**
     * Set the maximum job duration in minutes.
     *
     * @param minutes
     *            the maximum job duration in minutes.
     */
    public void setMaxRuntime(int minutes) {
        this.maxRuntime = minutes;
    }

    /**
     * Get the start time of the job.
     *
     * @return the start time of the job.
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Set the start time of the job.
     *
     * Currently supported values are "now", or an explicit time and optional date in the format "HH:mm[ dd.MM[.YYYY]]"
     *
     * @param startTime
     *            the start time of the job.
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
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
        return Collections.unmodifiableList(arguments);
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
     * Get the scheduler specific arguments.
     *
     * @return Returns the scheduler specific arguments.
     */
    public List<String> getSchedulerArguments() {
        return Collections.unmodifiableList(schedulerArguments);
    }

    /**
     * Sets the scheduler specific arguments for this job.
     *
     * Some jobs require extra arguments to be provided to the scheduler, for example to select a certain type of node. These arguments tend to be very
     * scheduler and location specific and are therefore hard to generalize.
     *
     * This method provides a simple mechanism to add such arguments to a JobDescription. These arguments are typically copied into the scheduler specific
     * section of a generated submit script.
     *
     * @param arguments
     *            the scheduler specific arguments.
     */
    public void setSchedulerArguments(String... arguments) {
        this.schedulerArguments.clear();

        for (String argument : arguments) {
            addSchedulerArgument(argument);
        }
    }

    /**
     * Add a scheduler specific argument.
     *
     * The argument may not be <code>null</code> or empty.
     *
     * @param argument
     *            the scheduler specific argument.
     */
    public void addSchedulerArgument(String argument) {

        if (argument == null || argument.length() == 0) {
            throw new IllegalArgumentException("Scheduker argument may not be null or empty!");
        }

        schedulerArguments.add(argument);
    }

    /**
     * Get the environment of the executable.
     *
     * The environment of the executable consists of a {@link Map} of environment variables with their values (for example: "JAVA_HOME", "/path/to/java").
     *
     * @return the environment of the executable.
     */
    public Map<String, String> getEnvironment() {
        return Collections.unmodifiableMap(environment);
    }

    /**
     * Sets the environment of the executable.
     *
     * The environment of the executable consists of a {@link Map} of environment variables with their values (for example: "JAVA_HOME", "/path/to/java").
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
     * The environment of the executable consists of a {@link Map} of environment variables with their values (for example: "JAVA_HOME", "/path/to/java").
     *
     * The key of an environment variable may not be <code>null</code> or empty.
     *
     * @param key
     *            the unique key under which to store the value.
     * @param value
     *            the value to store the value.
     */
    public void addEnvironment(String key, String value) {

        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException("Envrionment variable name may not be null or empty!");
        }

        environment.put(key, value);
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

    /* Generated */
    @Override
    public String toString() {
        return "JobDescription [name=" + name + ", queueName=" + queueName + ", executable=" + executable + ", arguments=" + arguments + ", schedulerArguments="
                + schedulerArguments + ", stdin=" + stdin + ", stdout=" + stdout + ", stderr=" + stderr + ", workingDirectory=" + workingDirectory
                + ", environment=" + environment + ", tasks=" + tasks + ", coresPerTask=" + coresPerTask + ", tasksPerNode="
                + tasksPerNode + ", maxMemory=" + maxMemory + ", tempSpace=" + tempSpace + ", startPerTask=" + startPerTask + ", maxTime=" + maxRuntime + "]";
    }

    /* Generated */
    @SuppressWarnings("PMD.NPathComplexity")
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + arguments.hashCode();
        result = prime * result + schedulerArguments.hashCode();
        result = prime * result + environment.hashCode();
        result = prime * result + ((executable == null) ? 0 : executable.hashCode());
        result = prime * result + maxMemory;
        result = prime * result + tempSpace;
        result = prime * result + maxRuntime;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + tasks;
        result = prime * result + coresPerTask;
        result = prime * result + ((queueName == null) ? 0 : queueName.hashCode());
        result = prime * result + (startPerTask ? 1231 : 1237);
        result = prime * result + ((stderr == null) ? 0 : stderr.hashCode());
        result = prime * result + ((stdin == null) ? 0 : stdin.hashCode());
        result = prime * result + ((stdout == null) ? 0 : stdout.hashCode());
        result = prime * result + tasksPerNode;
        result = prime * result + ((workingDirectory == null) ? 0 : workingDirectory.hashCode());
        return result;
    }

    @SuppressWarnings("PMD.NPathComplexity")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        JobDescription other = (JobDescription) obj;

        return maxRuntime == other.maxRuntime && tasks == other.tasks && startPerTask == other.startPerTask && tempSpace == other.tempSpace
                && coresPerTask == other.coresPerTask && maxMemory == other.maxMemory && tasksPerNode == other.tasksPerNode && Objects.equals(name, other.name)
                && Objects.equals(executable, other.executable) && Objects.equals(workingDirectory, other.workingDirectory)
                && Objects.equals(queueName, other.queueName) && Objects.equals(stdin, other.stdin) && Objects.equals(stdout, other.stdout)
                && Objects.equals(stderr, other.stderr) && Objects.equals(arguments, other.arguments)
                && Objects.equals(schedulerArguments, other.schedulerArguments) && Objects.equals(environment, other.environment);
    }
}
