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
package nl.esciencecenter.xenon.adaptors.schedulers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.Streams;

public class MockInteractiveProcess implements InteractiveProcess {

	JobDescription job;
	String jobID;
	
	long deadline;
	long killDeadline;
	long killDelay;
	
	Streams streams;
	
	boolean destroyed = false;
	
	public MockInteractiveProcess(JobDescription job, String jobID, long delay, long killDelay) { 
		this.job = job;
		this.jobID = jobID;
	
		ByteArrayInputStream stdout = new ByteArrayInputStream("Hello World\n".getBytes());
		ByteArrayInputStream stderr = new ByteArrayInputStream(new byte[0]);
		
		ByteArrayOutputStream stdin = new ByteArrayOutputStream();
		
		this.streams = new StreamsImplementation(jobID, stdout, stdin, stderr);
	
		this.deadline = System.currentTimeMillis() + delay;
		this.killDelay = killDelay;
	}
	
	@Override
	public synchronized boolean isDone() {
		
		if (System.currentTimeMillis() >= deadline) { 
			return true;
		}
		
		if (destroyed && System.currentTimeMillis() >= killDeadline) { 
			return true;
		}
		
		return false;
	}

	@Override
	public synchronized int getExitStatus() {
		if (destroyed) { 
			return 1;
		}
		
		if (isDone()) {
			return 0;
		}

		return -1;
	}

	@Override
	public synchronized void destroy() {
		if (destroyed || isDone()) { 
			return;
		}
		
		destroyed = true;
		killDeadline = System.currentTimeMillis() + killDelay;
	}

	@Override
	public Streams getStreams() {
		return streams;
	}

}
