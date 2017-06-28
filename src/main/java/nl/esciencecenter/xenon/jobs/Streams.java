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
package nl.esciencecenter.xenon.jobs;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Streams is a container for the standard input, output and error streams of a {@link Job}.
 * 
 * Note that these standard streams are only available for interactive jobs. 
 * 
 * @version 1.0
 * @since 1.0
 */
public class Streams {

	private final Job job;
	private final InputStream stdout;
	private final InputStream stderr;
	private final OutputStream stdin;

	/**
	 * Create a Streams containing the job and its standard streams.
	 * 
	 * @param job
	 *            the job.
	 * @param stdout
	 *            the standard output stream.
	 * @param stdin
	 *            the standard input stream.
	 * @param stderr
	 *            the standard error stream.
	 */
	public Streams(Job job, InputStream stdout, OutputStream stdin, InputStream stderr) {
		this.job = job;
		this.stdout = stdout;
		this.stdin = stdin;
		this.stderr = stderr;
	}

	/**
	 * Get the job for which this Streams was created.
	 * 
	 * @return the Job.
	 */
	public Job getJob() {
		return job;
	}

	/**
	 * Returns the standard output stream of job.
	 * 
	 * @return the standard output stream of job.
	 */
	public InputStream getStdout() {
		return stdout;
	}

	/**
	 * Returns the standard error stream of job.
	 * 
	 * @return the standard error stream of job.
	 */
	public InputStream getStderr() {
		return stderr;
	}

	/**
	 * Returns the standard input stream of job.
	 * 
	 * @return the standard input stream of this job.
	 */
	public OutputStream getStdin() {
		return stdin;
	}
}
