package nl.esciencecenter.xenon.schedulers;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;

public class TestScheduler extends Scheduler {

	protected TestScheduler(String uniqueID, String adaptor, String location, boolean isOnline, boolean supportsBatch,
			boolean supportsInteractive, XenonProperties properties) {
		super(uniqueID, adaptor, location, isOnline, supportsBatch, supportsInteractive, properties);
	}

	@Override
	public String[] getQueueNames() throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws XenonException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isOpen() throws XenonException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getDefaultQueueName() throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobHandle[] getJobs(String... queueNames) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueueStatus getQueueStatus(String queueName) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueueStatus[] getQueueStatuses(String... queueNames) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobHandle submitJob(JobDescription description) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobStatus getJobStatus(JobHandle job) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobStatus[] getJobStatuses(JobHandle... jobs) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Streams getStreams(JobHandle job) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobStatus cancelJob(JobHandle job) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobStatus waitUntilDone(JobHandle job, long timeout) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobStatus waitUntilRunning(JobHandle job, long timeout) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

}
