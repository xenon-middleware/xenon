package nl.esciencecenter.octopus.adaptors.gridengine;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.jobs.JobImplementation;
import nl.esciencecenter.octopus.engine.jobs.JobStatusImplementation;
import nl.esciencecenter.octopus.engine.jobs.QueueStatusImplementation;
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
 * Connection to a remote scheduler, implemented by calling command line commands over a ssh connection.
 * 
 * @author Niels Drost
 * 
 */
public class GridEngineSchedulerConnection {

    //FIXME: this should be a setting
    public static final int POLL_DELAY = 100; //ms

    protected static final Logger logger = LoggerFactory.getLogger(GridEngineSchedulerConnection.class);

    private static int schedulerID = 0;

    protected static synchronized int getNextSchedulerID() {
        return schedulerID++;
    }

    private final QstatOutputParser parser;

    public static final int QACCT_GRACE_TIME = 60000; //ms, 1 minute

    /**
     * Map with the last seen time of jobs. There is a short but noticeable delay between jobs disappearing from the qstat queue
     * output, and information about this job appearing in the qacct output. Instead of throwing an exception, we allow for a
     * certain grace time. Jobs will report the status "pending" during this time.
     */
    private final HashMap<String, Long> lastSeenMap;

    private final URI actualLocation;
    private final OctopusProperties properties;
    private final String[] queueNames;
    private final String id;
    private final OctopusEngine engine;
    private final Scheduler sshScheduler;
    private final SchedulerImplementation schedulerImplementation;

