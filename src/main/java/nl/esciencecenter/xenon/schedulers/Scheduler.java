/**
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import nl.esciencecenter.xenon.InvalidAdaptorException;
import nl.esciencecenter.xenon.InvalidCredentialException;
import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.UnknownPropertyException;
import nl.esciencecenter.xenon.InvalidPropertyException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.gridengine.GridEngineSchedulerAdaptor;
import nl.esciencecenter.xenon.adaptors.schedulers.local.LocalSchedulerAdaptor;
import nl.esciencecenter.xenon.adaptors.schedulers.slurm.SlurmSchedulerAdaptor;
import nl.esciencecenter.xenon.adaptors.schedulers.ssh.SshSchedulerAdaptor;
import nl.esciencecenter.xenon.adaptors.schedulers.torque.TorqueSchedulerAdaptor;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.schedulers.SchedulerAdaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;

/**
 * Scheduler represents a (possibly remote) scheduler that can be used to submit jobs and retrieve queue information.
 * 
 * @version 1.0
 * @since 1.0
 */
public abstract class Scheduler {

	/** The name of this component, for use in exceptions */
	private static final String COMPONENT_NAME = "Scheduler";

	private static final HashMap<String, SchedulerAdaptor> adaptors = new LinkedHashMap<>();

	static { 
		/** Load all supported file adaptors */
		addAdaptor(new LocalSchedulerAdaptor());
		addAdaptor(new SshSchedulerAdaptor());
		addAdaptor(new GridEngineSchedulerAdaptor());
		addAdaptor(new SlurmSchedulerAdaptor());
		addAdaptor(new TorqueSchedulerAdaptor());
	}

	private static void addAdaptor(SchedulerAdaptor adaptor) { 
		adaptors.put(adaptor.getName(), adaptor);
	}

	private static SchedulerAdaptor getAdaptorByName(String adaptorName) throws XenonException {

		if (adaptorName == null || adaptorName.trim().isEmpty()) {
			throw new InvalidAdaptorException(COMPONENT_NAME, "Adaptor name may not be null or empty");
		}

		SchedulerAdaptor adaptor = adaptors.get(adaptorName);

		if (adaptor == null) {
			throw new InvalidAdaptorException(COMPONENT_NAME, "File adaptor not found " + adaptor);
		}

		return adaptor;
	}

	public static String [] getAdaptorNames() {
		ArrayList<String> tmp = new ArrayList<>();
		
		for (SchedulerAdaptor a : adaptors.values()) { 
			tmp.add(a.getName());
		}
		
		return tmp.toArray(new String[tmp.size()]);
	}

	public static SchedulerAdaptorDescription getAdaptorDescription(String adaptorName) throws XenonException {
		return getAdaptorByName(adaptorName).getAdaptorDescription();
	}

	public static SchedulerAdaptorDescription [] getAdaptorDescriptions() throws XenonException {
		String [] names = getAdaptorNames();
		
		SchedulerAdaptorDescription[] result = new SchedulerAdaptorDescription[names.length];
		
		for (int i=0;i<names.length;i++) { 
			result[i] = getAdaptorDescription(names[i]);
		}
		
		return result;
	}
	
	/**
     * Create a new Scheduler that represents a (possibly remote) job 
     * scheduler at the <code>location</code> using <code>credentials</code> 
     * to get access. Make sure to always close {@code Scheduler} instances 
     * by calling {@code Scheduler.close()} when you no longer need them, 
     * otherwise their associated resources may remain allocated.
     * 
     * @param adaptor
     *            the adaptor used to access the Scheduler.
     * @param location
     *            the location of the Scheduler.
     * @param credential
     *            the Credentials to use to get access to the Scheduler.
     * @param properties
     *            optional properties to configure the Scheduler when it is created.
     * 
     * @return the new Scheduler.
     * 
     * @throws UnknownPropertyException
     *             If a unknown property was provided.
     * @throws InvalidPropertyException
     *             If a known property was provided with an invalid value.
     * @throws InvalidLocationException
     *             If the location was invalid.
     * @throws InvalidCredentialException
     *             If the credential is invalid to access the location.
     * 
     * @throws XenonException
     *             If the creation of the Scheduler failed.
     */
	public static Scheduler create(String adaptor, String location, Credential credential, Map<String, String> properties) 
            throws XenonException {
		return getAdaptorByName(adaptor).createScheduler(location, credential, properties);
	}
	
