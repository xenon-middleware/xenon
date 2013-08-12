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
package nl.esciencecenter.octopus.adaptors.gridengine;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.adaptors.gridengine.ParallelEnvironmentInfo.AllocationRule;
import nl.esciencecenter.octopus.exceptions.OctopusException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Niels Drost
 * 
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ParallelEnvironmentInfoTest {

    @Test
    public void test01a_parallelEnvironmentInfoFromMap_IntegerRule_Result() throws OctopusException {
        Map<String, String> input = new HashMap<String, String>();

        input.put("pe_name", "some.pe");
        input.put("slots", "200");
        input.put("allocation_rule", "2");

        ParallelEnvironmentInfo result = new ParallelEnvironmentInfo(input);

        assertEquals("parallel environment name incorrect", "some.pe", result.getName());
        assertEquals("parallel environment slots incorrect", 200, result.getSlots());
        assertEquals("parallel environment allocation rule incorrect", ParallelEnvironmentInfo.AllocationRule.INTEGER,
                result.getAllocationRule());
        assertEquals("parallel environment allocation ppn incorrect", 2, result.getPpn());
    }

    @Test
    public void test01b_parallelEnvironmentInfoFromMap_PeSlotsRule_Result() throws OctopusException {
        Map<String, String> input = new HashMap<String, String>();

        input.put("pe_name", "some.pe");
        input.put("slots", "200");
        input.put("allocation_rule", "$pe_slots");

        ParallelEnvironmentInfo result = new ParallelEnvironmentInfo(input);

        assertEquals("parallel environment name incorrect", "some.pe", result.getName());
        assertEquals("parallel environment slots incorrect", 200, result.getSlots());
        assertEquals("parallel environment allocation rule incorrect", ParallelEnvironmentInfo.AllocationRule.PE_SLOTS,
                result.getAllocationRule());
    }

    @Test
    public void test01c_parallelEnvironmentInfoFromMap_RoundRobinRule_Result() throws OctopusException {
        Map<String, String> input = new HashMap<String, String>();

        input.put("pe_name", "some.pe");
        input.put("slots", "200");
        input.put("allocation_rule", "$round_robin");

        ParallelEnvironmentInfo result = new ParallelEnvironmentInfo(input);

        assertEquals("parallel environment name incorrect", "some.pe", result.getName());
        assertEquals("parallel environment slots incorrect", 200, result.getSlots());
        assertEquals("parallel environment allocation rule incorrect", ParallelEnvironmentInfo.AllocationRule.ROUND_ROBIN,
                result.getAllocationRule());
    }

    @Test
    public void test01d_parallelEnvironmentInfoFromMap_RoundRobinRule_Result() throws OctopusException {
        Map<String, String> input = new HashMap<String, String>();

        input.put("pe_name", "some.pe");
        input.put("slots", "200");
        input.put("allocation_rule", "$fill_up");

        ParallelEnvironmentInfo result = new ParallelEnvironmentInfo(input);

        assertEquals("parallel environment name incorrect", "some.pe", result.getName());
        assertEquals("parallel environment slots incorrect", 200, result.getSlots());
        assertEquals("parallel environment allocation rule incorrect", ParallelEnvironmentInfo.AllocationRule.FILL_UP,
                result.getAllocationRule());
    }
    
    @Test(expected=OctopusException.class)
    public void test01e_parallelEnvironmentInfoFromMap_IncorrectAllocationRule_ExceptionThrown() throws OctopusException {
        Map<String, String> input = new HashMap<String, String>();

        input.put("pe_name", "some.pe");
        input.put("slots", "200");
        input.put("allocation_rule", "five");

        new ParallelEnvironmentInfo(input);
    }
    
    @Test(expected=OctopusException.class)
    public void test01f_parallelEnvironmentInfoFromMap_NoName_ExceptionThrown() throws OctopusException {
        Map<String, String> input = new HashMap<String, String>();

        input.put("slots", "200");
        input.put("allocation_rule", "2");

        new ParallelEnvironmentInfo(input);
    }
    
    @Test(expected=OctopusException.class)
    public void test01g_parallelEnvironmentInfoFromMap_NoSlots_ExceptionThrown() throws OctopusException {
        Map<String, String> input = new HashMap<String, String>();

        input.put("pe_name", "some.pe");
        input.put("allocation_rule", "2");

        new ParallelEnvironmentInfo(input);
    }
    
    @Test(expected=OctopusException.class)
    public void test01h_parallelEnvironmentInfoFromMap_IncorrectSlots_ExceptionThrown() throws OctopusException {
        Map<String, String> input = new HashMap<String, String>();

        input.put("pe_name", "some.pe");
        input.put("slots", "twohundred");
        input.put("allocation_rule", "2");

        new ParallelEnvironmentInfo(input);
    }
    
    @Test(expected=OctopusException.class)
    public void test01i_parallelEnvironmentInfoFromMap_NoAllocationRule_ExceptionThrown() throws OctopusException {
        Map<String, String> input = new HashMap<String, String>();

        input.put("pe_name", "some.pe");
        input.put("slots", "200");

        new ParallelEnvironmentInfo(input);
    }

    @Test
    public void test02_toString_SomeInfo_Result() throws OctopusException {
        ParallelEnvironmentInfo info = new ParallelEnvironmentInfo("some.name", 4, AllocationRule.INTEGER, 2);
        
        String result = info.toString();
        
        String expected = "ParallelEnvironmentInfo [name=some.name, slots=4, allocationRule=INTEGER, ppn=2]";
        
        System.out.println(result);
        
        assertEquals(expected, result);
    }


    
}
