/*
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

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.XenonProperties;

public class MockDefaultScheduler extends Scheduler {

	public MockDefaultScheduler(String uniqueID, String adaptor, String location, XenonProperties properties) {
		super(uniqueID, adaptor, location, properties);
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
	public String[] getJobs(String... queueNames) throws XenonException {
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
	public String submitBatchJob(JobDescription description) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Streams submitInteractiveJob(JobDescription description) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public JobStatus getJobStatus(String job) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobStatus[] getJobStatuses(String... jobs) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobStatus cancelJob(String job) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobStatus waitUntilDone(String job, long timeout) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobStatus waitUntilRunning(String job, long timeout) throws XenonException {
		// TODO Auto-generated method stub
		return null;
	}

}