	public static Scheduler create(String adaptor, String location, Credential credential) throws XenonException {
		return create(adaptor, location, credential, new HashMap<String, String>(0));
	}
	
	public static Scheduler create(String adaptor, String location) throws XenonException {
		return create(adaptor, location, new DefaultCredential());
	}
	
	public static Scheduler create(String adaptor) throws XenonException {
		return create(adaptor, null);
	}
	
	private final String uniqueID;
	private final String adaptor;
	private final String location;
	protected final XenonProperties properties;

	private final boolean isOnline;
	private final boolean supportsInteractive;
	private final boolean supportsBatch;
	
	protected Scheduler(String uniqueID, String adaptor, String location, 
			boolean isOnline, boolean supportsBatch, boolean supportsInteractive, 
			XenonProperties properties) {

		if (uniqueID == null) {
			throw new IllegalArgumentException("Identifier may not be null!");
		}

		if (adaptor == null) {
			throw new IllegalArgumentException("Adaptor may not be null!");
		}
	
		if (location == null) {
			throw new IllegalArgumentException("Location may not be null!");
		}

		this.uniqueID = uniqueID;
		this.adaptor = adaptor;
		this.location = location;

		this.isOnline = isOnline;
		this.supportsBatch = supportsBatch;
		this.supportsInteractive = supportsInteractive;
		this.properties = properties;
	}

	/**
	 * Get the name of the adaptor that created this Scheduler.
	 * 
	 * @return the name of the adaptor.
	 */
	public String getAdaptorName() { 
		return adaptor;
	}

	/**
	 * Get the location that this Scheduler is connected to.
	 * 
	 * @return the location this Scheduler is connected to.
	 */
	public String getLocation() { 
		return location;
	}

	/**
     * Does this Scheduler supports the submission of interactive jobs ?
     * 
     * For interactive jobs the standard streams of the job must be handled by the submitting process. Failing to do so may cause
     * the job to hang indefinitely.
     * 
     * @return if this scheduler supports the submission of interactive jobs ?
     */
	public boolean supportsInteractive() { 
		return supportsInteractive;
	}

    /**
     * Does this Scheduler support the submission of batch jobs ?
     * 
     * For batch jobs the standard streams of the jobs are redirected from / to files.
     * 
     * @return if this scheduler supports the submission of batch jobs ?
     */
    public boolean supportsBatch() { 
    	return supportsBatch;
    }

    /**
     * Is this an online scheduler ?
     * 
     * Online schedulers need to remain active for their jobs to run. Ending an online scheduler will kill all jobs that were
     * submitted to it.
     * 
     * Offline schedulers do not need to remains active for their jobs to run. A submitted job will typically be handed over to
     * some external server that will manage the job for the rest of its lifetime.
     * 
     * Online schedulers typically support both interactive jobs (where the user controls the standard streams) and batch jobs
     * (where the standard streams are redirected to/from files).
     * 
     * Since it is impossible to continue an interactive jobs when a scheduler ends, interactive jobs will always be killed,
     * even in an offline scheduler.
     * 
     * @return if this scheduler is online.
     */
    public boolean isOnline() { 
    	return isOnline;
    }

	/**
	 * Get the properties used to create this Scheduler.
	 * 
	 * @return the properties used to create this Scheduler.
	 */
	public Map<String, String> getProperties() { 
		return properties.toMap();
	}
	
    /**
     * Get the queue names supported by this Scheduler.
     * 
     * @return the queue names supported by this Scheduler.
     * 
     * @throws XenonException
     * 		       If an I/O error occurred.
     */
    public abstract String[] getQueueNames() throws XenonException;
    
