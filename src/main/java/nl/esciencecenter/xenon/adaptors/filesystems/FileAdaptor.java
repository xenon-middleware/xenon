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
package nl.esciencecenter.xenon.adaptors.filesystems;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.adaptors.Adaptor;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.filesystems.FileSystem;
import nl.esciencecenter.xenon.filesystems.FileSystemAdaptorDescription;

public abstract class FileAdaptor extends Adaptor implements FileSystemAdaptorDescription {
	
	public static final String ADAPTORS_PREFIX = "xenon.adaptors.file.";
	
	protected FileAdaptor(String name, String description, String [] locations, XenonPropertyDescription [] properties) { 
		super(name, description, locations, properties);
	}

	@Override
	public boolean supportsThirdPartyCopy() { 
		// By default, adaptors do not support third party copy.
		return false;
	}

	@Override
	public boolean canReadSymboliclinks() {
		// By default, adaptors can read symbolic links.
		return true;
	}

	@Override
	public boolean canCreateSymboliclinks() { 
		// By default, adaptors cannot create symbolic links.
		return false;
	}

	@Override
	public boolean isConnectionless() { 
		// By default, adaptors require a connection.
		return false;
	}



	@Override
	public boolean supportsReadingPosixPermissions() {
		return false;
	}

	@Override
	public boolean supportsSettingPosixPermissions() {
		return false;
	}

	@Override
	public boolean supportsRename() { 
		return true;
	}
	
	@Override
	public boolean canAppend() { 
	    return true; 
	}

	@Override
	public boolean needsSizeBeforehand(){
		return false;
	}
	
	public abstract FileSystem createFileSystem(String location, Credential credential, Map<String,String> properties) throws XenonException;
	
}
