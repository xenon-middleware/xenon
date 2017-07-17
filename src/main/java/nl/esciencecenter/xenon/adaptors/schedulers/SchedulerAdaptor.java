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

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.schedulers.Scheduler;
import nl.esciencecenter.xenon.schedulers.SchedulerAdaptorDescription;

public abstract class SchedulerAdaptor {

	public static final String ADAPTORS_PREFIX = "xenon.adaptors.schedulers.";
	
	private static int currentID = 1;
	
	private final SchedulerAdaptorDescription adaptorDescription;
	
	protected SchedulerAdaptor(String name, String description, String [] locations, XenonPropertyDescription [] properties, 
			boolean isOnline, boolean supportsBatch, boolean supportsInteractive) {
		adaptorDescription = new SchedulerAdaptorDescription(name, description, locations, properties, 
				isOnline, supportsBatch, supportsInteractive);
	}
	
    protected synchronized String getNewUniqueID() {
        String res = adaptorDescription.getName() + "." + currentID;
        currentID++;
        return res;
    }

	public String getName() { 
		return adaptorDescription.getName();
	}
	
	public SchedulerAdaptorDescription getAdaptorDescription() { 
		return adaptorDescription;
	}
	   
	public abstract Scheduler createScheduler(String location, Credential credential, Map<String,String> properties) throws XenonException;
	
}
