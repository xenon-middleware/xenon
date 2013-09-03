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
package nl.esciencecenter.octopus.engine;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.AdaptorStatus;
import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.UnknownPropertyException;

import org.junit.Test;

public class OctopusEngineTest {

    @Test
    public void newEngine_NullProperties_Succeeds() throws Exception {
        Octopus octopus = OctopusEngine.newOctopus(null);
        assertEquals("Expected empty hashmap", new HashMap<String, String>(), octopus.getProperties());
    }

    @Test
    public void newEngine_EmptyProperties_Succeeds() throws Exception {
        HashMap<String, String> tmp = new HashMap<String, String>();
        Octopus octopus = OctopusEngine.newOctopus(tmp);
        assertEquals("Expected empty hashmap", tmp, octopus.getProperties());
    }

    @Test(expected = UnknownPropertyException.class)
    public void newEngine_UnknownProperties_ThrowsException() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("key", "value");
        OctopusEngine.newOctopus(properties);
    }

    @Test
    public void newEngine_CorrectProperties_Success() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("octopus.adaptors.ssh.loadKnownHosts", "false");
        OctopusEngine.newOctopus(properties);
    }

    @Test(expected = UnknownPropertyException.class)
    public void newEngine_CorrectPropertiesAtWrongLevel_ThrowsException() throws Exception {
        Map<String, String> properties = new HashMap<>();
        // This property is valid at scheduler level, not at octopus level 
        properties.put("octopus.adaptors.ssh.queue.pollingDelay", "1500");
        OctopusEngine.newOctopus(properties);
    }

    @Test
    public void newEngine_MultipleCorrectProperties_Success() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("octopus.adaptors.ssh.loadKnownHosts", "false");
        properties.put("octopus.adaptors.local.queue.pollingDelay", "1500");
        OctopusEngine.newOctopus(properties);
    }

    @Test(expected = UnknownPropertyException.class)
    public void newEngine_CorrectAndIncorrectProperties_ThrowsException() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("octopus.adaptors.ssh.loadKnownHosts", "false");
        properties.put("key", "value");
        OctopusEngine.newOctopus(properties);
    }

    @Test(expected = UnknownPropertyException.class)
    public void newEngine_PropertiesAtCorrectAndWrongLevel_ThrowsException() throws Exception {
        Map<String, String> properties = new HashMap<>();
        // These are octopus level properties
        properties.put("octopus.adaptors.ssh.loadKnownHosts", "false");
        properties.put("octopus.adaptors.local.queue.pollingDelay", "1500");
        // This property is valid at scheduler level, not at octopus level 
        properties.put("octopus.adaptors.ssh.queue.pollingDelay", "1500");
        OctopusEngine.newOctopus(properties);
    }

    @Test
    public void getAdaptorInfo_LocalAdaptor_Succeeds() throws Exception {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newOctopus(null);
        AdaptorStatus adaptorInfo = octopus.getAdaptorStatus("local");
        assertEquals("The adaptor info for the local adaptor should contain the name \"local\"", "local", adaptorInfo.getName());
    }

    @Test
    public void getAdaptorInfo_UnknownAdaptor_ThrowsException() throws Exception {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newOctopus(null);
        try {
            octopus.getAdaptorStatus("hupsefluts");
            fail();
        } catch (OctopusException e) {
            assertEquals("engine adaptor: Could not find adaptor named hupsefluts", e.getMessage());
        }
    }

    @Test
    public void getAdaptorFor_LocalFile_Succeeds() throws Exception {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newOctopus(null);
        Adaptor adaptor = octopus.getAdaptorFor("file");
        assertEquals("The local adaptor should be returned", "local", adaptor.getName());
    }

    @Test
    public void getAdaptorFor_UnknownScheme_ThrowsException() throws Exception {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newOctopus(null);
        try {
            octopus.getAdaptorFor("hupsefluts");
            fail();
        } catch (OctopusException e) {
            assertEquals("engine adaptor: Could not find adaptor for scheme hupsefluts", e.getMessage());
        }
    }

    @Test
    public void getAdaptor_LocalAdaptor_Succeeds() throws Exception {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newOctopus(null);
        Adaptor adaptor = octopus.getAdaptor("local");
        assertEquals("The local adaptor should be returned", "local", adaptor.getName());
    }

    @Test
    public void getAdaptor_UnknownAdaptor_ThrowsException() throws Exception {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newOctopus(null);
        try {
            octopus.getAdaptor("hupsefluts");
            fail();
        } catch (OctopusException e) {
            assertEquals("engine adaptor: Could not find adaptor named hupsefluts", e.getMessage());
        }
    }

    //    @Test
    //    public void testGetAdaptors() throws Exception {
    //        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newOctopus(null);
    //
    //        Adaptor[] tmp = octopus.getAdaptors();
    //
    //        assert (tmp != null);
    //        assert (tmp.length == 3);
    //    }

    @Test
    public void getAdaptorInfos_StandardAdaptors_Succeeds() throws Exception {

        // We currently have 4 adaptors, local, ssh, gridengine, slurm 
        int count = 4;

        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newOctopus(null);

        AdaptorStatus[] tmp = octopus.getAdaptorStatuses();

        assertNotNull("Expected AdaptorStatus array", tmp);
        assertTrue("Expected " + count + " adaptors to be returned", tmp.length == count);
    }

    @Test
    public void close_SingleOctopus_Succeeds() throws Exception {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newOctopus(null);
        OctopusEngine.closeOctopus(octopus);
    }

    @Test
    public void close_MultipleOctopus_Succeeds() throws Exception {
        OctopusEngine octopus1 = (OctopusEngine) OctopusEngine.newOctopus(null);
        OctopusEngine octopus2 = (OctopusEngine) OctopusEngine.newOctopus(null);

        OctopusEngine.closeOctopus(octopus2);
        OctopusEngine.closeOctopus(octopus1);
    }

    @Test
    public void endAll_MultipleOctopus_Succeeds() throws Exception {
        OctopusEngine.newOctopus(null);
        OctopusEngine.newOctopus(null);

        OctopusEngine.endAll();
    }

    @Test(expected = OctopusException.class)
    public void closenIvokedTwice_SingleOctopus_ThrowsException() throws Exception {
        OctopusEngine octopus1 = (OctopusEngine) OctopusEngine.newOctopus(null);
        OctopusEngine.closeOctopus(octopus1);
        OctopusEngine.closeOctopus(octopus1);
    }

    @Test
    public void toString_OctopusNoProperties_Succeeds() throws Exception {
        OctopusEngine octopus1 = (OctopusEngine) OctopusEngine.newOctopus(null);
        String tmp = octopus1.toString();

        // TODO; should check output of toString() ?
    }

}
