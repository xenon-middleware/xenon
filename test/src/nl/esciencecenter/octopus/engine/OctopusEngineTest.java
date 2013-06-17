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
import static org.hamcrest.CoreMatchers.*;

import java.util.Properties;

import nl.esciencecenter.octopus.AdaptorStatus;
import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusException;

import org.junit.Test;

public class OctopusEngineTest {

    @Test
    public void testNewEngineWithNulls() throws OctopusException {
        Octopus octopus = OctopusEngine.newOctopus(null);
        assertThat(octopus.getProperties(), is(new Properties()));
    }

    @Test
    public void testNewEngineWithWithProperties() throws OctopusException {
        Properties properties = new Properties();
        properties.setProperty("key", "value");
        Octopus octopus = OctopusEngine.newOctopus(properties);
        assertThat(octopus.getProperties(), is(properties));
    }

    public OctopusEngine getEngineWithOnlyLocalAdaptor() throws OctopusException {
        Properties properties = new Properties();
        properties.setProperty("octopus.adaptors.load", "local");
        Octopus octopus = null;
        octopus = OctopusEngine.newOctopus(properties);
        return (OctopusEngine) octopus;
    }

    @Test
    public void testGetAdaptorInfo() throws OctopusException {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newOctopus(null);
        AdaptorStatus adaptorInfo = octopus.getAdaptorInfo("local");
        assertThat(adaptorInfo.getName(), is("local"));
    }

    @Test
    public void testGetAdaptorInfo_UnknownAdaptor() throws OctopusException {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newOctopus(null);
        try {
            octopus.getAdaptorInfo("hupsefluts");
            fail();
        } catch (OctopusException e) {
            assertThat(e.getMessage(), is("engine adaptor: Could not find adaptor named hupsefluts"));
        }
    }

    @Test
    public void testGetAdaptorFor() throws OctopusException {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newOctopus(null);
        Adaptor adaptor = octopus.getAdaptorFor("file");
        assertThat(adaptor.getName(), is("local"));
    }

    @Test
    public void testGetAdaptorFor_UnknownScheme() throws OctopusException {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newOctopus(null);
        try {
            octopus.getAdaptorFor("hupsefluts");
            fail();
        } catch (OctopusException e) {
            assertThat(e.getMessage(), is("engine adaptor: Could not find adaptor for scheme hupsefluts"));
        }
    }

    @Test
    public void testGetAdaptor() throws OctopusException {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newOctopus(null);
        Adaptor adaptor = octopus.getAdaptor("local");
        assertThat(adaptor.getName(), is("local"));
    }

    @Test
    public void testGetAdaptor_UnknownAdaptor() throws OctopusException {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newOctopus(null);
        try {
            octopus.getAdaptor("hupsefluts");
            fail();
        } catch (OctopusException e) {
            assertThat(e.getMessage(), is("engine adaptor: Could not find adaptor named hupsefluts"));
        }
    }

    @Test
    public void testGetAdaptors() throws OctopusException {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newOctopus(null);
 
        Adaptor [] tmp = octopus.getAdaptors();
        
        assert(tmp != null);
        assert(tmp.length == 3);
    }

    @Test
    public void testGetAdaptorInfos() throws OctopusException {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newOctopus(null);
 
        AdaptorStatus [] tmp = octopus.getAdaptorInfos();
        
        assert(tmp != null);
        assert(tmp.length == 3);
    }

    @Test
    public void testClose() throws OctopusException {
        OctopusEngine octopus = (OctopusEngine) OctopusEngine.newOctopus(null);
        OctopusEngine.closeOctopus(octopus);
    }  
      
    @Test
    public void testCloseMultiple() throws OctopusException {
        OctopusEngine octopus1 = (OctopusEngine) OctopusEngine.newOctopus(null);
        OctopusEngine octopus2 = (OctopusEngine) OctopusEngine.newOctopus(null);
        
        OctopusEngine.closeOctopus(octopus2);
        OctopusEngine.closeOctopus(octopus1);
    }  

    @Test
    public void testEndAll() throws OctopusException {
        OctopusEngine octopus1 = (OctopusEngine) OctopusEngine.newOctopus(null);
        OctopusEngine octopus2 = (OctopusEngine) OctopusEngine.newOctopus(null);
        
        OctopusEngine.endAll();
    }  

    @Test
    public void testEnd() throws OctopusException {
        OctopusEngine octopus1 = (OctopusEngine) OctopusEngine.newOctopus(null);
        octopus1.end();
    }
   
    @Test
    public void testEndTwice() throws OctopusException {
        OctopusEngine octopus1 = (OctopusEngine) OctopusEngine.newOctopus(null);
        octopus1.end();
        octopus1.end();
    }
    
    @Test(expected = OctopusException.class)
    public void testCloseTwice() throws OctopusException {
        OctopusEngine octopus1 = (OctopusEngine) OctopusEngine.newOctopus(null);
        
        OctopusEngine.closeOctopus(octopus1);
        OctopusEngine.closeOctopus(octopus1);
    }  

    @Test
    public void testToString() throws OctopusException {
        OctopusEngine octopus1 = (OctopusEngine) OctopusEngine.newOctopus(null);
        String tmp = octopus1.toString();

        // TODO; should check output of toString() ?
    }
        
}


