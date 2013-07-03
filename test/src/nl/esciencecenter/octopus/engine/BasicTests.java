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

import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.*;

import nl.esciencecenter.octopus.AdaptorStatus;
import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;

public class BasicTests {

    @org.junit.Test
    public void test1() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);
        OctopusFactory.endOctopus(octopus);
    }

    @org.junit.Test
    public void test2() throws Exception {

        Properties properties = new Properties();

        properties.setProperty("some.key", "some.value");

        Octopus octopus = OctopusFactory.newOctopus(properties);

        assertEquals(octopus.getProperties().get("some.key"), "some.value");
        
        OctopusFactory.endOctopus(octopus);
        
    }

    @org.junit.Test
    public void test3() throws Exception {

        Octopus octopus = OctopusFactory.newOctopus(null);

        //test if the local adaptor exists
        AdaptorStatus localInfo = octopus.getAdaptorInfo("local");

        System.out.println(localInfo.getName());
        System.out.println(localInfo.getDescription());
        System.out.println(Arrays.toString(localInfo.getSupportedSchemes()));
        System.out.println(localInfo.getSupportedProperties());

        OctopusFactory.endOctopus(octopus);        
    }
}
