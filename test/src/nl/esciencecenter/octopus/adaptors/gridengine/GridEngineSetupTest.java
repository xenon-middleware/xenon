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

import static org.junit.Assert.assertArrayEquals;
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
public class GridEngineSetupTest {

    /**
     * Test method for {@link nl.esciencecenter.octopus.adaptors.gridengine.GridEngineSetup#getQueueNames()}.
     */
    @Test
    public void test01_getQueueNames() {
        String[] input = new String[] { "small.q", "middle.q", "large.q" };

        GridEngineSetup testSetup = new GridEngineSetup(input, null, null);

        String[] result = testSetup.getQueueNames();

        assertArrayEquals("returned queue names should be equal to input queue names", input, result);
    }

    @Test
    public void test02a_calculateSlots_singleNodePe_slots() throws OctopusException {
        String[] queueNames = new String[] { "some.q" };

        Map<String, QueueInfo> queueInfos = new HashMap<String, QueueInfo>();
        queueInfos.put("some.q", new QueueInfo("some.q", 4, "some.pe"));

        Map<String, ParallelEnvironmentInfo> peInfos = new HashMap<String, ParallelEnvironmentInfo>();
        peInfos.put("some.pe", new ParallelEnvironmentInfo("some.pe", 100, AllocationRule.PE_SLOTS, 0));

        GridEngineSetup testSetup = new GridEngineSetup(queueNames, queueInfos, peInfos);

        int expected = 1;

        int result = testSetup.calculateSlots("some.pe", "some.q", 1);

        assertEquals("pe_slots pe allocation should always return 1", expected, result);
    }

    @Test(expected = OctopusException.class)
    public void test02b_calculateSlots_singleNodePeMultipleNodes_exceptionThrown() throws OctopusException {
        String[] queueNames = new String[] { "some.q" };

        Map<String, QueueInfo> queueInfos = new HashMap<String, QueueInfo>();
        queueInfos.put("some.q", new QueueInfo("some.q", 4, "some.pe"));

        Map<String, ParallelEnvironmentInfo> peInfos = new HashMap<String, ParallelEnvironmentInfo>();
        peInfos.put("some.pe", new ParallelEnvironmentInfo("some.pe", 100, AllocationRule.PE_SLOTS, 0));

        GridEngineSetup testSetup = new GridEngineSetup(queueNames, queueInfos, peInfos);

        testSetup.calculateSlots("some.pe", "some.q", 2);
    }

    @Test
    public void test02c_calculateSlots_fillUpPe_slots() throws OctopusException {
        String[] queueNames = new String[] { "some.q" };

        Map<String, QueueInfo> queueInfos = new HashMap<String, QueueInfo>();
        queueInfos.put("some.q", new QueueInfo("some.q", 4, "some.pe"));

        Map<String, ParallelEnvironmentInfo> peInfos = new HashMap<String, ParallelEnvironmentInfo>();
        peInfos.put("some.pe", new ParallelEnvironmentInfo("some.pe", 100, AllocationRule.FILL_UP, 0));

        GridEngineSetup testSetup = new GridEngineSetup(queueNames, queueInfos, peInfos);

        //we expect all slots of the pe to be claimed for all nodes
        int expected = 2 * 4;

        int result = testSetup.calculateSlots("some.pe", "some.q", 2);

        assertEquals("fill_up pe allocation should return node*slots", expected, result);
    }

    @Test
    public void test02d_calculateSlots_roundRobinPe_slots() throws OctopusException {
        String[] queueNames = new String[] { "some.q" };

        Map<String, QueueInfo> queueInfos = new HashMap<String, QueueInfo>();
        queueInfos.put("some.q", new QueueInfo("some.q", 4, "some.pe"));

        Map<String, ParallelEnvironmentInfo> peInfos = new HashMap<String, ParallelEnvironmentInfo>();
        peInfos.put("some.pe", new ParallelEnvironmentInfo("some.pe", 100, AllocationRule.ROUND_ROBIN, 0));

        GridEngineSetup testSetup = new GridEngineSetup(queueNames, queueInfos, peInfos);

        //we expect the number of nodes
        int expected = 2;

        int result = testSetup.calculateSlots("some.pe", "some.q", 2);

        assertEquals("round_robin pe allocation should return the number of nodes", expected, result);
    }

    @Test
    public void test02e_calculateSlots_integerPe_slots() throws OctopusException {
        String[] queueNames = new String[] { "some.q" };

        Map<String, QueueInfo> queueInfos = new HashMap<String, QueueInfo>();
        queueInfos.put("some.q", new QueueInfo("some.q", 4, "some.pe"));

        Map<String, ParallelEnvironmentInfo> peInfos = new HashMap<String, ParallelEnvironmentInfo>();
        peInfos.put("some.pe", new ParallelEnvironmentInfo("some.pe", 100, AllocationRule.INTEGER, 3));

        GridEngineSetup testSetup = new GridEngineSetup(queueNames, queueInfos, peInfos);

        //we expect the number of nodes
        int expected = 6;

        int result = testSetup.calculateSlots("some.pe", "some.q", 2);

        assertEquals("normal pe allocation should always return node*slots", expected, result);
    }

    @Test(expected = OctopusException.class)
    public void test02e_calculateSlots_InvalidPe_exceptionThrown() throws OctopusException {
        String[] queueNames = new String[] { "some.q" };

        Map<String, QueueInfo> queueInfos = new HashMap<String, QueueInfo>();
        queueInfos.put("some.q", new QueueInfo("some.q", 4, "some.pe"));

        Map<String, ParallelEnvironmentInfo> peInfos = new HashMap<String, ParallelEnvironmentInfo>();

        GridEngineSetup testSetup = new GridEngineSetup(queueNames, queueInfos, peInfos);

        testSetup.calculateSlots("some.pe", "some.q", 2);
    }

    @Test(expected = OctopusException.class)
    public void test02e_calculateSlots_InvalidQueue_exceptionThrown() throws OctopusException {
        String[] queueNames = new String[] { "some.q" };

        Map<String, QueueInfo> queueInfos = new HashMap<String, QueueInfo>();

        Map<String, ParallelEnvironmentInfo> peInfos = new HashMap<String, ParallelEnvironmentInfo>();
        peInfos.put("some.pe", new ParallelEnvironmentInfo("some.pe", 100, AllocationRule.INTEGER, 3));

        GridEngineSetup testSetup = new GridEngineSetup(queueNames, queueInfos, peInfos);

        testSetup.calculateSlots("some.pe", "some.q", 2);
    }

    @Test
    public void test03_qconfPeDetailsArguments() {
        String[] input = new String[] { "some.pe", "other.pe", "this.pe", "that.pe" };

        String[] expected = new String[] { "-pe", "some.pe", "-pe", "other.pe", "-pe", "this.pe", "-pe", "that.pe" };
        
        String[] result = GridEngineSetup.qconfPeDetailsArguments(input);
        
        assertArrayEquals("setup does not generate pe arguments properly", expected, result);
    }

}
