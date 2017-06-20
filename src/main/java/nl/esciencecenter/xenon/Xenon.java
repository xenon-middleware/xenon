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
package nl.esciencecenter.xenon;

import nl.esciencecenter.xenon.engine.files.FilesEngine;
import nl.esciencecenter.xenon.engine.jobs.JobsEngine;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.jobs.Jobs;

/**
 * XenonFactory is used to create and end Xenon instances. Make sure to always 
 * end instances when you no longer need them, otherwise they remain allocated.
 * 
 * @version 1.0
 * @since 1.0
 */
public final class Xenon {

	/** The local adaptor is a special case, therefore we publish its name here. */
	public static final String LOCAL_FILE_ADAPTOR_NAME = "file";

	/** All our own properties start with this prefix. */
	public static final String PREFIX = "xenon.";

	/** All our own adaptor properties start with this prefix. */
	public static final String ADAPTORS_PREFIX = PREFIX + "adaptors.";

	private static FilesEngine files;
	private static JobsEngine jobs;
	
    public static synchronized Files files() throws XenonException { 
    	
    	if (files == null) { 
    		files = new FilesEngine();
    	}
    	
    	return files;
    }
    
    public static synchronized Jobs jobs() throws XenonException { 

    	if (jobs == null) { 
    		jobs = new JobsEngine();
    	}
    	
    	return jobs;
    }
    
    
    /**
     * Return the description of the properties that can be set a creation time of 
     * a Xenon instance. These properties may be passed when invoking 
     * <code>newXenon</code>.
     * 
     * Note that the set of property descriptions returned here will depend on the 
     * set of scheme adaptors Xenon has available.
     * 
     * @return a XenonPropertyDescription describing the properties.
     * 
     * @throws XenonException
     *             If the XenonPropertyDescription could not be created.
     */
    public static XenonPropertyDescription [] getSupportedProperties() throws XenonException {
        ///return XenonEngine.getSupportedProperties();
    	return null;
    }
    
   
   
    /**
     * End all Xenon instances created by this factory.
     * 
     * All exceptions thrown during endAll are ignored.
     */
    public static synchronized void endAll() {
    	if (jobs != null) { 
    		jobs.end();
    	}
    	
    	if (files != null) { 
    		files.end();
    	}
    }
}
