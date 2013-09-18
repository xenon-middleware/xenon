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
package nl.esciencecenter.cobalt.engine.jobs;

import java.util.Arrays;
import java.util.Map;

import nl.esciencecenter.cobalt.credentials.Credential;
import nl.esciencecenter.cobalt.engine.CobaltProperties;
import nl.esciencecenter.cobalt.jobs.Scheduler;

public class SchedulerImplementation implements Scheduler {

    private final String adaptorName;
    private final String uniqueID;
    private final String scheme;
    private final String location;
    
    private final CobaltProperties properties;
    private final String[] queueNames;
    private final Credential credential;

    private final boolean isOnline;
    private final boolean supportsInteractive;
    private final boolean supportsBatch;

    public SchedulerImplementation(String adaptorName, String uniqueID, String scheme, String location, String[] queueNames, 
            Credential credential, CobaltProperties properties, boolean isOnline, boolean supportsInteractive, 
            boolean supportsBatch) {

        if (adaptorName == null) {
            throw new IllegalArgumentException("AdaptorName may not be null!");
        }

        if (uniqueID == null) {
            throw new IllegalArgumentException("uniqueID may not be null!");
        }

        this.adaptorName = adaptorName;
        this.uniqueID = uniqueID;
        this.scheme = scheme;
        this.location = location;
        this.credential = credential;
        this.isOnline = isOnline;
        this.supportsInteractive = supportsInteractive;
        this.supportsBatch = supportsBatch;

        if (properties == null) {
            this.properties = new CobaltProperties();
        } else {
            this.properties = properties;
        }

        if (queueNames == null) {
            this.queueNames = new String[0];
        } else {
            this.queueNames = Arrays.copyOf(queueNames, queueNames.length);
        }
    }

    public Credential getCredential() {
        return credential;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public String getLocation() {
        return location;
    }
    
    @Override
    public Map<String, String> getProperties() {
        return properties.toMap();
    }

    @Override
    public String getAdaptorName() {
        return adaptorName;
    }

    @Override
    public String[] getQueueNames() {
        return queueNames.clone();
    }

    @Override
    public boolean supportsInteractive() {
        return supportsInteractive;
    }

    @Override
    public boolean supportsBatch() {
        return supportsBatch;
    }

    @Override
    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public String toString() {
        return "SchedulerImplementation [uniqueID=" + uniqueID + ", adaptorName=" + adaptorName + ", scheme=" + scheme 
                + ", location=" + location + ", properties=" + properties + ", queueNames=" + Arrays.toString(queueNames) 
                + ", isOnline=" + isOnline + ", supportsInteractive=" + supportsInteractive + ", supportsBatch=" + supportsBatch 
                + "]";
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
