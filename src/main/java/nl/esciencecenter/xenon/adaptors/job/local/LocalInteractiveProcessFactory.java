package nl.esciencecenter.xenon.adaptors.job.local;

import static nl.esciencecenter.xenon.adaptors.job.local.LocalSchedulerAdaptor.ADAPTOR_NAME;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.job.InteractiveProcess;
import nl.esciencecenter.xenon.adaptors.job.InteractiveProcessFactory;
import nl.esciencecenter.xenon.adaptors.job.JobImplementation;
import nl.esciencecenter.xenon.adaptors.job.SchedulerClosedException;

public class LocalInteractiveProcessFactory implements InteractiveProcessFactory {

	private boolean open = true;

	@Override
	public synchronized InteractiveProcess createInteractiveProcess(JobImplementation job) throws XenonException {
		if (!open) { 
			throw new SchedulerClosedException(ADAPTOR_NAME, "Scheduler is closed");
		}
		return new LocalInteractiveProcess(job);
	}

	@Override
	public synchronized void close() throws XenonException {
		if (!open) { 
			throw new SchedulerClosedException(ADAPTOR_NAME, "Scheduler already closed");
		}
		open = false;
	}

	@Override
	public synchronized boolean isOpen() throws XenonException {
		return open;
	}
}


