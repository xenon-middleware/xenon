package nl.esciencecenter.octopus.jobs;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Path;

public class JobDescription {

    private int nodeCount = 1;

    private int processesPerNode = 1;

    private String queueName = null;

    private int maxTime = 30; // minutes

    private String executable = null;

    private List<String> arguments = new ArrayList<String>();

    private Map<String, String> environment = new HashMap<String, String>();

    private Path stdin = null;

    private Path stdout = null;

    private Path stderr = null;

    private Path workingDirectory = null;
    
    private boolean offlineMode = false;

    public JobDescription() {
        // NOTHING
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
     * Set the number of resources, which is the total number of resources where
     * the number of processes should be distributed on.
     * 
     * @param resourceCount
     *            the number of resources
     */
    public void setNodeCount(int resourceCount) {
        this.nodeCount = resourceCount;
    }

    /**
     * Get the number of processes started on each node. The total number of
     * processes started is getProcessesPerNode() * getNodeCount()
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
     * Returns the path to the executable. For the following commandline
     * <code>"/bin/cat hello world > out"</code> it will return a {@link String}
     * "/bin/cat".
     * 
     * @return the path to the executable.
     */
    public String getExecutable() {
        return executable;
    }

    /**
     * Sets the path to the executable. For the following commandline
     * <code>"/bin/cat hello world > out"</code> the {@link String} "/bin/cat"
     * should be provided.
     * 
     * @param executable
     *            The path to the executable.
     */
    public void setExecutable(String executable) {
        this.executable = executable;
    }

    /**
     * Returns the arguments of the executable. For the following commandline
     * <code>"/bin/cat hello world > out"</code> it will return a {@link String}
     * []{"hello", "world", ">", "out"}
     * 
     * @return Returns the commandline arguments.
     */
    public List<String> getArguments() {
        return arguments;
    }

    /**
     * Sets the arguments of the executable. For the following commandline
     * <code>"/bin/cat hello world"</code> the {@link String}[]{"hello",
     * "world"} contains the arguments.
     * 
     * @param arguments
     *            The commandline arguments to set.
     */
    public void setArguments(String... arguments) {
        this.arguments.clear();
        this.arguments.addAll(Arrays.asList(arguments));
    }

    /**
     * Returns the environment of the executable. The environment of the
     * executable consists of a {@link Map} of environment variables with their
     * values (for instance the key, value pair "JAVA_HOME", "/path/to/java").
     * 
     * @return the environment
     */
    public Map<String, String> getEnvironment() {
        return environment;
    }

    /**
     * Sets the environment of the executable. The environment of the executable
     * consists of a {@link Map} of environment variables with their values (for
     * instance the key, value pair "JAVA_HOME", "/path/to/java").
     * 
     * @param environment
     *            The environment to set.
     */
    public void setEnvironment(Map<String, String> environment) {
        this.environment = new HashMap<String, String>(environment);
    }

    /**
     * Returns the stdin {@link Path}.
     * 
     * @return the stdin {@link Path}.
     */
    public Path getStdin() {
        return stdin;
    }

    /**
     * Sets the {@link Path} where stdin is redirected from.
     * 
     * @param stdin
     *            The {@link Path} where stdin is redirected from.
     */
    public void setStdin(Path stdin) {
        this.stdin = stdin;
    }

    /**
     * Returns the stdout {@link Path}.
     * 
     * @return the stdout {@link Path}.
     */
    public Path getStdout() {
        return stdout;
    }

    /**
     * Sets the stdout {@link Path}. Note that stdout will be redirected to
     * either a {@link Path} or a {@link OutputStream}. The last invocation of
     * <code>setStdout()</code> determines whether the destination of the
     * output.
     * 
     * @param stdout
     *            The {@link Path} where stdout is redirected to.
     */
    public void setStdout(Path stdout) {
        this.stdout = stdout;
    }

    /**
     * Returns the stderr {@link Path}.
     * 
     * @return the stderr {@link Path}
     */
    public Path getStderr() {
        return stderr;
    }

    /**
     * Sets the stderr {@link Path}. Note that stderr will be redirected to
     * either a {@link Path} or a {@link OutputStream}. The last invocation of
     * <code>setStderr()</code> determines whether the destination of the
     * output.
     * 
     * @param stderr
     *            The {@link Path} where stderr is redirected to.
     */
    public void setStderr(Path stderr) {
        this.stderr = stderr;
    }

    public void setWorkingDirectory(Path workingDirectory) throws OctopusException {
    	
    	if (!workingDirectory.isLocal()) { 
    		throw new OctopusException("Working directory must be local not " + workingDirectory);
    	}
    	
    	this.workingDirectory = workingDirectory;
    }

    public Path getWorkingDirectory() {
        return workingDirectory;
    }
    
    public boolean offlineMode() {
        return offlineMode;
    }

    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode = offlineMode;
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

        //res += ", preStagedFiles: " + preStagedFiles;
        //res += ", postStagedFiles: " + postStagedFiles;
        
        //res += ", deleteSandbox: " + deleteSandbox;
        //res += ", wipeSandbox: " + wipeSandbox;
        res += ", offlineMode: " + offlineMode;

        res += ")";

        return res;
    }

}
