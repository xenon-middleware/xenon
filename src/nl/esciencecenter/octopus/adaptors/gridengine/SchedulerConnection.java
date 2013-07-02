package nl.esciencecenter.octopus.adaptors.gridengine;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.util.RemoteCommandRunner;
import nl.esciencecenter.octopus.exceptions.IncompleteJobDescriptionException;
import nl.esciencecenter.octopus.exceptions.InvalidJobDescriptionException;
import nl.esciencecenter.octopus.exceptions.InvalidLocationException;
import nl.esciencecenter.octopus.exceptions.NoSuchQueueException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.UnknownPropertyException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection to a remote scheduler, implemented by calling command line commands over a ssh connection.
 * 
 * @author Niels Drost
 * 
 */
public abstract class SchedulerConnection {

    //FIXME: this should be a setting
    public static final int POLL_DELAY = 100; //ms

    protected static final Logger logger = LoggerFactory.getLogger(SchedulerConnection.class);

    private static int schedulerID = 0;

    protected static synchronized int getNextSchedulerID() {
        return schedulerID++;
    }

    private final String adaptorName;
    private final String[] adaptorSchemes;
    private final OctopusProperties properties;
    private final String id;
    private final OctopusEngine engine;
    private final Scheduler sshScheduler;
    private final FileSystem sshFileSystem;

    protected SchedulerConnection(URI location, Credential credential, Properties properties, OctopusEngine engine,
            String adaptorName, String[] adaptorSchemes) throws OctopusIOException, OctopusException {
        this.engine = engine;
        this.properties = new OctopusProperties(properties);
        this.adaptorName = adaptorName;
        this.adaptorSchemes = adaptorSchemes;

        if (properties != null && properties.size() > 0) {
            throw new UnknownPropertyException(adaptorName, "scheduler does not support any property");
        }

        try {
            checkLocation(location);

            id = adaptorName + "-" + getNextSchedulerID();
            URI actualLocation = new URI("ssh", location.getSchemeSpecificPart(), location.getFragment());

            if (location.getHost() == null || location.getHost().length() == 0) {
                //FIXME: check if this works for encode uri's, illegal characters, fragments, etc..
                actualLocation = new URI("local:///");
            }

            logger.debug("creating ssh scheduler for {} adaptor at {}", adaptorName, actualLocation);
            sshScheduler = engine.jobs().newScheduler(actualLocation, credential, this.properties);

            logger.debug("creating file system for {} adaptor at {}", adaptorName, actualLocation);
            sshFileSystem = engine.files().newFileSystem(actualLocation, credential, this.properties);

        } catch (URISyntaxException e) {
            throw new OctopusException(adaptorName, "cannot create ssh uri from given location " + location, e);
        }
    }

    //testing version
    protected SchedulerConnection() {
        adaptorName = null;
        adaptorSchemes = null;
        properties = null;
        id = null;
        engine = null;
        sshScheduler = null;
        sshFileSystem = null;
    }

    void checkLocation(URI location) throws InvalidLocationException {
        //only null or "/" are allowed as paths
        if (!(location.getPath() == null || location.getPath().length() == 0 || location.getPath().equals("/"))) {
            throw new InvalidLocationException(adaptorName, "Paths are not allowed in a uri for this scheduler, uri given: "
                    + location);
        }

        if (location.getFragment() != null && location.getFragment().length() > 0) {
            throw new InvalidLocationException(adaptorName, "Fragments are not allowed in a uri for this scheduler, uri given: "
                    + location);
        }

        for (String scheme : adaptorSchemes) {
            if (scheme.equals(location.getScheme())) {
                //alls-well
                return;
            }
        }
        throw new InvalidLocationException(adaptorName, "Adaptor does not support scheme: " + location.getScheme());
    }

    String runCommand(String stdin, String executable, String... arguments) throws OctopusException, OctopusIOException {
        JobDescription description = new JobDescription();
        description.setInteractive(true);
        description.setExecutable(executable);
        description.setArguments(arguments);

        RemoteCommandRunner runner = new RemoteCommandRunner(engine, sshScheduler, stdin, description);

        String stderr = runner.getStderr();

        if (runner.getExitCode() != 0 || !stderr.isEmpty()) {
            throw new CommandFailedException(adaptorName, "could not run command \"" + executable + "\" with arguments \""
                    + Arrays.toString(arguments) + "\" at \"" + sshScheduler + "\". Exit code = " + runner.getExitCode()
                    + " Error output: " + stderr, runner.getExitCode(), runner.getStderr());
        }

        return runner.getStdout();
    }

