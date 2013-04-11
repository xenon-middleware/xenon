package nl.esciencecenter.octopus.engine.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.octopus.jobs.JobDescription;

public final class JobDescriptionImplementation implements JobDescription {

    private String queueName = null;

    private String executable = null;

    private String stdin = null;

    private String stdout = "stdout.txt";

    private String stderr = "stderr.txt";

    private String workingDirectory = null;

    private List<String> arguments = new ArrayList<String>();

    private Map<String, String> environment = new HashMap<String, String>();

    private int nodeCount = 1;

    private int processesPerNode = 1;

    private int maxTime = 30; // minutes
    
    private boolean offlineMode = false;
    
    protected JobDescriptionImplementation() { 
        // nothing
    }
    
    /**
     * Get the number of nodes.
     * 
     * @return the number of resources
     */
    public int getNodeCount() {
        return nodeCount;
    }

    /**
     * Set the number of resources, which is the total number of resources where the number of processes should be distributed on.
     * 
     * @param resourceCount
     *            the number of resources
     */
    public void setNodeCount(int resourceCount) {
        this.nodeCount = resourceCount;
    }

    /**
     * Get the number of processes started on each node. The total number of processes started is getProcessesPerNode() *
     * getNodeCount()
     * 
     * @return the number of processes
     */
    public int getProcessesPerNode() {
        return processesPerNode;
    }

    /**
     * Get the number of processes started on each node.
     * 
     * @param ppn
     *            the number of processes
     */
    public void setProcessesPerNode(int ppn) {
        this.processesPerNode = ppn;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }

    /**
     * Returns the path to the executable. For the following commandline <code>"/bin/cat hello world > out"</code> it will return
     * a {@link String} "/bin/cat".
     * 
     * @return the path to the executable.
     */
    public String getExecutable() {
        return executable;
    }

    /**
     * Sets the path to the executable. For the following commandline <code>"/bin/cat hello world > out"</code> the {@link String}
     * "/bin/cat" should be provided.
     * 
     * @param executable
     *            The path to the executable.
     */
    public void setExecutable(String executable) {
        this.executable = executable;
    }

    /**
     * Returns the arguments of the executable. For the following commandline <code>"/bin/cat hello world > out"</code> it will
     * return a {@link String} []{"hello", "world", ">", "out"}
     * 
     * @return Returns the commandline arguments.
     */
    public List<String> getArguments() {
        return arguments;
    }

    /**
     * Sets the arguments of the executable. For the following commandline <code>"/bin/cat hello world"</code> the {@link String}
     * []{"hello", "world"} contains the arguments.
     * 
     * @param arguments
     *            The commandline arguments to set.
     */
    public void setArguments(String... arguments) {
        this.arguments.clear();
        this.arguments.addAll(Arrays.asList(arguments));
    }

    /**
     * Returns the environment of the executable. The environment of the executable consists of a {@link Map} of environment
     * variables with their values (for instance the key, value pair "JAVA_HOME", "/path/to/java").
     * 
     * @return the environment
     */
    public Map<String, String> getEnvironment() {
        return environment;
    }

    /**
     * Sets the environment of the executable. The environment of the executable consists of a {@link Map} of environment
     * variables with their values (for instance the key, value pair "JAVA_HOME", "/path/to/java").
     * 
     * @param environment
     *            The environment to set.
     */
    public void setEnvironment(Map<String, String> environment) {
        this.environment = new HashMap<String, String>(environment);
    }

    public boolean offlineMode() {
        return offlineMode;
    }

    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode = offlineMode;
    }
    @Override
    public void setStdin(String stdin) {
        this.stdin = stdin;
    }

    @Override
    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    @Override
    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    @Override
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    @Override
    public String getStdin() {
        return stdin;
    }

    @Override
    public String getStdout() {
        return stdout;
    }

    @Override
    public String getStderr() {
        return stderr;
    }

    @Override
    public String getWorkingDirectory() {
        return workingDirectory;
    }
    
    public String toString() {
        String res = "JobDescription(";
        res += "node count: " + nodeCount;
        res += "ppn: " + processesPerNode;
        res += "queue: " + queueName;
        res += "maxTime: " + maxTime;
        res += "executable: " + executable;
        res += ", arguments: " + arguments;
        res += ", environment: " + environment;

        res += ", stdin: " + stdin;
        res += ", stdout: " + stdout;
        res += ", stderr: " + stderr;

        res += ", offlineMode: " + offlineMode;

        res += ")";

        return res;
    }
}
