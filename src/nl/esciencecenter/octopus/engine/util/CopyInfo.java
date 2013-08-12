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

package nl.esciencecenter.octopus.engine.util;

import  nl.esciencecenter.octopus.exceptions.UnsupportedOperationException;
import nl.esciencecenter.octopus.engine.files.CopyImplementation;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.CopyOption;

/**
 * CopyInfo contains all necessary information needed for asynchronous copy operations.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class CopyInfo {

    private final CopyImplementation copy;
    private final CopyOption mode;
    private final boolean verify;
    private final boolean async;
    
    private Exception exception;
    private boolean cancel = false;
    private long bytesToCopy = -1;
    private long bytesCopied = 0;

    public CopyInfo(CopyImplementation copy, CopyOption mode, boolean verify, boolean async) {
        super();
        this.copy = copy;
        this.mode = mode;
        this.verify = verify;
        this.async = async;
    }

    public CopyImplementation getCopy() {
        return copy;
    }

    public CopyOption getMode() {
        return mode;
    }

    public boolean mustVerify() {
        return verify;
    }

    public boolean isAsync() {
        return async;
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
        return "CopyInfo [ID=" + copy.getUniqueID() + ", source=" + copy.getSource().getPath() + ", target="
                + copy.getTarget().getPath() + ", mode=" + mode + ", verify=" + verify + ", bytesToCopy=" + getBytesToCopy()
                + ", bytesCopied=" + getBytesCopied() + ", isCancelled=" + isCancelled() + "]";
    }

    
    private static CopyOption checkMode(String adaptorName, CopyOption previous, CopyOption current) 
            throws UnsupportedOperationException { 
        
        if (previous != null && !previous.equals(current)) {
            throw new UnsupportedOperationException(adaptorName, "Conflicting copy options: " + previous + " and " + 
                    current);
        }

        return current;
    }
    
    /**
     * @param adaptorName
     * @param nextID
     * @param source
     * @param target
     * @param options
     * @return
     * @throws UnsupportedOperationException 
     */
    public static CopyInfo createCopyInfo(String adaptorName, String nextID, AbsolutePath source, AbsolutePath target,
            CopyOption ... options) throws UnsupportedOperationException {

        boolean async = false;
        boolean verify = false;
        
        CopyOption mode = null;
        
        for (CopyOption opt : options) {
            switch (opt) {
            case CREATE:
            case REPLACE:
            case APPEND:
            case RESUME:
            case IGNORE:
                mode = checkMode(adaptorName, mode, opt);
                break;
            case VERIFY:
                verify = true;
                break;
            case ASYNCHRONOUS:
                async = true;
                break;
            }
        }

        if (mode == null) {
            mode = CopyOption.CREATE;
        }

        if (verify && mode != CopyOption.RESUME) {
            throw new UnsupportedOperationException(adaptorName, "Conflicting copy options: " + mode + " and VERIFY");
        }

        CopyImplementation copy = new CopyImplementation(adaptorName, nextID, source, target);
        return new CopyInfo(copy, mode, verify, async);
    }
    
}