    /**
     * Checks if the queue names given are valid, and throw an exception otherwise. Checks against the list of queues when the
     * scheduler was created.
     */
    protected void checkQueueNames(String[] givenQueueNames) throws NoSuchQueueException {
        //create a hashset with all given queues
        HashSet<String> invalidQueues = new HashSet<String>(Arrays.asList(givenQueueNames));

        //remove all valid queues from the set
        invalidQueues.removeAll(Arrays.asList(getQueueNames()));

        //if anything remains, these are invalid. throw an exception with the invalid queues
        if (!invalidQueues.isEmpty()) {
            throw new NoSuchQueueException(adaptorName, "Invalid queues given: "
                    + Arrays.toString(invalidQueues.toArray(new String[0])));
        }
    }

    public OctopusProperties getProperties() {
        return properties;
    }

    public String getID() {
        return id;
    }

    public void close() throws OctopusIOException, OctopusException {
        engine.jobs().close(sshScheduler);
    }

    public JobStatus waitUntilDone(Job job, long timeout) throws OctopusIOException, OctopusException {
        long deadline = System.currentTimeMillis() + timeout;

        if (timeout == 0) {
            deadline = Long.MAX_VALUE;
        }

        JobStatus status = getJobStatus(job);

        while (System.currentTimeMillis() < deadline) {
            status = getJobStatus(job);

            if (status.isDone()) {
                return status;
            }

            try {
                Thread.sleep(POLL_DELAY);
            } catch (InterruptedException e) {
                return status;
            }
        }

        return status;
    }

    public JobStatus waitUntilRunning(Job job, long timeout) throws OctopusIOException, OctopusException {
        long deadline = System.currentTimeMillis() + timeout;

        if (timeout == 0) {
            deadline = Long.MAX_VALUE;
        }

        JobStatus status = getJobStatus(job);

        while (System.currentTimeMillis() < deadline) {
            status = getJobStatus(job);

            if (status.isRunning() || status.isDone()) {
                return status;
            }

            try {
                Thread.sleep(POLL_DELAY);
            } catch (InterruptedException e) {
                return status;
            }
        }

        return status;
    }

    //do some checks on the job description. subclass could perform additional checks
    protected void verifyJobDescription(JobDescription description) throws OctopusException {
        String executable = description.getExecutable();

        if (executable == null) {
            throw new IncompleteJobDescriptionException(adaptorName, "Executable missing in JobDescription!");
        }

        int nodeCount = description.getNodeCount();

        if (nodeCount < 1) {
            throw new InvalidJobDescriptionException(adaptorName, "Illegal node count: " + nodeCount);
        }

        int processesPerNode = description.getProcessesPerNode();

        if (processesPerNode < 1) {
            throw new InvalidJobDescriptionException(adaptorName, "Illegal processes per node count: " + processesPerNode);
        }

        int maxTime = description.getMaxTime();

        if (maxTime <= 0) {
            throw new InvalidJobDescriptionException(adaptorName, "Illegal maximum runtime: " + maxTime);
        }

        if (description.isInteractive()) {
            throw new InvalidJobDescriptionException(adaptorName, "Adaptor does not support interactive jobs");
        }
    }

    protected AbsolutePath getFsEntryPath() {
        return sshFileSystem.getEntryPath();
    }

    //implemented by sub-class

    /**
     * As the SchedulerImplementation contains the list of queues, the subclass is responsible of implementing this function
     */
    abstract Scheduler getScheduler();

    abstract String[] getQueueNames();

    abstract QueueStatus getQueueStatus(String queueName) throws OctopusIOException, OctopusException;

    abstract QueueStatus[] getQueueStatuses(String... queueNames) throws OctopusIOException, OctopusException;

    abstract Job[] getJobs(String... queueNames) throws OctopusIOException, OctopusException;

    abstract Job submitJob(JobDescription description) throws OctopusIOException, OctopusException;

    abstract JobStatus cancelJob(Job job) throws OctopusIOException, OctopusException;

    abstract JobStatus getJobStatus(Job job) throws OctopusException, OctopusIOException;

    abstract JobStatus[] getJobStatuses(Job... jobs) throws OctopusIOException, OctopusException;

}