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
import nl.esciencecenter.xenon.files.CopyHandle;

/**
 * 
 */
public class CopyHandleImplementation implements CopyHandle {

    private final String uniqueID;
    private final CopyDescription description;
    
    public CopyHandleImplementation(String uniqueID, CopyDescription description) {
        super();
        this.uniqueID = uniqueID;
        this.description = description;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public CopyDescription getDescription() { 
    	return description;
    }
    
    public boolean hasID(String copyID) { 
        return uniqueID.equals(copyID);
    }

    @Override
    public String toString() {
        return "CopyImplementation [uniqueID=" + uniqueID + ", description=" + description + "]";
    }
}
