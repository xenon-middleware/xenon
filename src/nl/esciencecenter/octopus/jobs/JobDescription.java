package nl.esciencecenter.octopus.jobs;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Map<Path, Path> preStagedFiles = new HashMap<Path, Path>();

    private Map<Path, Path> postStagedFiles = new HashMap<Path, Path>();

    private boolean deleteSandbox = true;

    private boolean wipeSandbox = false;
    
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

    /**
     * Returns the pre staged file set. This a {@link Map} with the source
     * {@link Path}s as keys and the destination {@link Path}s as values. This
     * method returns the files that should be pre staged regardless of whether
     * they are already pre staged or not.
     * 
     * @return the pre staged file set.
     */
    public Map<Path, Path> getPreStagedFiles() {
        return preStagedFiles;
    }

    /**
     * Sets the pre staged file set. Any former pre staged files added to the
     * pre staged file set are no longer part of the pre staged file set. More
     * {@link Path}s can be added using the <code>addPreStagedPath</code>
     * methods. See these methods for a table stating at which locations the
     * {@link Path}s will end up after the pre staging.
     * 
     * @param files
     *            An array of files that should be pre staged.
     */
    public void setPreStagedFiles(Path... files) {
        preStagedFiles = new HashMap<Path, Path>();
        for (int i = 0; i < files.length; i++) {
            addPreStagedFile(files[i]);
        }
    }

    /**
     * Add a single pre stage file. This is similar to
     * <code>addPreStagedPath(src, null)</code>.
     * 
     * @param src
     *            the file that should be pre staged.
     */
    public void addPreStagedFile(Path src) {
        addPreStagedFile(src, null);
    }

    /**
     * Add a single pre stage file that should be pre staged to the given
     * destination. The table below shows where the pre stage files will end up
     * after pre staging.
     * <p>
     * <TABLE border="2" frame="box" rules="groups" summary="pre staging * overview" cellpadding="2">
     * <CAPTION>where do the pre staged files end up </CAPTION> <COLGROUP
     * align="left"> <COLGROUP align="center"> <COLGROUP align="left" > <THEAD
     * valign="top">
     * 
     * <TR>
     * <TH>source file
     * <TH>destination file
     * <TH>location after pre staging<TBODY>
     * <TR>
     * <TD><code>path/to/file</code>
     * <TD><code>null</code>
     * <TD><code>sandbox/file</code>
     * <TR>
     * <TD><code>path/to/file</code>
     * <TD><code>other/path/to/file</code>
     * <TD><code>sandbox/other/path/to/file</code>
     * <TR>
     * <TD><code>path/to/file</code>
     * <TD><code>/other/path/to/file</code>
     * <TD><code>/other/path/to/file</code>
     * <TR>
     * <TD><code>/path/to/file</code>
     * <TD><code>null</code>
     * <TD><code>sandbox/file</code>
     * <TR>
     * <TD><code>/path/to/file</code>
     * <TD><code>other/path/to/file</code>
     * <TD><code>sandbox/other/path/to/file</code>
     * <TR>
     * <TD><code>/path/to/file</code>
     * <TD><code>/other/path/to/file</code>
     * <TD><code>/other/path/to/file</code>
     * </TABLE>
     * 
     * @param src
     *            the {@link Path} that should be pre staged (may not be
     *            <code>null</code>)
     * @param dest
     *            the {@link Path} that should exist after the pre staging (may
     *            be <code>null</code>, see table).
     */
    public void addPreStagedFile(Path src, Path dest) {
        if (src == null) {
            throw new NullPointerException("the source file cannot be null when adding a preStaged file");
        }
        preStagedFiles.put(src, dest);
    }

    /**
     * Returns the post stage file set. The key {@link Path}s are the source
     * files on the execution site, the values are the {@link Path}s with the
     * destination of the post staging. This method returns the files that
     * should be post staged regardless of whether they are already post staged
     * or not.
     * 
     * @return the post stage file set
     */
    public Map<Path, Path> getPostStagedFiles() {
        return postStagedFiles;
    }

    /**
     * Sets the post staged file set. Any former post staged files added to the
     * post staged file set are no longer part of the post staged file set. More
     * {@link Path}s can be added using the <code>addPostStagedPath</code>
     * methods. See these methods for a table stating at which locations the
     * {@link Path}s will end up after the post staging.
     * 
     * @param files
     *            An array of files that should be pre staged.
     */
    public void setPostStagedFiles(Path... files) {
        postStagedFiles = new HashMap<Path, Path>();
        for (int i = 0; i < files.length; i++) {
            addPostStagedFiles(files[i]);
        }
    }

    /**
     * Add a single post stage file. This is similar to
     * <code>addPostStagedPath(src, null)</code>.
     * 
     * @param src
     *            the file that should be post staged.
     */
    public void addPostStagedFiles(Path src) {
        addPostStagedFiles(src, null);
    }

    /**
     * Add a single post stage file that should be post staged to the given
     * destination. The table below shows where the post stage files will end up
     * after post staging.
     * <p>
     * <TABLE border="2" frame="box" rules="groups" summary="post staging * overview" cellpadding="2">
     * <CAPTION>where do the post staged files end up </CAPTION> <COLGROUP
     * align="left"> <COLGROUP align="center"> <COLGROUP align="left" > <THEAD
     * valign="top">
     * 
     * <TR>
     * <TH>source file
     * <TH>destination file
     * <TH>location after post staging<TBODY>
     * <TR>
     * <TD><code>path/to/file</code>
     * <TD><code>null</code>
     * <TD><code>cwd/file</code>
     * <TR>
     * <TD><code>path/to/file</code>
     * <TD><code>other/path/to/file</code>
     * <TD><code>cwd/other/path/to/file</code>
     * <TR>
     * <TD><code>path/to/file</code>
     * <TD><code>/other/path/to/file</code>
     * <TD><code>/other/path/to/file</code>
     * <TR>
     * <TD><code>/path/to/file</code>
     * <TD><code>null</code>
     * <TD><code>cwd/file</code>
     * <TR>
     * <TD><code>/path/to/file</code>
     * <TD><code>other/path/to/file</code>
     * <TD><code>cwd/other/path/to/file</code>
     * <TR>
     * <TD><code>/path/to/file</code>
     * <TD><code>/other/path/to/file</code>
     * <TD><code>/other/path/to/file</code>
     * </TABLE>
     * 
     * @param src
     *            the {@link Path} that should be post staged (may not be
     *            <code>null</code>)
     * @param dest
     *            the {@link Path} that should exist after the post staging (may
     *            be <code>null</code>, see table).
     */
    public void addPostStagedFiles(Path src, Path dest) {
        if (src == null) {
            throw new NullPointerException("the source file cannot be null when adding a postStaged file");
        }

        postStagedFiles.put(src, dest);
    }

    public boolean deleteSandbox() {
        return deleteSandbox;
    }

    public void setDeleteSandbox(boolean deleteSandbox) {
        this.deleteSandbox = deleteSandbox;
    }

    public boolean wipeSandbox() {
        return wipeSandbox;
    }

    /**
     * Sets the wipe sandbox flag. If set, the sandbox will be overwritten
     * before being deleted, to make sure no data remains on the harddrive.
     * Useful for privacy sensitive data. Implies deleteSandbox() == true
     */
    public void setWipeSandbox(boolean wipeSandbox) {
        this.wipeSandbox = wipeSandbox;
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

        res += ", preStagedFiles: " + preStagedFiles;
        res += ", postStagedFiles: " + postStagedFiles;
        
        res += ", deleteSandbox: " + deleteSandbox;
        res += ", wipeSandbox: " + wipeSandbox;
        res += ", offlineMode: " + offlineMode;

        res += ")";

        return res;
    }

}