    /**
     * Close this Scheduler.
     * 
     * @throws XenonException
     *             If the Scheduler failed to close.
     */
    public abstract void close() throws XenonException;

    /**
     * Test if the connection of this Scheduler is open.
     * 
     * @throws XenonException
     *             If an I/O error occurred.
     * @return  
     *          <code>true</code> if the connection of this Scheduler is still open, <code>false</code> otherwise.             
     *             
     */
    public abstract boolean isOpen() throws XenonException;

    /**
     * Get the name of the default queue.
     * 
     * @return the name of the default queue for this scheduler, or <code>null</code> if no default queue is available.
     * 
     * @throws XenonException
     *             If an I/O error occurred.
     */
    public abstract String getDefaultQueueName() throws XenonException;

    /**
     * Get all job identifier of jobs currently in (one ore more) queues. 
     * 
     * If no queue names are specified, the job identifiers for all queues are returned.
     * 
     * Note that job identifiers of jobs submitted by other users or other schedulers may also be returned.
     * 
     * @param queueNames
     *            the names of the queues.
     * 
     * @return an array containing the resulting job identifiers .
     * 
     * @throws NoSuchQueueException
     *             If the queue does not exist in the scheduler.
     * @throws XenonException
     *             If the Scheduler failed to get jobs.
     */
    public abstract String[] getJobs(String... queueNames) throws XenonException;

    /**
     * Get the status of the <code>queue</code>.
     * 
     * @param queueName
     *            the name of the queue.
     * 
     * @return the resulting QueueStatus.
     * 
     * @throws NoSuchQueueException
     *             If the queue does not exist in the scheduler.
     * @throws XenonException
     *             If the Scheduler failed to get its status.
     */
    public abstract QueueStatus getQueueStatus(String queueName) throws XenonException;

    /**
     * Get the status of all <code>queues</code>. 
     * 
     * Note that this method will only throw an exception when this exception will influence all status requests. For example, if
     * the scheduler is no longer connected.
     * 
     * Exceptions that only refer to a single queue are returned in the QueueStatus returned for that queue.
     * 
     * @param queueNames
     *            the names of the queues.
     * 
     * @return an array containing the resulting QueueStatus.
     * 
     * @throws XenonException
     *             If the Scheduler failed to get the statusses.
     */
    public abstract QueueStatus[] getQueueStatuses(String... queueNames) throws XenonException;

    /**
     * Submit a batch job.
     * 
     * @param description
     *            the description of the batch job to submit.
     * 
     * @return the job identifier representing the running job.
     * 
     * @throws IncompleteJobDescriptionException
     *             If the description did not contain the required information.
     * @throws InvalidJobDescriptionException
     *             If the description contains illegal or conflicting values.
     * @throws UnsupportedJobDescriptionException
     *             If the description is not legal for this scheduler.
     * @throws XenonException
     *             If the Scheduler failed to get submit the job.
     */
    public abstract String submitBatchJob(JobDescription description) throws XenonException;

    /**
     * Submit an interactive job (optional operation).
     * 
     * @param description
     *            the description of the interactive job to submit.
     * 
     * @return a <code>Streams</code> object containing the job identifier and the standard streams of a job.
     * 
     * @throws IncompleteJobDescriptionException
     *             If the description did not contain the required information.
     * @throws InvalidJobDescriptionException
     *             If the description contains illegal or conflicting values.
     * @throws UnsupportedJobDescriptionException
     *             If the description is not legal for this scheduler.
     * @throws XenonException
     *             If the Scheduler failed to get submit the job.
     */
    public abstract Streams submitInteractiveJob(JobDescription description) throws XenonException;

    /**
     * Get the status of a Job.
     * 
     * @param jobIdentifier
     *            the job identifier of the job to get the status for.
     * 
     * @return the status of the Job.
     * 
     * @throws NoSuchJobException
     *             If the job is not known.
     * @throws XenonException
     *             If the status of the job could not be retrieved.
     */
    public abstract JobStatus getJobStatus(String jobIdentifier) throws XenonException;

