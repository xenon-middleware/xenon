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

import java.util.Map;

import nl.esciencecenter.octopus.OctopusPropertyDescription;
import nl.esciencecenter.octopus.engine.util.ImmutableArray;

import org.junit.Test;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class AdaptorStatusImplementationTest {

    @Test
    public void testGetters() {

        AdaptorStatusImplementation a = new AdaptorStatusImplementation("NAME", "DESCRIPTION", 
                new ImmutableArray<String>("SCHEME1", "SCHEME2"),new ImmutableArray<String>("L1", "L2"), 
                new ImmutableArray<OctopusPropertyDescription>(), null);

        String name = a.getName();

        assert (name.equals("NAME"));

        String description = a.getDescription();

        assert (description.equals("DESCRIPTION"));

        String[] schemes = a.getSupportedSchemes();

        assert (schemes != null);
        assert (schemes.length == 2);
        assert (schemes[0].equals("SCHEME1"));
        assert (schemes[1].equals("SCHEME2"));

        String[] locations = a.getSupportedLocations();

        assert (locations != null);
        assert (locations.length == 2);
        assert (locations[0].equals("L1"));
        assert (locations[1].equals("L2"));
        
        OctopusPropertyDescription[] props = a.getSupportedProperties();

        assert (props == null);

        Map<String, String> info = a.getAdaptorSpecificInformation();

        assert (info == null);
    }

    @Test
    public void testToString() {

        String tmp = new AdaptorStatusImplementation("NAME", "DESCRIPTION", new ImmutableArray<String>("SCHEME1", "SCHEME2"), 
                new ImmutableArray<String>("L1", "L2"), new ImmutableArray<OctopusPropertyDescription>(), null).toString();

        assert (tmp.equals("AdaptorStatusImplementation [name=NAME, description=DESCRIPTION, supportedSchemes=[SCHEME1, " +
        		"SCHEME2], supportedProperties=[], adaptorSpecificInformation=null]"));
    }

}
