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
package nl.esciencecenter.octopus.adaptors.slurm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import nl.esciencecenter.octopus.exceptions.OctopusException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Niels Drost
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SlurmSchedulerConnectionTest {

    @Test
    public void test01a_exitcodeFromString_SomeExitcode_Integer() throws OctopusException {
        String input = "5";
        
        Integer expected = 5;
        
        Integer result = SlurmSchedulerConnection.exitcodeFromString(input);

        assertEquals("Failed to obtain exit code from simple string", expected, result);
    }
    
    @Test
    public void test01b_exitcodeFromString_SomeExitcodeWithSignal_Integer() throws OctopusException {
        String input = "5:43";
        
        Integer expected = 5;
        
        Integer result = SlurmSchedulerConnection.exitcodeFromString(input);

        assertEquals("Failed to obtain exit code from exitcode string with signal", expected, result);
    }
    
    @Test
    public void test01c_exitcodeFromString_NullInput_NullOutput() throws OctopusException {
        String input = null;
        
        Integer expected = null;
        
        Integer result = SlurmSchedulerConnection.exitcodeFromString(input);

        assertEquals("Null input should lead to Null output", expected, result);
    }
    
    @Test(expected=OctopusException.class)
    public void test01d_exitcodeFromString_NotANumber_ExceptionThrown() throws OctopusException {
        String input = "five";
        
        SlurmSchedulerConnection.exitcodeFromString(input);
    }

    @Test
    public void testGetJobStatusFromSacctInfo() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetJobStatusFromScontrolInfo() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetJobStatusFromSqueueInfo() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetQueueStatusFromSInfo() {
        fail("Not yet implemented");
    }

    @Test
    public void testIdentifiersAsCSList() {
        fail("Not yet implemented");
    }

    @Test
    public void testIsDoneState() {
        fail("Not yet implemented");
    }

    @Test
    public void testIsFailedState() {
        fail("Not yet implemented");
    }

    @Test
    public void testVerifyJobDescriptionJobDescription() {
        fail("Not yet implemented");
    }

}
