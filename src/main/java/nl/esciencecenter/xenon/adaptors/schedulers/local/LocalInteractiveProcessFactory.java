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
package nl.esciencecenter.xenon.adaptors.schedulers.local;

import static nl.esciencecenter.xenon.adaptors.schedulers.local.LocalSchedulerAdaptor.ADAPTOR_NAME;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.InteractiveProcess;
import nl.esciencecenter.xenon.adaptors.schedulers.InteractiveProcessFactory;
import nl.esciencecenter.xenon.adaptors.schedulers.SchedulerClosedException;
import nl.esciencecenter.xenon.schedulers.JobDescription;

public class LocalInteractiveProcessFactory implements InteractiveProcessFactory {

	private boolean open = true;

	@Override
	public synchronized InteractiveProcess createInteractiveProcess(JobDescription description, String jobIdentifier) throws XenonException {
		if (!open) { 
			throw new SchedulerClosedException(ADAPTOR_NAME, "Scheduler is closed");
		}
		return new LocalInteractiveProcess(description, jobIdentifier);
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


