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

import java.util.Arrays;

/**
 * AdaptorStatus contains information on a specific adaptor.
 * 
 * @version 1.0
 * @since 1.0
 */
public class AdaptorDescription {
   
	private final String name;
	private final String description;
	private final String [] supportedLocations;
	private final XenonPropertyDescription [] supportedProperties;
	
	public AdaptorDescription(String name, String description, String[] supportedLocations, 
			XenonPropertyDescription[] supportedProperties) {
		super();
		this.name = name;
		this.description = description;
		this.supportedLocations = supportedLocations;
		this.supportedProperties = supportedProperties;
	}

	/**
     * Get the name of the adaptor.
     * 
     * @return the name of the adaptor.
     */
    public String getName() { 
    	return name;
    }

    /**
     * Get the description of the adaptor.
     * 
     * @return the description of the adaptor.
     */
    public String getDescription() { 
    	return description;
    }

    /**
     * Get the supported locations for this adaptor.
     * 
     * @return the locations supported by this adaptor.
     */
    public String[] getSupportedLocations() { 
    	return supportedLocations.clone();
    }
    
    /**
     * Returns an array containing all properties this adaptor supports.
     * 
     * @return an array containing all properties this adaptor supports.
     */
    public XenonPropertyDescription[] getSupportedProperties() { 
    	return supportedProperties.clone();
    }

    @Override
    public String toString() {
        return "AdaptorDescription [name=" + name + ", description=" + description + 
        		", supportedLocations=" + Arrays.toString(supportedLocations) +
        		", supportedProperties=" + Arrays.toString(supportedProperties) + "]";
    }
    
    /**
     * Returns a map containing adaptor specific status information.
     * 
     * @return a map containing adaptor specific status information.
     */
    //Map<String, String> getAdaptorSpecificInformation();
}