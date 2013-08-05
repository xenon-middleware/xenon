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
package nl.esciencecenter.octopus.adaptors.scripting;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import nl.esciencecenter.octopus.exceptions.IncompleteJobDescriptionException;
import nl.esciencecenter.octopus.exceptions.InvalidJobDescriptionException;
import nl.esciencecenter.octopus.exceptions.InvalidLocationException;
import nl.esciencecenter.octopus.jobs.JobDescription;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Niels Drost
 * 
 */
@FixMethodOrder(MethodSorters.JVM)
public class SchedulerConnectionTest {

    @Test
    public void verifyJobDescription_ValidJobDescription_NoException() throws Exception {
        JobDescription description = new JobDescription();

        //all the settings the function checks for set exactly right
        description.setExecutable("/bin/nothing");
        description.setNodeCount(1);
        description.setProcessesPerNode(1);
        description.setMaxTime(1);
        description.setInteractive(false);

        SchedulerConnection.verifyJobDescription(description, "testing");
    }
    
    @Test(expected=IncompleteJobDescriptionException.class)
    public void verifyJobDescription_NoExecutable_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        description.setExecutable(null);

        SchedulerConnection.verifyJobDescription(description, "testing");
    }
    
    @Test(expected=InvalidJobDescriptionException.class)
    public void verifyJobDescription_ZeroNodeCount_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        description.setExecutable("/bin/nothing");
        description.setNodeCount(0);

        SchedulerConnection.verifyJobDescription(description, "testing");
    }

    
    @Test(expected=InvalidJobDescriptionException.class)
    public void verifyJobDescription_NegativeNodeCount_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        description.setExecutable("/bin/nothing");
        description.setNodeCount(-1);

        SchedulerConnection.verifyJobDescription(description, "testing");
    }

    @Test(expected=InvalidJobDescriptionException.class)
    public void verifyJobDescription_ZeroProcessesPerNode_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        description.setExecutable("/bin/nothing");
        description.setProcessesPerNode(0);

        SchedulerConnection.verifyJobDescription(description, "testing");
    }

    
    @Test(expected=InvalidJobDescriptionException.class)
    public void verifyJobDescription_NegativeProcessesPerNode_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        description.setExecutable("/bin/nothing");
        description.setProcessesPerNode(-1);

        SchedulerConnection.verifyJobDescription(description, "testing");
    }
    
    @Test(expected=InvalidJobDescriptionException.class)
    public void verifyJobDescription_ZeroMaxTime_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        description.setExecutable("/bin/nothing");
        description.setMaxTime(0);

        SchedulerConnection.verifyJobDescription(description, "testing");
    }

    
    @Test(expected=InvalidJobDescriptionException.class)
    public void verifyJobDescription_NegativeMaxTime_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        description.setExecutable("/bin/nothing");
        description.setMaxTime(-1);

        SchedulerConnection.verifyJobDescription(description, "testing");
    }
    
    @Test(expected=InvalidJobDescriptionException.class)
    public void verifyJobDescription_InteractiveJob_ExceptionThrown() throws Exception {
        JobDescription description = new JobDescription();

        description.setExecutable("/bin/nothing");
        description.setInteractive(true);

        SchedulerConnection.verifyJobDescription(description, "testing");
    }

    @Test
    public void getSubSchedulerLocation_NoHost_LocalLocation() throws Exception {
        URI input = new URI("fake:///");

        URI expected = new URI("local:///");
        
        URI result = SchedulerConnection.getSubSchedulerLocation(input, "fake", "fake");
        
        assertEquals(expected, result);
    }

    
    @Test
    public void getSubSchedulerLocation_RemoteHost_SSHLocation() throws Exception {
        URI input = new URI("fake://some.host.nl");

        URI expected = new URI("ssh://some.host.nl");
        
        URI result = SchedulerConnection.getSubSchedulerLocation(input, "fake", "fake");
        
        assertEquals(expected, result);
    }
    
    @Test
    public void getSubSchedulerLocation_SingleSlashPath_NoPathLocation() throws Exception {
        URI input = new URI("fake://some.host.nl/");

        //note the path has been stripped
        URI expected = new URI("ssh://some.host.nl");
        
        URI result = SchedulerConnection.getSubSchedulerLocation(input, "fake", "fake");
        
        assertEquals(expected, result);
    }

    @Test
    public void getSubSchedulerLocation_ValidScheme_NoException() throws Exception {
        URI input = new URI("valid://some.host.nl/");

        SchedulerConnection.getSubSchedulerLocation(input, "fake", "fake", "other.scheme", "valid");
    }
    
    @Test(expected = InvalidLocationException.class)
    public void getSubSchedulerLocation_InvalidScheme_ThrowsException() throws Exception {
        URI input = new URI("illegal://some.host.nl/");

        SchedulerConnection.getSubSchedulerLocation(input, "fake", "fake");
    }


}
