package nl.esciencecenter.octopus.adaptors.gridengine;

import java.util.Formatter;
import java.util.Locale;

import nl.esciencecenter.octopus.jobs.JobDescription;

public class JobScriptGenerator {

    public static String generate(JobDescription description) {
        StringBuilder stringBuilder = new StringBuilder();
        Formatter script = new Formatter(stringBuilder, Locale.US);
        
        script.format("#!/bin/sh\n");
        script.format("#$ -N octopus\n"); 
        script.format("\n");

        script.format("%s", description.getExecutable());
        
        for(String argument: description.getArguments()) {
            script.format(" %s", argument);
        }
        
        script.format("exit 22\n");
        
        script.close();
        return stringBuilder.toString();
    }
}
