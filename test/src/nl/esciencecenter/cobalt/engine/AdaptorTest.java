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

package nl.esciencecenter.cobalt.engine;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.cobalt.CobaltException;
import nl.esciencecenter.cobalt.CobaltPropertyDescription;
import nl.esciencecenter.cobalt.CobaltPropertyDescription.Component;
import nl.esciencecenter.cobalt.CobaltPropertyDescription.Type;
import nl.esciencecenter.cobalt.credentials.Credentials;
import nl.esciencecenter.cobalt.engine.Adaptor;
import nl.esciencecenter.cobalt.engine.CobaltEngine;
import nl.esciencecenter.cobalt.engine.CobaltProperties;
import nl.esciencecenter.cobalt.engine.CobaltPropertyDescriptionImplementation;
import nl.esciencecenter.cobalt.engine.util.ImmutableArray;
import nl.esciencecenter.cobalt.files.Files;
import nl.esciencecenter.cobalt.jobs.Jobs;

import org.junit.Test;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class AdaptorTest {

    class TestAdaptor extends Adaptor {

        public TestAdaptor(CobaltEngine octopusEngine, String name, String description, ImmutableArray<String> supportedSchemes,
                ImmutableArray<String> supportedLocations, ImmutableArray<CobaltPropertyDescription> validProperties, 
                CobaltProperties p) throws CobaltException {

            super(octopusEngine, name, description, supportedSchemes, supportedLocations, validProperties, p);
        }

        @Override
        public Map<String, String> getAdaptorSpecificInformation() {
            return null;
        }

        @Override
        public Files filesAdaptor() throws CobaltException {
            return null;
        }

        @Override
        public Jobs jobsAdaptor() throws CobaltException {
            return null;
        }

        @Override
        public Credentials credentialsAdaptor() throws CobaltException {
            return null;
        }

        @Override
        public void end() {
        }

    }

    @Test
    public void test0() throws CobaltException {

        ImmutableArray<String> schemes = new ImmutableArray<String>("SCHEME1", "SCHEME2");
        ImmutableArray<String> locations = new ImmutableArray<String>("L1", "L2");

        TestAdaptor t = new TestAdaptor(null, "test", "DESCRIPTION", schemes, locations, 
                new ImmutableArray<CobaltPropertyDescription>(), new CobaltProperties());

        String[] tmp = t.getSupportedSchemes();

        assert (tmp != null);
        assert (Arrays.equals(schemes.asArray(), tmp));
    }

    @Test
    public void test1() throws CobaltException {

        ImmutableArray<String> schemes = new ImmutableArray<String>("SCHEME1", "SCHEME2");
        ImmutableArray<String> locations = new ImmutableArray<String>("L1", "L2");

        ImmutableArray<CobaltPropertyDescription> supportedProperties = new ImmutableArray<CobaltPropertyDescription>(
                new CobaltPropertyDescriptionImplementation("octopus.adaptors.test.p1", Type.STRING, EnumSet.of(Component.COBALT),
                        "aap2", "test property p1"),

                new CobaltPropertyDescriptionImplementation("octopus.adaptors.test.p2", Type.STRING, EnumSet.of(Component.COBALT),
                        "noot2", "test property p2"));

        CobaltProperties prop = new CobaltProperties(supportedProperties, new HashMap<String, String>());
        TestAdaptor t = new TestAdaptor(null, "test", "DESCRIPTION", schemes, locations, supportedProperties, prop);

        CobaltPropertyDescription[] p = t.getSupportedProperties();

        assert (p != null);
        assert (p.length == 2);
        assert (p[0].getName().equals("octopus.adaptors.test.p1"));
        assert (p[0].getDefaultValue().equals("aap2"));
        assert (p[1].getName().equals("octopus.adaptors.test.p2"));
        assert (p[1].getDefaultValue().equals("noot2"));
    }

    @Test
    public void test2() throws CobaltException {

        ImmutableArray<String> schemes = new ImmutableArray<String>("SCHEME1", "SCHEME2");
        ImmutableArray<String> locations = new ImmutableArray<String>("L1", "L2");

        ImmutableArray<CobaltPropertyDescription> supportedProperties = new ImmutableArray<CobaltPropertyDescription>(
                new CobaltPropertyDescriptionImplementation("octopus.adaptors.test.p1", Type.STRING, EnumSet.of(Component.COBALT),
                        "aap2", "test property p1"),

                new CobaltPropertyDescriptionImplementation("octopus.adaptors.test.p2", Type.STRING, EnumSet.of(Component.COBALT),
                        "noot2", "test property p2"));

        Map<String, String> m = new HashMap<>();
        m.put("octopus.adaptors.test.p1", "mies");
        m.put("octopus.adaptors.test.p2", "zus");

        CobaltProperties prop = new CobaltProperties(supportedProperties, new HashMap<String, String>());
        TestAdaptor t = new TestAdaptor(null, "test", "DESCRIPTION", schemes, locations, supportedProperties, prop);

        CobaltPropertyDescription[] p = t.getSupportedProperties();

        assert (p != null);
        assert (p.length == 2);
        assert (p[0].getName().equals("octopus.adaptors.test.p1"));
        assert (p[0].getDefaultValue().equals("mies"));
        assert (p[1].getName().equals("octopus.adaptors.test.p2"));
        assert (p[1].getDefaultValue().equals("zus"));
    }

    @Test(expected = CobaltException.class)
    public void test3() throws CobaltException {

        ImmutableArray<String> schemes = new ImmutableArray<String>("SCHEME1", "SCHEME2");
        ImmutableArray<String> locations = new ImmutableArray<String>("L1", "L2");

        ImmutableArray<CobaltPropertyDescription> supportedProperties = new ImmutableArray<CobaltPropertyDescription>(
                new CobaltPropertyDescriptionImplementation("octopus.adaptors.test.p1", Type.STRING, EnumSet.of(Component.COBALT),
                        "aap2", "test property p1"),

                new CobaltPropertyDescriptionImplementation("octopus.adaptors.test.p2", Type.STRING, EnumSet.of(Component.COBALT),
                        "noot2", "test property p2"));

        Map<String, String> p = new HashMap<>();
        p.put("octopus.adaptors.test.p3", "mies");

        CobaltProperties prop = new CobaltProperties(supportedProperties, p);

        new TestAdaptor(null, "test", "DESCRIPTION", schemes, locations, supportedProperties, prop);
    }

}
