package nl.esciencecenter.octopus.engine;

import nl.esciencecenter.octopus.AdaptorInfo;
import nl.esciencecenter.octopus.engine.files.FilesAdaptor;
import nl.esciencecenter.octopus.engine.jobs.JobsAdaptor;

/**
 * New-style adaptor interface. Adaptors are expected to implement one or more
 * create functions of the Octopus interface, depending on which functionality they
 * provide.
 * 
 * @author Niels Drost
 * 
 */
public abstract class Adaptor implements AdaptorInfo {

	private final String name;
	private final String description;
	private final String [] supportedSchemes;
	
    protected final OctopusEngine octopusEngine;

    // FIXME: Add properties!
    protected Adaptor(OctopusEngine octopusEngine, String name, String description, String [] supportedSchemes) {
		
    	super();
		
    	this.name = name;
		this.description = description;
		this.supportedSchemes = supportedSchemes;
		
		this.octopusEngine = octopusEngine;
	}
    
    public String getName() {
        return name;
    }

    public String getDescription() {
    	return description;
    }
    
    public String[] getSupportedSchemes() {
        return supportedSchemes.clone();
    }
    
    public boolean supports(String scheme) {
    
    	for (String s : supportedSchemes) {
            if (s.equalsIgnoreCase(scheme)) {
                return true;
            }
        }
        
//    	if (scheme == null) {
//            return true;
//        }
        
        return false;
    }

    public abstract FilesAdaptor filesAdaptor(); 
    
    public abstract JobsAdaptor jobsAdaptor();

    public abstract void end();
}
