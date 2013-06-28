package nl.esciencecenter.octopus.adaptors.gridengine;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Properties;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.octopus.engine.util.RemoteCommandRunner;
import nl.esciencecenter.octopus.exceptions.NoSuchQueueException;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.UnknownPropertyException;
import nl.esciencecenter.octopus.jobs.Job;
import nl.esciencecenter.octopus.jobs.JobDescription;
import nl.esciencecenter.octopus.jobs.JobStatus;
import nl.esciencecenter.octopus.jobs.QueueStatus;
import nl.esciencecenter.octopus.jobs.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract connection to a remote scheduler, implemented by calling command line commands over a ssh connection. Actual interface
 * to be implemented by adaptors by running commands and parsing output.
 * 
 * @author Niels Drost
 * 
 */
public abstract class AbstractSchedulerConnection {

    //FIXME: this should be a setting
    public static final int POLL_DELAY = 100; //ms

    protected static final Logger logger = LoggerFactory.getLogger(GridEngineSchedulerConnection.class);

    private static int schedulerID = 0;

    protected static synchronized int getNextSchedulerID() {
        return schedulerID++;
    }

    protected final URI actualLocation;
    protected final OctopusProperties properties;
    protected final String[] queueNames;
    protected final String id;
    protected final OctopusEngine engine;
    protected final Scheduler sshScheduler;
    protected final SchedulerImplementation schedulerImplementation;

    public AbstractSchedulerConnection(URI location, Credential credential, Properties properties, OctopusEngine engine)
            throws OctopusIOException, OctopusException {
        this.engine = engine;
        this.properties = new OctopusProperties(properties);

        if (properties != null && properties.size() > 0) {
            throw new UnknownPropertyException(GridengineAdaptor.ADAPTOR_NAME,
                    "grid engine scheduler does not support any property");
        }

        GridengineAdaptor.checkLocation(location);
        try {

            id = GridengineAdaptor.ADAPTOR_NAME + "-" + getNextSchedulerID();

            if (location.getHost() == null || location.getHost().length() == 0) {
                //FIXME: check if this works for encode uri's, illegal characters, fragments, etc..
                actualLocation = new URI("local:///");
            } else {
                actualLocation = new URI("ssh", location.getSchemeSpecificPart(), location.getFragment());
            }
        } catch (URISyntaxException e) {
            throw new OctopusException(GridengineAdaptor.ADAPTOR_NAME, "cannot create ssh uri from given location", e);
        }

        logger.debug("creating ssh scheduler for GridEngine adaptor at " + actualLocation);
        sshScheduler = engine.jobs().newScheduler(actualLocation, credential, this.properties);

        this.queueNames = getQueueNamesFromServer();

        logger.debug("queues for " + location + " are " + Arrays.toString(this.queueNames));

        this.schedulerImplementation =
                new SchedulerImplementation(GridengineAdaptor.ADAPTOR_NAME, id, location, this.queueNames, credential,
                        getProperties(), false, false, true);
    }

    protected String runCommand(String stdin, String executable, String... arguments) throws OctopusException, OctopusIOException {
        JobDescription description = new JobDescription();
        description.setInteractive(true);
        description.setExecutable(executable);
        description.setArguments(arguments);

        RemoteCommandRunner runner = new RemoteCommandRunner(engine, sshScheduler, stdin, description);

        String stderr = runner.getStderr();

        if (runner.getExitCode() != 0 || !stderr.isEmpty()) {
            throw new CommandFailedException(GridengineAdaptor.ADAPTOR_NAME, "could not run command \"" + executable
                    + "\" at server \"" + actualLocation.getHost() + "\". Error output: " + stderr);
        }

        return runner.getStdout();
    }

    public String[] getQueueNames() {
        return queueNames;
    }

    public OctopusProperties getProperties() {
        return properties;
    }

    public SchedulerImplementation getScheduler() {
        return schedulerImplementation;
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

    private void checkQueueNames(String[] queueNames) throws NoSuchQueueException {
        for (String requestedQueueName : queueNames) {
            boolean found = false;
            for (String queueName : getQueueNames()) {
                if (requestedQueueName.equals(queueName)) {
                    found = true;
                }
            }
            if (!found) {
                throw new NoSuchQueueException(GridengineAdaptor.ADAPTOR_NAME, requestedQueueName
                        + " does not exist at this scheduler");
            }
        }
    }

    public QueueStatus getQueueStatus(String queueName) throws OctopusIOException, OctopusException {
        if (queueName == null) {
            throw new NullPointerException("queue name cannot be null");
        }

        String[] queueNames = { queueName };
        
        checkQueueNames(queueNames);

        return getQueueStatusesFromServer(queueNames)[0];
    }

    public QueueStatus[] getQueueStatuses(String... queueNames) throws OctopusIOException, OctopusException {
        if (queueNames.length == 0) {
            queueNames = getQueueNames();
        } else {
            checkQueueNames(queueNames);
        }

        return getQueueStatusesFromServer(queueNames);
    }

    public Job[] getJobs(String[] queueNames) throws OctopusIOException, OctopusException {
        if (queueNames.length == 0) {
            queueNames = getQueueNames();
        } else {
            checkQueueNames(queueNames);
        }

        return getJobsFromServer(queueNames);
    }

    abstract String[] getQueueNamesFromServer() throws OctopusIOException, OctopusException;

    abstract QueueStatus[] getQueueStatusesFromServer(String[] queueNames) throws OctopusIOException, OctopusException;

    abstract Job[] getJobsFromServer(String[] queueNames) throws OctopusIOException, OctopusException;

    public abstract Job submitJob(JobDescription description) throws OctopusException, OctopusIOException;

    public abstract JobStatus getJobStatus(Job job) throws OctopusException, OctopusIOException;

    public abstract JobStatus[] getJobStatuses(Job[] jobs) throws OctopusException, OctopusIOException;

    public abstract JobStatus cancelJob(Job job) throws OctopusIOException, OctopusException;

}