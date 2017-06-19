package nl.esciencecenter.xenon.adaptors.job.ssh;

import static nl.esciencecenter.xenon.adaptors.job.ssh.SshProperties.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.file.sftp.SSHUtil;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.XenonProperties;
import nl.esciencecenter.xenon.engine.jobs.JobAdaptor;
import nl.esciencecenter.xenon.engine.jobs.JobsEngine;
import nl.esciencecenter.xenon.engine.jobs.SchedulerImplementation;
import nl.esciencecenter.xenon.engine.util.JobQueues;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.jobs.Job;
import nl.esciencecenter.xenon.jobs.JobDescription;
import nl.esciencecenter.xenon.jobs.JobStatus;
import nl.esciencecenter.xenon.jobs.QueueStatus;
import nl.esciencecenter.xenon.jobs.Scheduler;
import nl.esciencecenter.xenon.jobs.Streams;

public class SshJobs extends JobAdaptor {
	 
	private static final Logger LOGGER = LoggerFactory.getLogger(SshJobs.class);
    
	private static final AtomicInteger currentID = new AtomicInteger(1);

	private static String getNewUniqueID() {
		return "ssh" + currentID.getAndIncrement();
	}
	
	static class SchedulerInfo {

		private final JobQueues jobQueues;
		private ClientSession session;

        private SchedulerInfo(ClientSession session, JobQueues jobQueues) {
            this.session = session;
            this.jobQueues = jobQueues;
        }

        private JobQueues getJobQueues() {
            return jobQueues;
        }

        private void end() throws IOException {
            jobQueues.end();
            session.close();
        }
    }
	
	private final Map<String, SchedulerInfo> schedulers = new HashMap<>();
	 
	protected SshJobs(JobsEngine jobsEngine, Map<String, String> properties) throws XenonException {
		super(jobsEngine, ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_SCHEME, ADAPTOR_LOCATIONS, VALID_PROPERTIES,
                new XenonProperties(VALID_PROPERTIES, properties));
	}

	@Override
	public Scheduler newScheduler(String location, Credential credential, Map<String, String> properties)
			throws XenonException {
		
		  LOGGER.debug("newFileSystem scheme = SFTP location = {} credential = {} properties = {}", location, credential, properties);
	        
		  XenonProperties xp = new XenonProperties(VALID_PROPERTIES, properties);
	        
		  boolean loadKnownHosts = xp.getBooleanProperty(LOAD_STANDARD_KNOWN_HOSTS);
		  boolean loadSSHConfig = xp.getBooleanProperty(LOAD_SSH_CONFIG);
		  boolean useSSHAgent = xp.getBooleanProperty(AGENT);
		  boolean useAgentForwarding = xp.getBooleanProperty(AGENT_FORWARDING);
	        
		  SshClient client = SSHUtil.createSSHClient(loadKnownHosts, loadSSHConfig, useSSHAgent, useAgentForwarding);
	     
		  long timeout = xp.getLongProperty(TIMEOUT);
	        
		  ClientSession session = SSHUtil.connect(ADAPTOR_NAME, client, location, credential, timeout);
	        
//	        SftpFileSystem fs = (SftpFileSystem) session.createSftpFileSystem(); 
//	     
		  String uniqueID = getNewUniqueID();
		  
		  SchedulerImplementation scheduler = new SchedulerImplementation(ADAPTOR_NAME, uniqueID, location,
	                new String[] { "single", "multi", "unlimited" }, credential, xp, true, true, true);

		  SshInteractiveProcessFactory factory = new SshInteractiveProcessFactory(session);

		  // Create a file system that point to the same location as the scheduler.
		  Files files = getJobEngine().getXenonEngine().files();
		  FileSystem fs = files.newFileSystem("sftp", location, credential, properties);

		  long pollingDelay = xp.getLongProperty(POLLING_DELAY);
		  int multiQThreads = xp.getIntegerProperty(MULTIQ_MAX_CONCURRENT);

		  JobQueues jobQueues = new JobQueues(ADAPTOR_NAME, files, scheduler, fs.getEntryPath(), factory,
	                multiQThreads, pollingDelay);

		  synchronized (this) {
			  schedulers.put(uniqueID, new SchedulerInfo(session, jobQueues));
		  }

		  return scheduler;
	}

	@Override
	public void close(Scheduler scheduler) throws XenonException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isOpen(Scheduler scheduler) throws XenonException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getDefaultQueueName(Scheduler scheduler) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Job[] getJobs(Scheduler scheduler, String... queueNames) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueueStatus getQueueStatus(Scheduler scheduler, String queueName) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueueStatus[] getQueueStatuses(Scheduler scheduler, String... queueNames) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Job submitJob(Scheduler scheduler, JobDescription description) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobStatus getJobStatus(Job job) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobStatus[] getJobStatuses(Job... jobs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Streams getStreams(Job job) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobStatus cancelJob(Job job) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobStatus waitUntilDone(Job job, long timeout) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobStatus waitUntilRunning(Job job, long timeout) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getAdaptorSpecificInformation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void end() {
		// TODO Auto-generated method stub
		
	}

}
