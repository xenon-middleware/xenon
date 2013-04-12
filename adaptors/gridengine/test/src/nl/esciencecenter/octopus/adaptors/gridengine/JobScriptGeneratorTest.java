package nl.esciencecenter.octopus.adaptors.gridengine;

import static org.junit.Assert.*;

import nl.esciencecenter.octopus.jobs.JobDescription;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JobScriptGeneratorTest {

    @Test
    public void testGenerate() {
        JobScriptGenerator generator = new JobScriptGenerator();
        
        JobDescription job = new JobDescription();
        
        job.setExecutable("/bin/echo");
        
        job.setArguments("this", "and", "that");
        
        System.out.println(generator.generate(job));
    }

}
