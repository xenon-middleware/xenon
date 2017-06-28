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
package nl.esciencecenter.xenon.adaptors.file;

import nl.esciencecenter.xenon.files.CopyDescription;

/**
 * CopyInfo contains all necessary information needed for asynchronous copy operations.
 * 
 * @version 1.0
 * @since 1.0
 */
public class CopyInfo {

	private final CopyDescription description;
	private final CopyHandleImplementation copy;
    
    private Exception exception;
    private boolean cancel = false;
    private long bytesToCopy = -1;
    private long bytesCopied = 0;

    public CopyInfo(CopyDescription description, CopyHandleImplementation copy) {
        super();
        this.description = description;
        this.copy = copy;
    }

    public CopyHandleImplementation getCopy() {
        return copy;
    }
    
    public CopyDescription getDescription() {
        return description;
    }
    
    public boolean hasID(String copyID) { 
        return copy.hasID(copyID);
    }
    
    public String getUniqueID() { 
        return copy.getUniqueID();
    }    
    
    public synchronized Exception getException() {
        return exception;
    }

    public synchronized void setException(Exception e) {
        exception = e;
    }

    public synchronized void cancel() {
        this.cancel = true;
    }

    public synchronized boolean isCancelled() {
        return cancel;
    }

    public synchronized void setBytesToCopy(long bytesToCopy) {
        this.bytesToCopy = bytesToCopy;
    }

    public synchronized long getBytesToCopy() {
        return bytesToCopy;
    }

    public synchronized void setBytesCopied(long bytesCopied) {
        this.bytesCopied = bytesCopied;
    }

    public synchronized long getBytesCopied() {
        return bytesCopied;
    }

    @Override
    public String toString() {
        return "CopyInfo [ID=" + copy.getUniqueID() + ", description=" + description  
                + " bytesToCopy=" + getBytesToCopy() + ", bytesCopied=" + getBytesCopied() + ", isCancelled=" + isCancelled() + "]";
    }
    
}