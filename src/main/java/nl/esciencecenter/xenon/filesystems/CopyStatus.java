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
package nl.esciencecenter.xenon.filesystems;

/**
 * CopyStatus contains status information for a specific copy operation.
 */
public class CopyStatus {

	private final CopyHandle copy;
	private final String state;
	private final Exception exception;

	private final long bytesToCopy;
	private final long bytesCopied;

	public CopyStatus(CopyHandle copy, String state, long bytesToCopy, long bytesCopied, Exception exception) {
		super();
		this.copy = copy;
		this.state = state;
		this.bytesToCopy = bytesToCopy;
		this.bytesCopied = bytesCopied;
		this.exception = exception;
		
		System.out.println("STATUS " +state + " " +bytesToCopy + " " + bytesCopied + " " + exception);
		
	}

	/**
	 * Get the Copy for which this CopyStatus was created.
	 * 
	 * @return the Copy.
	 */
	public CopyHandle getCopy() {
		return copy;
	}

	/**
	 * Get the state of the Copy operation.
	 * 
	 * @return the state of the Copy operation.
	 */
	public String getState() {
		return state;
	}

	/**
	 * Get the exception produced by the Copy or while retrieving the status.
	 * 
	 * @return the exception.
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * Is the Copy still running?
	 * 
	 * @return if the Copy is running.
	 */
	public boolean isRunning() {
		return "RUNNING".equals(state);
	}

	/**
	 * Is the Copy done?
	 * 
	 * @return if the Copy is done.
	 */
	public boolean isDone() {
		return "DONE".equals(state) || "FAILED".equals(state);
	}

	/**
	 * Has the Copy or status retrieval produced a exception ?
	 * 
	 * @return if the Copy or status retrieval produced a exception.
	 */
	public boolean hasException() {
		return exception != null;
	}

	/**
	 * Get the number of bytes that need to be copied for the entire copy operation.
	 * 
	 * @return the number of bytes that need to be copied.
	 */
	public long bytesToCopy() {
		return bytesToCopy;
	}

	/**
	 * Get the number of bytes that have been copied.
	 * 
	 * @return the number of bytes that have been copied.
	 */
	public long bytesCopied() {
		return bytesCopied;
	}

	@Override
	public String toString() {
		return "CopyStatus [copy=" + copy + ", state=" + state + ", exception=" + exception + 
				", bytesToCopy=" + bytesToCopy + ", bytesCopied=" + bytesCopied + "]";
	}
}