    public GridEngineSchedulerConnection(URI location, Credential credential, Properties properties, OctopusEngine engine)
            throws OctopusIOException, OctopusException {
        this.engine = engine;
        this.properties = new OctopusProperties(properties);
        
        if (properties != null && properties.size() > 0) {
            throw new UnknownPropertyException(GridengineAdaptor.ADAPTOR_NAME,
                    "grid engine scheduler does not support any property");
        }
        
        lastSeenMap = new HashMap<String, Long>();

        parser = new QstatOutputParser(false);
        
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
    
    private synchronized void markJobSeen(String identifier) {
        long current = System.currentTimeMillis();

        lastSeenMap.put(identifier, current);
    }

    private synchronized void markJobsSeen(Set<String> identifiers) {
        long current = System.currentTimeMillis();

        for (String identifier : identifiers) {
            lastSeenMap.put(identifier, current);
        }
    }

    private synchronized void clearJobSeen(String identifier) {
        lastSeenMap.remove(identifier);
    }

    private synchronized boolean haveRecentlySeen(String identifier) {
        if (!lastSeenMap.containsKey(identifier)) {
            return false;
        }
        return System.currentTimeMillis() < (lastSeenMap.get(identifier) + QACCT_GRACE_TIME);
    }

    private String runCommand(String stdin, String executable, String... arguments) throws OctopusException, OctopusIOException {
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
    
    public String[] getQueueNamesFromServer() throws OctopusIOException, OctopusException {
        String qstatOutput = runCommand(null, "qstat", "-xml", "-g", "c");

        Map<String, Map<String, String>> allMap = parser.parseQueueInfos(qstatOutput);

        return allMap.keySet().toArray(new String[0]);
    }

    public QueueStatus getQueueStatus(String queueName) throws OctopusIOException, OctopusException {
        if (queueName == null) {
            throw new NullPointerException("queue name cannot be null");
        }

        String[] queueNames = { queueName };

        checkQueueNames(queueNames);

        return getQueueStatuses(queueNames)[0];
    }

    public QueueStatus[] getQueueStatuses(String... queueNames) throws OctopusIOException, OctopusException {
        if (queueNames.length == 0) {
            queueNames = getQueueNames();
        } else {
            checkQueueNames(queueNames);
        }

        QueueStatus[] result = new QueueStatus[queueNames.length];

        String qstatOutput = runCommand(null, "qstat", "-xml", "-g", "c");

        Map<String, Map<String, String>> allMap = parser.parseQueueInfos(qstatOutput);

        for (int i = 0; i < queueNames.length; i++) {
            if (allMap == null || allMap.isEmpty()) {
                Exception exception =
                        new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Failed to get status of queues on server");
                result[i] = new QueueStatusImplementation(getScheduler(), queueNames[i], exception, null);
            }

            //state for only the requested job
            Map<String, String> map = allMap.get(queueNames[i]);

            if (map == null || map.isEmpty()) {
                Exception exception =
                        new NoSuchQueueException(GridengineAdaptor.ADAPTOR_NAME, "Cannot get status of queue " + queueNames[i]
                                + " from server");
                result[i] = new QueueStatusImplementation(getScheduler(), queueNames[i], exception, null);
            }

            result[i] = new QueueStatusImplementation(getScheduler(), queueNames[i], null, map);
        }

        return result;
    }

    public Job[] getJobs(String[] queueNames) throws OctopusIOException, OctopusException {
        if (queueNames.length == 0) {
            queueNames = getQueueNames();
        } else {
            checkQueueNames(queueNames);
        }

        String statusOutput = runCommand(null, "qstat", "-xml");

        Map<String, Map<String, String>> status = parser.parseJobInfos(statusOutput);

        String[] jobIDs = status.keySet().toArray(new String[0]);

        Job[] result = new Job[jobIDs.length];

        for (int i = 0; i < result.length; i++) {
            markJobSeen(jobIDs[i]);
            result[i] = new JobImplementation(schedulerImplementation, jobIDs[i], null, false, false);
        }

        return result;
    }
    
    /**
     * Submit a job to a GridEngine machine. Mostly involves parsing output
     * 
     * @param description
     *            the script to submit
     * @return the id of the job
     * @throws OctopusException
     *             if the qsub command could not be run
     * @throws OctopusIOException
     */
    public Job submitJob(JobDescription description) throws OctopusException, OctopusIOException {
        String jobScript = JobScriptGenerator.generate(description);

        String output = runCommand(jobScript, "qsub");

        String identifier = QsubOutputParser.checkSubmitJobResult(output);

        markJobSeen(identifier);
        return new JobImplementation(schedulerImplementation, identifier, description, false, false);
    }
    
    /**
     * Cancel a job.
     * 
     * @param job
     *            the job to cancel
     * @return
     * @throws OctopusException
     *             if the qdel command could not be run
     * @throws OctopusIOException
     *             if the qdel command could not be run, or the jobID could not be read from the output
     */
    public JobStatus cancelJob(Job job) throws OctopusIOException, OctopusException {
        String output = runCommand(null, "qdel", job.getIdentifier());

        QdelOutputParser.checkCancelJobResult(job, output);

        return null;
    }

    public JobStatus getJobStatus(Job job) throws OctopusException, OctopusIOException {

        String statusOutput = runCommand(null, "qstat", "-xml");

        Map<String, Map<String, String>> allMap = parser.parseJobInfos(statusOutput);

        markJobsSeen(allMap.keySet());

        Map<String, String> map = allMap.get(job.getIdentifier());

        if (map == null || map.isEmpty()) {
            //perhaps the job is already finished?
            Map<String, String> accountingInfo = null;
            try {
                String output = runCommand(null, "qacct", "-j", job.getIdentifier());

                accountingInfo = QAcctOutputParser.getJobAccountingInfo(output);
            } catch (CommandFailedException e) {
                logger.debug("qacct command failed" ,e);
            }

            if (accountingInfo != null) {
                Integer exitCode = null;

                String exitCodeString = accountingInfo.get("exit_status");

                try {
                    if (exitCodeString != null) {
                        exitCode = Integer.parseInt(exitCodeString);
                    }
                } catch (NumberFormatException e) {
                    throw new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "cannot parse exit code of job "
                            + job.getIdentifier() + " from string " + exitCodeString, e);

                }

                clearJobSeen(job.getIdentifier());
                return new JobStatusImplementation(job, "done", exitCode, null, false, true, accountingInfo);
            } else if (haveRecentlySeen(job.getIdentifier())) {
                return new JobStatusImplementation(job, "pending", null, null, false, true, accountingInfo);
            } else {
                Exception exception =
                        new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "Job " + job.getIdentifier()
                                + " not found on server");
                return new JobStatusImplementation(job, null, null, exception, false, false, null);
            }
        }

        String state = map.get("state");

        if (state == null || state.length() == 0) {
            Exception exception =
                    new OctopusIOException(GridengineAdaptor.ADAPTOR_NAME, "State for job " + job.getIdentifier()
                            + " not found on server");
            return new JobStatusImplementation(job, null, null, exception, false, false, map);
        }

        markJobSeen(job.getIdentifier());
        return new JobStatusImplementation(job, state, null, null, state.equals("running"), false, map);
    }

    public JobStatus[] getJobStatuses(Job[] in) throws OctopusIOException, OctopusException {
        JobStatus[] result = new JobStatus[in.length];
        
        for (int i = 0; i < result.length; i++) {
            if (in[i] == null) {
                result[i] = null;
            } else {
                result[i] = getJobStatus(in[i]);
            }
        }
        return result;
    }


}