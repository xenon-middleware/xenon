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

import java.util.Arrays;

import nl.esciencecenter.xenon.AdaptorDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public class FileSystemAdaptorDescription extends AdaptorDescription {

	private final boolean supportsThirdPartyCopy;
	
    public FileSystemAdaptorDescription(String name, String description, String[] supportedLocations,
			XenonPropertyDescription[] supportedProperties, boolean supportsThirdPartyCopy) {
		super(name, description, supportedLocations, supportedProperties);
		this.supportsThirdPartyCopy = supportsThirdPartyCopy;
	}

	/**
     * Does this scheme support third party copy ?
     * 
     * In third party copy, a file is copied between two remote locations, without passing through the local machine first.  
     * 
     * @return
     *          if this scheme supports third party copy.
     */
    public boolean supportsThirdPartyCopy() { 
    	return supportsThirdPartyCopy;
    }
    
    @Override
    public String toString() {
        return "FileAdaptorDescription [name=" + getName() + ", description=" + getDescription() + 
        		", supportedLocations=" + Arrays.toString(getSupportedLocations()) +
        		", supportedProperties=" + Arrays.toString(getSupportedProperties()) +  
        		", supportsThirdPArtyCopy=" + supportsThirdPartyCopy + "]";
    }

	public boolean supportsSymboliclinks() {
    	return true;
	}
}
