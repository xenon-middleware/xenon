package nl.esciencecenter.octopus.engine.jobs;

import java.util.UUID;

import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.jobs.JobDescription;

public class Sandbox {
    
    private final UUID sandboxID;
    private final Path directory;

    public Sandbox(JobDescription description, OctopusEngine engine, Path sandboxRoot) throws OctopusException {
        this.sandboxID = UUID.randomUUID();
        
        this.directory = sandboxRoot.resolve(sandboxID.toString());
    }

    /**
     * Working directory for adaptor
     */
    public String getWorkingDirectory() {
        return null;
        
    }

    /**
     * @return 
     */
    public Path getStdin() {
        // TODO Auto-generated method stub
        return null;
    }

    public Path getStdout() {
        // TODO Auto-generated method stub
        return null;
    }

    public Path getStderr() {
        // TODO Auto-generated method stub
        return null;
    }

    public void postState() throws OctopusException {
        // TODO Auto-generated method stub
        
    }

    public void preStage()  throws OctopusException {
        // TODO Auto-generated method stub
        
    }

}
