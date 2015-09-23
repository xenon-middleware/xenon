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
package nl.esciencecenter.xenon.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.AdaptorStatus;
import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.UnknownPropertyException;
import nl.esciencecenter.xenon.engine.Adaptor;
import nl.esciencecenter.xenon.engine.XenonEngine;

import org.junit.Test;

public class XenonEngineTest {

    @Test
    public void newEngine_NullProperties_Succeeds() throws Exception {
        Xenon x = XenonEngine.newXenon(null);
        assertEquals("Expected empty hashmap", new HashMap<String, String>(), x.getProperties());
    }

    @Test
    public void newEngine_EmptyProperties_Succeeds() throws Exception {
        HashMap<String, String> tmp = new HashMap<String, String>();
        Xenon x = XenonEngine.newXenon(tmp);
        assertEquals("Expected empty hashmap", tmp, x.getProperties());
    }

    @Test(expected = UnknownPropertyException.class)
    public void newEngine_UnknownProperties_ThrowsException() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("key", "value");
        XenonEngine.newXenon(properties);
    }

    @Test
    public void newEngine_CorrectProperties_Success() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("xenon.adaptors.ssh.loadKnownHosts", "false");
        XenonEngine.newXenon(properties);
    }

    @Test(expected = UnknownPropertyException.class)
    public void newEngine_CorrectPropertiesAtWrongLevel_ThrowsException() throws Exception {
        Map<String, String> properties = new HashMap<>();
        // This property is valid at scheduler level, not at xenon level 
        properties.put("xenon.adaptors.ssh.queue.pollingDelay", "1500");
        XenonEngine.newXenon(properties);
    }

    @Test
    public void newEngine_MultipleCorrectProperties_Success() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("xenon.adaptors.ssh.loadKnownHosts", "false");
        properties.put("xenon.adaptors.local.queue.pollingDelay", "1500");
        XenonEngine.newXenon(properties);
    }

    @Test(expected = UnknownPropertyException.class)
    public void newEngine_CorrectAndIncorrectProperties_ThrowsException() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("xenon.adaptors.ssh.loadKnownHosts", "false");
        properties.put("key", "value");
        XenonEngine.newXenon(properties);
    }

    @Test(expected = UnknownPropertyException.class)
    public void newEngine_PropertiesAtCorrectAndWrongLevel_ThrowsException() throws Exception {
        Map<String, String> properties = new HashMap<>();
        // These are xenon level properties
        properties.put("xenon.adaptors.ssh.loadKnownHosts", "false");
        properties.put("xenon.adaptors.local.queue.pollingDelay", "1500");
        // This property is valid at scheduler level, not at xenon level 
        properties.put("xenon.adaptors.ssh.queue.pollingDelay", "1500");
        XenonEngine.newXenon(properties);
    }

    @Test
    public void getAdaptorInfo_LocalAdaptor_Succeeds() throws Exception {
        XenonEngine x = (XenonEngine) XenonEngine.newXenon(null);
        AdaptorStatus adaptorInfo = x.getAdaptorStatus("local");
        assertEquals("The adaptor info for the local adaptor should contain the name \"local\"", "local", adaptorInfo.getName());
    }

    @Test
    public void getAdaptorInfo_UnknownAdaptor_ThrowsException() throws Exception {
        XenonEngine x = (XenonEngine) XenonEngine.newXenon(null);
        try {
            x.getAdaptorStatus("hupsefluts");
            fail();
        } catch (XenonException e) {
            assertEquals("engine adaptor: Could not find adaptor named hupsefluts", e.getMessage());
        }
    }

    @Test
    public void getAdaptorFor_LocalFile_Succeeds() throws Exception {
        XenonEngine x = (XenonEngine) XenonEngine.newXenon(null);
        Adaptor adaptor = x.getAdaptorFor("file");
        assertEquals("The local adaptor should be returned", "local", adaptor.getName());
    }

    @Test
    public void getAdaptorFor_UnknownScheme_ThrowsException() throws Exception {
        XenonEngine x = (XenonEngine) XenonEngine.newXenon(null);
        try {
            x.getAdaptorFor("hupsefluts");
            fail();
        } catch (XenonException e) {
            assertEquals("engine adaptor: Could not find adaptor for scheme hupsefluts", e.getMessage());
        }
    }

    @Test
    public void getAdaptor_LocalAdaptor_Succeeds() throws Exception {
        XenonEngine x = (XenonEngine) XenonEngine.newXenon(null);
        Adaptor adaptor = x.getAdaptor("local");
        assertEquals("The local adaptor should be returned", "local", adaptor.getName());
    }

    @Test
    public void getAdaptor_UnknownAdaptor_ThrowsException() throws Exception {
        XenonEngine x = (XenonEngine) XenonEngine.newXenon(null);
        try {
            x.getAdaptor("hupsefluts");
            fail();
        } catch (XenonException e) {
            assertEquals("engine adaptor: Could not find adaptor named hupsefluts", e.getMessage());
        }
    }

    //    @Test
    //    public void testGetAdaptors() throws Exception {
    //        XenonEngine xenon = (XenonEngine) XenonEngine.newXenon(null);
    //
    //        Adaptor[] tmp = xenon.getAdaptors();
    //
    //        assert (tmp != null);
    //        assert (tmp.length == 3);
    //    }

    @Test
    public void getAdaptorInfos_StandardAdaptors_Succeeds() throws Exception {

        // We currently have 8 adaptors:
        // local, ssh, gridengine, slurm, ftp, gftp, torque, webdav
        int count = 8;

        XenonEngine x = (XenonEngine) XenonEngine.newXenon(null);

        AdaptorStatus[] tmp = x.getAdaptorStatuses();

        assertNotNull("Expected AdaptorStatus array", tmp);
        assertTrue("Expected " + count + " adaptors to be returned", tmp.length == count);
    }

    @Test
    public void close_SingleXenon_Succeeds() throws Exception {
        XenonEngine x = (XenonEngine) XenonEngine.newXenon(null);
        XenonEngine.closeXenon(x);
    }

    @Test
    public void close_MultipleXenon_Succeeds() throws Exception {
        XenonEngine x1 = (XenonEngine) XenonEngine.newXenon(null);
        XenonEngine x2 = (XenonEngine) XenonEngine.newXenon(null);

        XenonEngine.closeXenon(x2);
        XenonEngine.closeXenon(x1);
    }

    @Test
    public void endAll_MultipleXenon_Succeeds() throws Exception {
        XenonEngine.newXenon(null);
        XenonEngine.newXenon(null);

        XenonEngine.endAll();
    }

    @Test(expected = XenonException.class)
    public void closenIvokedTwice_SingleXenon_ThrowsException() throws Exception {
        XenonEngine x1 = (XenonEngine) XenonEngine.newXenon(null);
        XenonEngine.closeXenon(x1);
        XenonEngine.closeXenon(x1);
    }

    @Test
    public void toString_XenonNoProperties_Succeeds() throws Exception {
        XenonEngine x1 = (XenonEngine) XenonEngine.newXenon(null);
        String tmp = x1.toString();

        // TODO; should check output of toString() ?
    }

}
