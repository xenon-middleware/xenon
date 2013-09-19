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
package nl.esciencecenter.xenon;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.util.Properties;


import nl.esciencecenter.xenon.Cobalt;
import nl.esciencecenter.xenon.CobaltException;
import nl.esciencecenter.xenon.CobaltFactory;
import nl.esciencecenter.xenon.NoSuchCobaltException;

import org.junit.Test;

public class CobaltFactoryTest {

    @Test
    public void testNewOctopusFactory() throws Exception {
        // Test to satisfy coverage.
        Constructor<CobaltFactory> constructor = CobaltFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void testNewOctopus() throws CobaltException {
        Cobalt octopus = CobaltFactory.newCobalt(null);
        assertTrue(octopus.getProperties().equals(new Properties()));
    }

    @Test
    public void testEndOctopus() throws CobaltException {
        Cobalt octopus = CobaltFactory.newCobalt(null);
        CobaltFactory.endCobalt(octopus);
    }

    @Test(expected = NoSuchCobaltException.class)
    public void testEndOctopus2() throws CobaltException {
        Cobalt octopus = CobaltFactory.newCobalt(null);
        CobaltFactory.endCobalt(octopus);
        CobaltFactory.endCobalt(octopus);
    }

    @Test(expected = NoSuchCobaltException.class)
    public void testEndAll() throws CobaltException {
        Cobalt octopus1 = CobaltFactory.newCobalt(null);
        Cobalt octopus2 = CobaltFactory.newCobalt(null);

        CobaltFactory.endAll();
        CobaltFactory.endCobalt(octopus1);
        CobaltFactory.endCobalt(octopus2);        
    }
}
