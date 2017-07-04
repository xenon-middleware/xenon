package nl.esciencecenter.xenon.adaptors.schedulers.local;

import static nl.esciencecenter.xenon.adaptors.schedulers.local.LocalSchedulerAdaptor.ADAPTOR_NAME;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.InteractiveProcess;
import nl.esciencecenter.xenon.adaptors.schedulers.InteractiveProcessFactory;
import nl.esciencecenter.xenon.adaptors.schedulers.JobImplementation;
import nl.esciencecenter.xenon.adaptors.schedulers.SchedulerClosedException;

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


