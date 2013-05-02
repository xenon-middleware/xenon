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
package nl.esciencecenter.octopus.engine.jobs;

import java.net.URI;
import java.util.Arrays;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.jobs.Scheduler;

public class SchedulerImplementation implements Scheduler {

    private final String adaptorName;
    private final String uniqueID;

    private final URI uri;
    private final OctopusProperties properties;
    private final String[] queueNames;
    private final Credential credential;

    private final boolean isOnline;
    private final boolean supportsInteractive;
    private final boolean supportsBatch;

    public SchedulerImplementation(String adaptorName, String uniqueID, URI uri, String[] queueNames, Credential credential,
            OctopusProperties properties, boolean isOnline, boolean supportsInteractive, boolean supportsBatch) {

        if (adaptorName == null) { 
            throw new IllegalArgumentException("AdaptorName may not be null!");
        }
        
        if (uniqueID == null) { 
            throw new IllegalArgumentException("uniqueID may not be null!");
        }

        this.adaptorName = adaptorName;
        this.uniqueID = uniqueID;
        this.uri = uri;
        this.queueNames = queueNames;
        this.properties = properties;
        this.credential = credential;
        this.isOnline = isOnline;
        this.supportsInteractive = supportsInteractive;
        this.supportsBatch = supportsBatch;
    }

    public Credential getCredential() {
        return credential;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public OctopusProperties getProperties() {
        return properties;
    }

    @Override
    public String getAdaptorName() {
        return adaptorName;
    }

    @Override
    public String[] getQueueNames() {
        return queueNames.clone();
    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.octopus.jobs.Scheduler#supportsInteractive()
     */
    @Override
    public boolean supportsInteractive() {
        return supportsInteractive;
    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.octopus.jobs.Scheduler#supportsBatch()
     */
    @Override
    public boolean supportsBatch() {
        return supportsBatch;
    }

    /* (non-Javadoc)
     * @see nl.esciencecenter.octopus.jobs.Scheduler#isOnline()
     */
    @Override
    public boolean isOnline() {
        return isOnline;
    }


    
    
    @Override
    public String toString() {
        return "SchedulerImplementation [uniqueID=" + uniqueID + ", adaptorName=" + adaptorName + ", uri=" + uri
                + ", properties=" + properties + ", queueNames=" + Arrays.toString(queueNames)
                + ", isOnline=" + isOnline + ", supportsInteractive=" + supportsInteractive + ", supportsBatch=" 
                + supportsBatch + "]";
                // + ", localStandardStreams=" + localStandardStreams + ", hasDetachedJobs=" + hasDetachedJobs + "]";
    }

    @Override
    public int hashCode() {
        int result = 31 + adaptorName.hashCode();
        return 31 * result + uniqueID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { 
            return true;
        }
        
        if (obj == null) { 
            return false;
        }
        
        if (getClass() != obj.getClass()) { 
            return false;
        }
        
        SchedulerImplementation other = (SchedulerImplementation) obj;
        return adaptorName.equals(other.adaptorName) && uniqueID.equals(other.uniqueID);
    }
}
