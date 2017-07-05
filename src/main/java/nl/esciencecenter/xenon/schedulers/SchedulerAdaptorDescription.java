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

package nl.esciencecenter.xenon.schedulers;

import nl.esciencecenter.xenon.AdaptorDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription;

/**
 *
 */
public class SchedulerAdaptorDescription extends AdaptorDescription {

	private final boolean isOnline;
	private final boolean supportsBatch;
	private final boolean supportsInteractive;
	
    public SchedulerAdaptorDescription(String name, String description, String[] supportedLocations,
			XenonPropertyDescription[] supportedProperties, boolean isOnline, boolean supportsBatch, boolean supportsInteractive) {
		super(name, description, supportedLocations, supportedProperties);
		this.isOnline = isOnline;
		this.supportsBatch = supportsBatch;
		this.supportsInteractive = supportsInteractive;
    }

	/**
     * TODO: rename isOnline to something like separateServer, supportsDetach, etc.
     * 
     * @return
     * 		if this scheduler is online
     */
    public boolean isOnline() { 
    	return isOnline;
    }
    
    /**
     * Does this scheduler support batch jobs ?
     * 
     * @return
     * 		if this scheduler supports batch jobs
     */
    public boolean supportsBatch() { 
    	return supportsBatch;
    }
    
    /**
     * Does this scheduler support interactive jobs ?
     * 
     * @return
     * 		if this scheduler supports interactive jobs
     */
    public boolean supportsInteractive() { 
    	return supportsInteractive;
    }
    
}
