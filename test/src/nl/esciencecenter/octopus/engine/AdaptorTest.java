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
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;
import org.junit.Test;

import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.jobs.Jobs;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class AdaptorTest {

    class TestAdaptor extends Adaptor {

        public TestAdaptor(OctopusEngine octopusEngine, String name, String description, String[] supportedSchemes,
                String[][] defaultProperties, OctopusProperties properties) throws OctopusException {
            super(octopusEngine, name, description, supportedSchemes, defaultProperties, properties);
        }

        @Override
        public Map<String, String> getAdaptorSpecificInformation() {
            return null;
        }

        @Override
        public Files filesAdaptor() throws OctopusException {
            return null;
        }

        @Override
        public Jobs jobsAdaptor() throws OctopusException {
            return null;
        }

        @Override
        public Credentials credentialsAdaptor() throws OctopusException {
            return null;
        }

        @Override
        public void end() {
        }

    }

    @Test
    public void test0() throws OctopusException {

        String[] schemes = new String[] { "SCHEME1", "SCHEME2" };

        TestAdaptor t = new TestAdaptor(null, "test", "DESCRIPTION", schemes, null, new OctopusProperties());

        String[] tmp = t.getSupportedSchemes();

        assert (tmp != null);
        assert (Arrays.equals(schemes, tmp));
    }

    @Test
    public void test1() throws OctopusException {

        String[] schemes = new String[] { "SCHEME1", "SCHEME2" };
        String[][] defaultProperties =
                new String[][] { { "octopus.adaptors.test.p1", "aap1", "aap2" },
                        { "octopus.adaptors.test.p2", "noot1", "noot2" }, };

        TestAdaptor t = new TestAdaptor(null, "test", "DESCRIPTION", schemes, defaultProperties, new OctopusProperties());

        Map<String, String> p = t.getSupportedProperties();

        assert (p.get("octopus.adaptors.test.p1").equals("aap2"));
        assert (p.get("octopus.adaptors.test.p2").equals("noot2"));
    }

    @Test
    public void test2() throws OctopusException {

        String[] schemes = new String[] { "SCHEME1", "SCHEME2" };
        String[][] defaultProperties =
                new String[][] { { "octopus.adaptors.test.p1", "aap1", "aap2" },
                        { "octopus.adaptors.test.p2", "noot1", "noot2" }, };

        Properties p = new Properties();
        p.put("octopus.adaptors.test.p1", "mies");
        p.put("octopus.adaptors.test.p2", "zus");

        TestAdaptor t = new TestAdaptor(null, "test", "DESCRIPTION", schemes, defaultProperties, new OctopusProperties(p));

        Map<String, String> sp = t.getSupportedProperties();

        assert (sp.get("octopus.adaptors.test.p1").equals("mies"));
        assert (sp.get("octopus.adaptors.test.p2").equals("zus"));
    }

    @Test(expected = OctopusException.class)
    public void test3() throws OctopusException {

        String[] schemes = new String[] { "SCHEME1", "SCHEME2" };
        String[][] defaultProperties =
                new String[][] { { "octopus.adaptors.test.p1", "aap1", "aap2" },
                        { "octopus.adaptors.test.p2", "noot1", "noot2" }, };

        Properties p = new Properties();
        p.put("octopus.adaptors.test.p3", "mies");

        new TestAdaptor(null, "test", "DESCRIPTION", schemes, defaultProperties, new OctopusProperties(p));
    }

}
