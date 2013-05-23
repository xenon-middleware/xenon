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

package nl.esciencecenter.octopus.adaptors.local;

import java.util.Arrays;

import nl.esciencecenter.octopus.engine.files.CopyImplementation;
import nl.esciencecenter.octopus.files.CopyOption;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
class CopyInfo {
    
    final CopyImplementation copy;
    final CopyOption mode;
    final boolean verify;
    
    Exception exception;
    boolean cancel = false;
    long bytesToCopy = -1;
    long bytesCopied = 0;
    
    public CopyInfo(CopyImplementation copy, CopyOption mode, boolean verify) {
        super();
        this.copy = copy;
        this.mode = mode;
        this.verify = verify;
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
                + copy.getTarget().getPath() + ", mode=" + mode  + ", verify=" + verify + ", bytesToCopy=" + getBytesToCopy()  
                + ", bytesCopied=" + getBytesCopied()  + ", isCancelled=" + isCancelled() + "]";
    }
}