/**
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
package nl.esciencecenter.xenon.adaptors.gridengine;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.gridengine.QueueInfo;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Niels Drost
 * 
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QueueInfoTest {

    @Test
    public void test01a_queueInfoFromMap_Map_Result() throws XenonException {
        Map<String, String> input = new HashMap<>();

        input.put("qname", "some.q");
        input.put("slots", "200");
        input.put("pe_list", "some.pe other.pe");

        QueueInfo result = new QueueInfo(input);

        String[] expectedPeList = new String[] { "some.pe", "other.pe" };

        assertEquals("queue name incorrect", "some.q", result.getName());
        assertEquals("queue slots incorrect", 200, result.getSlots());

        assertArrayEquals("queue pe list incorrect", expectedPeList, result.getParallelEnvironments());
    }

    @Test(expected = XenonException.class)
    public void test01b_queueInfoFromMap_NoName_ExceptionThrown() throws XenonException {
        Map<String, String> input = new HashMap<>();

        //input.put("qname", "some.q");
        input.put("slots", "200");
        input.put("pe_list", "some.pe other.pe");

        QueueInfo result = new QueueInfo(input);

        String[] expectedPeList = new String[] { "some.pe", "other.pe" };

        assertEquals("queue name incorrect", "some.pe", result.getName());
        assertEquals("queue slots incorrect", 200, result.getSlots());

        assertArrayEquals("queue pe list incorrect", expectedPeList, result.getParallelEnvironments());
    }

    @Test(expected = XenonException.class)
    public void test01c_queueInfoFromMap_NoSlots_ExceptionThrown() throws XenonException {
        Map<String, String> input = new HashMap<>();

        input.put("qname", "some.q");
        //input.put("slots", "200");
        input.put("pe_list", "some.pe other.pe");

        QueueInfo result = new QueueInfo(input);

        String[] expectedPeList = new String[] { "some.pe", "other.pe" };

        assertEquals("queue name incorrect", "some.pe", result.getName());
        assertEquals("queue slots incorrect", 200, result.getSlots());

        assertArrayEquals("queue pe list incorrect", expectedPeList, result.getParallelEnvironments());
    }

    @Test(expected = XenonException.class)
    public void test01d_queueInfoFromMap_IncorrectSlots_ExceptionThrown() throws XenonException {
        Map<String, String> input = new HashMap<>();

        input.put("qname", "some.q");
        input.put("slots", "twohundred");
        input.put("pe_list", "some.pe other.pe");

        QueueInfo result = new QueueInfo(input);

        String[] expectedPeList = new String[] { "some.pe", "other.pe" };

        assertEquals("queue name incorrect", "some.pe", result.getName());
        assertEquals("queue slots incorrect", 200, result.getSlots());

        assertArrayEquals("queue pe list incorrect", expectedPeList, result.getParallelEnvironments());
    }

    @Test(expected = XenonException.class)
    public void test01e_queueInfoFromMap_NoPeList_ExceptionThrown() throws XenonException {
        Map<String, String> input = new HashMap<>();

        input.put("qname", "some.q");
        input.put("slots", "200");
        //input.put("pe_list", "some.pe other.pe");

        QueueInfo result = new QueueInfo(input);

        String[] expectedPeList = new String[] { "some.pe", "other.pe" };

        assertEquals("queue name incorrect", "some.pe", result.getName());
        assertEquals("queue slots incorrect", 200, result.getSlots());

        assertArrayEquals("queue pe list incorrect", expectedPeList, result.getParallelEnvironments());
    }

    @Test
    public void test02_toString_SomeInfo_Result() throws XenonException {
        QueueInfo info = new QueueInfo("some.name", 4, "some.pe", "other.pe");

        String result = info.toString();

        String expected = "QueueInfo [name=some.name, slots=4, parallelEnvironments=[some.pe, other.pe]]";

        System.out.println(result);

        assertEquals(expected, result);
    }

}
