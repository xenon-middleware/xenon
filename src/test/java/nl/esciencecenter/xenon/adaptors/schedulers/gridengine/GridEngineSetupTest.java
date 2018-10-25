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
package nl.esciencecenter.xenon.adaptors.schedulers.gridengine;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import nl.esciencecenter.xenon.adaptors.schedulers.gridengine.ParallelEnvironmentInfo.AllocationRule;

/**
 *
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GridEngineSetupTest {

    /**
     * Test method for {@link nl.esciencecenter.xenon.adaptors.schedulers.gridengine.GridEngineSetup#getQueueNames()}.
     */
    @Test
    public void test01_getQueueNames() {
        String[] input = new String[] { "small.q", "middle.q", "large.q" };

        GridEngineSetup testSetup = new GridEngineSetup(input, null, null, 15);

        String[] result = testSetup.getQueueNames();

        assertArrayEquals("returned queue names should be equal to input queue names", input, result);
    }

    static GridEngineSetup getGridEngineSetup(ParallelEnvironmentInfo pe) {
        String[] queueNames = new String[]{"some.q"};

        Map<String, QueueInfo> queueInfos = new HashMap<>();
        queueInfos.put("some.q", new QueueInfo("some.q", 4, pe.getName()));

        Map<String, ParallelEnvironmentInfo> peInfos = new HashMap<>();

        peInfos.put(pe.getName(), pe);
        return new GridEngineSetup(queueNames, queueInfos, peInfos, 15);
    }

    @Test
    public void test_getSingleNodeParallelEnvironment_allocationruleroundrobin_notpresent() {
        ParallelEnvironmentInfo pe = new ParallelEnvironmentInfo("some.pe", 100, AllocationRule.ROUND_ROBIN, 0);
        GridEngineSetup setup = getGridEngineSetup(pe);

        assertFalse(setup.getSingleNodeParallelEnvironment(4, null).isPresent());
    }

    @Test
    public void test_getSingleNodeParallelEnvironment_allocationrulefillup_notpresent() {
        ParallelEnvironmentInfo pe = new ParallelEnvironmentInfo("some.pe", 100, AllocationRule.FILL_UP, 0);
        GridEngineSetup setup = getGridEngineSetup(pe);

        assertFalse(setup.getSingleNodeParallelEnvironment(4, null).isPresent());
    }

    @Test
    public void test_getSingleNodeParallelEnvironment_allocationrulepeslots_present() {
        ParallelEnvironmentInfo pe = new ParallelEnvironmentInfo("some.pe", 100, AllocationRule.PE_SLOTS, 0);
        GridEngineSetup setup = getGridEngineSetup(pe);

        assertTrue(setup.getSingleNodeParallelEnvironment(4, null).isPresent());
    }

    @Test
    public void test_getSingleNodeParallelEnvironment_allocationruleintsmaller_notpresent() {
        ParallelEnvironmentInfo pe = new ParallelEnvironmentInfo("some.pe", 100, AllocationRule.INTEGER, 2);
        GridEngineSetup setup = getGridEngineSetup(pe);

        assertFalse(setup.getSingleNodeParallelEnvironment(4, null).isPresent());
    }

    @Test
    public void test_getSingleNodeParallelEnvironment_allocationruleinttequal_present() {
        ParallelEnvironmentInfo pe = new ParallelEnvironmentInfo("some.pe", 100, AllocationRule.INTEGER, 4);
        GridEngineSetup setup = getGridEngineSetup(pe);

        assertTrue(setup.getSingleNodeParallelEnvironment(4, null).isPresent());
    }

    @Test
    public void test_getSingleNodeParallelEnvironment_allocationruleintbigger_present() {
        ParallelEnvironmentInfo pe = new ParallelEnvironmentInfo("some.pe", 100, AllocationRule.INTEGER, 8);
        GridEngineSetup setup = getGridEngineSetup(pe);

        assertTrue(setup.getSingleNodeParallelEnvironment(4, null).isPresent());
    }

    @Test
    public void test_getSingleNodeParallelEnvironment_queueugiven_penotinqueue() {
        String[] queueNames = new String[]{"some.q"};

        Map<String, QueueInfo> queueInfos = new HashMap<>();
        queueInfos.put("some.q", new QueueInfo("some.q", 4));

        Map<String, ParallelEnvironmentInfo> peInfos = new HashMap<>();
        ParallelEnvironmentInfo pe = new ParallelEnvironmentInfo("some.pe", 100, AllocationRule.INTEGER, 4);
        peInfos.put("some.pe", pe);
        GridEngineSetup setup = new GridEngineSetup(queueNames, queueInfos, peInfos, 15);

        assertFalse(setup.getSingleNodeParallelEnvironment(4, "some.q").isPresent());
    }

    @Test
    public void test_getSingleNodeParallelEnvironment_queueugiven_peinqueue() {
        ParallelEnvironmentInfo pe = new ParallelEnvironmentInfo("some.pe", 100, AllocationRule.INTEGER, 4);
        GridEngineSetup setup = getGridEngineSetup(pe);

        assertTrue(setup.getSingleNodeParallelEnvironment(4, "some.q").isPresent());
    }

    @Test
    public void test_getSingleNodeParallelEnvironment_queueuabsent_penotinqueues() {
        String[] queueNames = new String[]{"some.q"};

        Map<String, QueueInfo> queueInfos = new HashMap<>();
        queueInfos.put("some.q", new QueueInfo("some.q", 4));

        Map<String, ParallelEnvironmentInfo> peInfos = new HashMap<>();
        ParallelEnvironmentInfo pe = new ParallelEnvironmentInfo("some.pe", 100, AllocationRule.INTEGER, 4);
        peInfos.put("some.pe", pe);
        GridEngineSetup setup = new GridEngineSetup(queueNames, queueInfos, peInfos, 15);

        assertFalse(setup.getSingleNodeParallelEnvironment(4, null).isPresent());
    }

    @Test
    public void test_getSingleNodeParallelEnvironment_queueuabsent_peinqueues() {
        String[] queueNames = new String[]{"some.q"};

        Map<String, QueueInfo> queueInfos = new HashMap<>();
        queueInfos.put("some.q", new QueueInfo("some.q", 4, "some.pe"));

        Map<String, ParallelEnvironmentInfo> peInfos = new HashMap<>();
        ParallelEnvironmentInfo pe = new ParallelEnvironmentInfo("some.pe", 100, AllocationRule.INTEGER, 4);
        peInfos.put("some.pe", pe);
        GridEngineSetup setup = new GridEngineSetup(queueNames, queueInfos, peInfos, 15);

        assertTrue(setup.getSingleNodeParallelEnvironment(4, null).isPresent());
    }

    @Test
    public void test03_qconfPeDetailsArguments() {
        String[] input = new String[] { "some.pe", "other.pe", "this.pe", "that.pe" };

        String[] expected = new String[] { "-sp", "some.pe", "-sp", "other.pe", "-sp", "this.pe", "-sp", "that.pe" };

        String[] result = GridEngineSetup.qconfPeDetailsArguments(input);

        assertArrayEquals("setup does not generate pe arguments properly", expected, result);
    }

}
