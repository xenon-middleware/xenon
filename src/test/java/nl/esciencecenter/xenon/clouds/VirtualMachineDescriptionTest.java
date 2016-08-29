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

package nl.esciencecenter.xenon.clouds;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

/**
 * @version 2.0
 * @since 2.0
 *
 */
public class VirtualMachineDescriptionTest {
   
    @Test
    public void test_new() throws Exception {
        new VirtualMachineDescription();
    }
    
    @Test
    public void test_setters_getters() throws Exception {
        VirtualMachineDescription vmd = new VirtualMachineDescription();
        
        vmd.setImageID("id1");
        String s = vmd.getImageID();
        assertEquals("set/getImageID failed", "id1", s);
        
        vmd.setRegion("region1");
        s = vmd.getRegion();
        assertEquals("set/getRegion failed", "region1", s);
        
        vmd.setNodeType("type1");
        s = vmd.getNodeType();
        assertEquals("set/getNodeType failed", "type1", s);
        
        Map<String, String> opt = new HashMap<>(3);
        opt.put("OPT1", "ARG1");
        opt.put("OPT2", "ARG2");

        vmd.setVirtualMachineOptions(opt);
        Map<String, String> opt2 = vmd.getVirtualMachineOptions();
        assertTrue("set/getVirtualMachineOptions failed", opt.equals(opt2));

        Map<String, String> opt3 = new HashMap<>(3);
        vmd.setVirtualMachineOptions(opt3);
        opt2 = vmd.getVirtualMachineOptions();
        assertTrue("set/getVirtualMachineOptions failed", opt3.equals(opt2));

        vmd.addVirtualMachineOption("OPT1", "ARG1");
        vmd.addVirtualMachineOption("OPT2", "ARG2");
        opt2 = vmd.getVirtualMachineOptions();
        assertTrue("set/getVirtualMachineOptions failed", opt.equals(opt2));

        
        
    }
}
