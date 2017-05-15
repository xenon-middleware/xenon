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
package nl.esciencecenter.xenon.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.engine.AdaptorStatusImplementation;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;

import org.junit.Test;

/**
 * 
 */
public class AdaptorStatusImplementationTest {

    @Test
    public void testGetters1() {

        AdaptorStatusImplementation a = new AdaptorStatusImplementation("NAME", "DESCRIPTION", 
                new ImmutableArray<>("SCHEME1"), new ImmutableArray<>("SCHEME2"), new ImmutableArray<>("L1", "L2"), 
                new ImmutableArray<XenonPropertyDescription>(), null);

        assertEquals("NAME", a.getName());
        assertEquals("DESCRIPTION", a.getDescription());

        String[] schemes = a.getSupportedSchemes();

        assertNotNull(schemes);
        assertEquals(2, schemes.length);
        assertEquals("SCHEME1", schemes[0]);
        assertEquals("SCHEME2", schemes[1]);

        String[] jobschemes = a.getSupportedJobSchemes();

        assertNotNull(jobschemes);
        assertEquals(1, jobschemes.length);
        assertEquals("SCHEME1", jobschemes[0]);

        String[] fileschemes = a.getSupportedFileSchemes();

        assertNotNull(fileschemes);
        assertEquals(1, fileschemes.length);
        assertEquals("SCHEME2", fileschemes[0]);

        String[] locations = a.getSupportedLocations();

        assertNotNull(locations);
        assertEquals(2, locations.length);
        assertEquals("L1", locations[0]);
        assertEquals("L2", locations[1]);
        
        assertEquals(0, a.getSupportedProperties().length);
        assertNull(a.getAdaptorSpecificInformation());
    }

    @Test
    public void testToString() {

        String tmp = new AdaptorStatusImplementation("NAME", "DESCRIPTION", new ImmutableArray<>("SCHEME1", "SCHEME2"), null,
                new ImmutableArray<>("L1", "L2"), new ImmutableArray<XenonPropertyDescription>(), null).toString();
        
        assert (tmp.equals("AdaptorStatusImplementation [name=NAME, description=DESCRIPTION, supportedJobSchemes=[SCHEME1, " +
        		"SCHEME2], supportedFileSchemes=[], supportedProperties=[], adaptorSpecificInformation=null]"));
    }

}
