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
package nl.esciencecenter.xenon.adaptors.filesystems.local;

import java.nio.file.Files;
import java.util.Map;

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.adaptors.XenonProperties;
import nl.esciencecenter.xenon.adaptors.filesystems.FileAdaptor;
import nl.esciencecenter.xenon.adaptors.shared.local.LocalUtil;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.Path;

/**
 * LocalFiles implements an Xenon <code>Files</code> adaptor for local file operations.
 * 
 * @see Files
 * 
 * @version 1.0
 * @since 1.0
 */
public class LocalFileAdaptor extends FileAdaptor {

	 /** Name of the local adaptor is defined in the engine. */
    public static final String ADAPTOR_NAME = "file";

    /** Local properties start with this prefix. */
    public static final String PREFIX = FileAdaptor.ADAPTORS_PREFIX + ADAPTOR_NAME + ".";
    
    /** Description of the adaptor */
    public static final String ADAPTOR_DESCRIPTION = "This is the local file adaptor that implements"
            + " file functionality for local access.";
    
    /** The locations supported by the adaptor */
    public static final String [] ADAPTOR_LOCATIONS = new String [] { "(null)", "(empty string)", "/" };
    
    /** The properties supported by this adaptor */
    public static final XenonPropertyDescription [] VALID_PROPERTIES = new XenonPropertyDescription[0];
    
    public LocalFileAdaptor() {
    	super(ADAPTOR_NAME, ADAPTOR_DESCRIPTION, ADAPTOR_LOCATIONS, VALID_PROPERTIES, false);
    }

	/** 
     * Check if a location string is valid for the local filesystem. 
     * 
     * The location should -only- contain a file system root, such as "/" or "C:". 
     * 
     * @param location
     *          the location to check.
     * @throws InvalidLocationException
     *          if the location is invalid.                   
     */
    private static void checkFileLocation(String location) throws InvalidLocationException {
        if (location == null || location.isEmpty() || LocalUtil.isLocalRoot(location)) {
            return;
        }

        throw new InvalidLocationException(ADAPTOR_NAME, "Location must only contain a file system root! (not " + location + ")");
    }
	
	@Override
	public FileSystem createFileSystem(String location, Credential credential, Map<String, String> properties) throws XenonException {
		
		checkFileLocation(location);
		
		if (location == null){ 
			location = "/";
		}
		
		LocalUtil.checkCredential(ADAPTOR_NAME, credential);

		XenonProperties xp = new XenonProperties(VALID_PROPERTIES, properties);

		String root = LocalUtil.getLocalRoot(location);
		Path relativePath = new Path(root).relativize(new Path(location));

		return new LocalFileSystem(getNewUniqueID(), location, relativePath, xp);
	}
    

}
