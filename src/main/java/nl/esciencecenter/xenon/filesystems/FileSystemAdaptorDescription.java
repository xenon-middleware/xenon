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

import nl.esciencecenter.xenon.AdaptorDescription;

/**
 * @version 1.0
 * @since 1.0
 *
 */
public interface FileSystemAdaptorDescription extends AdaptorDescription {

//	private final boolean supportsThirdPartyCopy;
//	private final boolean readSymbolicLinks;
//	private final boolean createSymbolicLinks;
//	
//    public FileSystemAdaptorDescription(String name, String description, String[] supportedLocations,
//			XenonPropertyDescription[] supportedProperties, boolean supportsThirdPartyCopy, boolean readSymbolicLinks, boolean createSymbolicLinks) {
//		super(name, description, supportedLocations, supportedProperties);
//		this.supportsThirdPartyCopy = supportsThirdPartyCopy;
//		this.readSymbolicLinks = readSymbolicLinks;
//		this.createSymbolicLinks = createSymbolicLinks;
//	}

	/**
     * Does this adaptor support third party copy ?
     * 
     * In third party copy, a file is copied between two remote locations, without passing through the local machine first.  
     * 
     * @return
     *          if this adaptor supports third party copy.
     */
    boolean supportsThirdPartyCopy();

	/**
     * Can this adaptor read symbolic links ?
     * 
     * @return
     *          if this adaptor can read symbolic links.
     */
    boolean canReadSymboliclinks();

	/**
     * Can this adaptor create symbolic links ?
     * 
     * In third party copy, a file is copied between two remote locations, without passing through the local machine first.  
     * 
     * @return
     *          if this adaptor can create symbolic links.
     */
    boolean canCreateSymboliclinks();
}
