package nl.esciencecenter.xenon.adaptors.file;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.files.FileSystemAdaptorDescription;
import nl.esciencecenter.xenon.files.FileSystem;

public abstract class FileAdaptor {
	
	public static final String ADAPTORS_PREFIX = "xenon.adaptors.file.";
	
	private static int currentID = 1;
	
	private final FileSystemAdaptorDescription adaptorDescription;
	
	protected FileAdaptor(String name, String description, String [] locations, XenonPropertyDescription [] properties, 
			boolean supportsThirdPartyCopy) {
		adaptorDescription = new FileSystemAdaptorDescription(name, description, locations, properties, supportsThirdPartyCopy);
	}
	
    protected synchronized String getNewUniqueID() {
        String res = adaptorDescription.getName() + "." + currentID;
        currentID++;
        return res;
    }

	public String getName() { 
		return adaptorDescription.getName();
	}
	
	public FileSystemAdaptorDescription getAdaptorDescription() { 
		// TODO: should include a map of properties ? 
		return adaptorDescription;
	}
	   
	public abstract FileSystem createFileSystem(String location, Credential credential, Map<String,String> properties) throws XenonException;
	
}
