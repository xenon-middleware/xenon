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
package nl.esciencecenter.octopus;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.util.Properties;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.exceptions.NoSuchOctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusException;

import org.junit.Test;

public class OctopusFactoryTest {

    @Test
    public void testNewOctopusFactory() throws Exception {
        // Test to satisfy coverage.
        Constructor<OctopusFactory> constructor = OctopusFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void testNewOctopus() throws OctopusException {
        Octopus octopus = OctopusFactory.newOctopus(null);
        assertTrue(octopus.getProperties().equals(new Properties()));
    }

    @Test
    public void testEndOctopus() throws OctopusException {
        Octopus octopus = OctopusFactory.newOctopus(null);
        OctopusFactory.endOctopus(octopus);
    }

    @Test(expected = NoSuchOctopusException.class)
    public void testEndOctopus2() throws OctopusException {
        Octopus octopus = OctopusFactory.newOctopus(null);
        OctopusFactory.endOctopus(octopus);
        OctopusFactory.endOctopus(octopus);
    }

    @Test(expected = NoSuchOctopusException.class)
    public void testEndAll() throws OctopusException {
        Octopus octopus1 = OctopusFactory.newOctopus(null);
        Octopus octopus2 = OctopusFactory.newOctopus(null);

        OctopusFactory.endAll();
        OctopusFactory.endOctopus(octopus1);
        OctopusFactory.endOctopus(octopus2);        
    }
}
