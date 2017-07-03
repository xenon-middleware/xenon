package nl.esciencecenter.xenon.adaptors.schedulers.ssh;

import static nl.esciencecenter.xenon.adaptors.schedulers.ssh.SshSchedulerAdaptor.ADAPTOR_NAME;

import org.apache.sshd.client.session.ClientSession;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.InteractiveProcess;
import nl.esciencecenter.xenon.adaptors.schedulers.InteractiveProcessFactory;
import nl.esciencecenter.xenon.adaptors.schedulers.JobImplementation;
import nl.esciencecenter.xenon.adaptors.schedulers.SchedulerClosedException;

public class SshInteractiveProcessFactory implements InteractiveProcessFactory {
	
	private final ClientSession session;
	
	protected SshInteractiveProcessFactory(ClientSession session) { 
		this.session = session;
	}
	
	@Override
    public InteractiveProcess createInteractiveProcess(JobImplementation job) throws XenonException {
		
		if (session.isClosed()) { 
			throw new SchedulerClosedException(ADAPTOR_NAME, "Scheduler is closed");
		}
		
     	return new SshInteractiveProcess(session, job);
    }

	@Override
	public void close() throws XenonException {
		
		if (session.isClosed()) { 
			throw new SchedulerClosedException(ADAPTOR_NAME, "Scheduler already closed");
		}
		
		try { 
			session.close();
		} catch (Exception e) {
			throw new XenonException(ADAPTOR_NAME, "Scheduler failed to close", e);
		}
	}

	@Override
	public boolean isOpen() throws XenonException {
		return session.isOpen();
	}
}