    /**
     * Get the status of all specified <code>jobs</code>.
     * <p>
     * The array of <code>JobStatus</code> contains one entry for each of the <code>jobs</code>. The order of the elements in the
     * returned <code>JobStatus</code> array corresponds to the order in which the <code>jobs</code> are passed as parameters. If
     * a <code>job</code> is <code>null</code>, the corresponding entry in the <code>JobStatus</code> array will also be
     * <code>null</code>. If the retrieval of the <code>JobStatus</code> fails for a job, the exception will be stored in the
     * corresponding <code>JobsStatus</code> entry.
     * </p>
     * @param jobIdentifiers
     *            the job identifiers for which to retrieve the status.
     * 
     * @return an array of the resulting JobStatusses.
     * 
     * @throws XenonException
     *             If an I/O error occurred
     */
    public abstract JobStatus[] getJobStatuses(String... jobIdentifiers) throws XenonException;
    
    /**
     * Cancel a job.
     * <p>
     * A status is returned that indicates the state of the job after the cancel. If the job was already done it cannot be 
     * cancelled.
     * </p>
     * <p>
     * A {@link JobStatus} is returned that can be used to determine the state of the job after cancelJob returns. Note that it 
     * may take some time before the job has actually terminated. The {@link #waitUntilDone(String, long) waitUntilDone} method can 
     * be used to wait until the job is terminated.
     * </p>
     * @param jobIdentifier
     *            the identifier of job to kill.
     * @return the status of the Job.
     * 
     * @throws NoSuchJobException
     *             If the job is not known.
     * @throws XenonException
     *             If the status of the job could not be retrieved.
     */
    public abstract JobStatus cancelJob(String jobIdentifier) throws XenonException;

    /**
     * Wait until a job is done or until a timeout expires.
     * <p>
     * This method will wait until a job is done (either gracefully or by being killed or producing an error), or until the 
     * timeout expires, whichever comes first. If the timeout expires, the job will continue to run.
     * </p>
     * <p>
     * The timeout is in milliseconds and must be &gt;= 0. When timeout is 0, it will be ignored and this method will wait until
     * the jobs is done.  
     * </p>
     * <p>
     * A JobStatus is returned that can be used to determine why the call returned.
     * </p>
     * @param jobIdentifier
     *            the identifier of the to wait for.
     * @param timeout
     *            the maximum time to wait for the job in milliseconds.
     * @return the status of the Job.
     * 
     * @throws IllegalArgumentException 
     *             If the value of timeout is negative
     * @throws NoSuchJobException
     *             If the job is not known.
     * @throws XenonException
     *             If the status of the job could not be retrieved.
     */
    public abstract JobStatus waitUntilDone(String jobIdentifier, long timeout) throws XenonException;

    /**
     * Wait until a job starts running, or until a timeout expires.
     * <p>
     * This method will return as soon as the job is no longer waiting in the queue, or when the timeout expires, whichever comes
     * first. If the job is no longer waiting in the queue, it may be running, but it may also be killed, finished or have produced 
     * an error. If the timeout expires, the job will continue to be queued normally.
     * </p>
     * <p>
     * The timeout is in milliseconds and must be &gt;= 0. When timeout is 0, it will be ignored and this method will wait until
     * the job is no longer queued.  
     * </p>
     * <p>
     * A JobStatus is returned that can be used to determine why the call returned.
     * </p>
     * @param jobIdentifier
     *            the identifier of the to wait for.
     * @param timeout
     *            the maximum time to wait in milliseconds.
     * @return the status of the Job.
     * 
     * @throws IllegalArgumentException 
     *             If the value of timeout is negative
     * @throws NoSuchJobException
     *             If the job is not known.
     * @throws XenonException
     *             If the status of the job could not be retrieved.
     */
    public abstract JobStatus waitUntilRunning(String jobIdentifier, long timeout) throws XenonException;

    
	@Override
	public int hashCode() {
		return uniqueID.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
	
		return uniqueID.equals(((Scheduler) obj).uniqueID);		
	}
    
}
