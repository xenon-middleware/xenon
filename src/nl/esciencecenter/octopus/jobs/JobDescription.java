package nl.esciencecenter.octopus.jobs;

import java.util.List;
import java.util.Map;

public interface JobDescription {

    /**
     * Get the number of nodes.
     * 
     * @return the number of resources
     */
    public int getNodeCount();

    /**
     * Set the number of resources, which is the total number of resources where the number of processes should be distributed on.
     * 
     * @param resourceCount
     *            the number of resources
     */
    public void setNodeCount(int resourceCount); 

    /**
     * Get the number of processes started on each node. The total number of processes started is getProcessesPerNode() *
     * getNodeCount()
     * 
     * @return the number of processes
     */
    public int getProcessesPerNode();

    /**
     * Get the number of processes started on each node.
     * 
     * @param ppn
     *            the number of processes
     */
    public void setProcessesPerNode(int ppn);

    public String getQueueName();

    /** 
     * Set the queuename to use in the scheduler. 
     * 
     * @param queueName
     */
    public void setQueueName(String queueName);

    public int getMaxTime();

    public void setMaxTime(int maxTime);

    /**
     * Returns the path to the executable. For the following commandline <code>"/bin/cat hello world > out"</code> it will return
     * a {@link String} "/bin/cat".
     * 
     * @return the path to the executable.
     */
    public String getExecutable();

    /**
     * Sets the path to the executable. For the following commandline <code>"/bin/cat hello world > out"</code> the {@link String}
     * "/bin/cat" should be provided.
     * 
     * @param executable
     *            The path to the executable.
     */
    public void setExecutable(String executable);

    /**
     * Returns the arguments of the executable. For the following commandline <code>"/bin/cat hello world > out"</code> it will
     * return a {@link String} []{"hello", "world", ">", "out"}
     * 
     * @return Returns the commandline arguments.
     */
    public List<String> getArguments();

    /**
     * Sets the arguments of the executable. For the following commandline <code>"/bin/cat hello world"</code> the {@link String}
     * []{"hello", "world"} contains the arguments.
     * 
     * @param arguments
     *            The commandline arguments to set.
     */
    public void setArguments(String... arguments);

    /**
     * Returns the environment of the executable. The environment of the executable consists of a {@link Map} of environment
     * variables with their values (for instance the key, value pair "JAVA_HOME", "/path/to/java").
     * 
     * @return the environment
     */
    public Map<String, String> getEnvironment();

    /**
     * Sets the environment of the executable. The environment of the executable consists of a {@link Map} of environment
     * variables with their values (for instance the key, value pair "JAVA_HOME", "/path/to/java").
     * 
     * @param environment
     *            The environment to set.
     */
    public void setEnvironment(Map<String, String> environment);

    public String getStdin();

    /**
     * Set the location of the file providing stdin (relative to the working directory). 
     *  
     * Default is "$(workingDirectory)/stdin.txt" 
     *  
     * @param stdin the location of the file from which stdin is redirected. 
     */
    public void setStdin(String stdin);

    public String getStdout();

    /**
     * Set the location of the file to which to redirect stdout (relative to the working directory). 
     *  
     * Default is "$(workingDirectory)/stdout.txt" 
     *  
     * @param stdout the location of the file where stdout is redirected to. 
     */
    public void setStdout(String stdout);

    public String getStderr();

    /**
     * Set the location of the file to which to redirect stderr (relative to the working directory). 
     *  
     * Default is "$(workingDirectory)/stderr.txt" 
     *  
     * @param stderr the location of the file where stderr is redirected to. 
     */
    public void setStderr(String stderr);
    
    /** 
     * Set the location of the working directory for the job (relative to a scheduler specific root).  
     * 
     * @param workingDirectory the location of the working directory.
     */
    public void setWorkingDirectory(String workingDirectory);
    
    public String getWorkingDirectory();
    
    public boolean offlineMode();

    public void setOfflineMode(boolean offlineMode);

}
