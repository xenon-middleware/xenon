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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Component;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.jobs.Jobs;
import nl.esciencecenter.xenon.util.Utils;

import org.junit.Test;

/**
 * 
 */
public class AdaptorTest {

    class TestAdaptor extends Adaptor {

        public TestAdaptor(XenonEngine xenonEngine, String name, String description, ImmutableArray<String> supportedJobSchemes,
                ImmutableArray<String> supportedFileSchemes, ImmutableArray<String> supportedLocations, 
                ImmutableArray<XenonPropertyDescription> validProperties, XenonProperties p) throws XenonException {

            super(xenonEngine, name, description, supportedJobSchemes, supportedFileSchemes, supportedLocations, validProperties,
                    p);
        }

        @Override
        public Map<String, String> getAdaptorSpecificInformation() {
            return null;
        }

        @Override
        public Files filesAdaptor() throws XenonException {
            return null;
        }

        @Override
        public Jobs jobsAdaptor() throws XenonException {
            return null;
        }

        @Override
        public Credentials credentialsAdaptor() throws XenonException {
            return null;
        }

        @Override
        public void end() { /* noop */ }

        /**
         * @return
         */
        public String[] getSupportedJobSchemes() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    @Test
    public void testJobScheme1() throws XenonException {

        ImmutableArray<String> schemesJob = new ImmutableArray<>("SCHEME1", "SCHEME2");
        ImmutableArray<String> locations = new ImmutableArray<>("L1", "L2");

        TestAdaptor t = new TestAdaptor(null, "test", "DESCRIPTION", schemesJob, null, locations, 
                new ImmutableArray<XenonPropertyDescription>(), new XenonProperties());

        String[] tmp = t.getSupportedSchemes();
        
        assert (tmp != null);
        assert (Arrays.equals(schemesJob.asArray(), tmp));
    }

    @Test
    public void testJobScheme2() throws XenonException {

        ImmutableArray<String> schemesJob = new ImmutableArray<>("SCHEME1", "SCHEME2");
        ImmutableArray<String> locations = new ImmutableArray<>("L1", "L2");

        TestAdaptor t = new TestAdaptor(null, "test", "DESCRIPTION", schemesJob, null, locations, 
                new ImmutableArray<XenonPropertyDescription>(), new XenonProperties());

        String[] tmp = t.getSupportedJobSchemes();
        
        assert (tmp != null);
        assert (Arrays.equals(schemesJob.asArray(), tmp));
    }

    @Test
    public void testJobScheme3() throws XenonException {

        ImmutableArray<String> schemesJob = new ImmutableArray<>("SCHEME1", "SCHEME2");
        ImmutableArray<String> locations = new ImmutableArray<>("L1", "L2");

        TestAdaptor t = new TestAdaptor(null, "test", "DESCRIPTION", schemesJob, null, locations, 
                new ImmutableArray<XenonPropertyDescription>(), new XenonProperties());

        String[] tmp = t.getSupportedFileSchemes();
        
        assert (tmp != null);
        assert (Arrays.equals(new String[0], tmp));
    }

    @Test
    public void testFileScheme1() throws XenonException {

        ImmutableArray<String> schemesFile = new ImmutableArray<>("SCHEME1", "SCHEME2");
        ImmutableArray<String> locations = new ImmutableArray<>("L1", "L2");

        TestAdaptor t = new TestAdaptor(null, "test", "DESCRIPTION", null, schemesFile, locations, 
                new ImmutableArray<XenonPropertyDescription>(), new XenonProperties());

        String[] tmp = t.getSupportedSchemes();
        
        assert (tmp != null);
        assert (Arrays.equals(schemesFile.asArray(), tmp));
    }

    @Test
    public void testFileScheme2() throws XenonException {

        ImmutableArray<String> schemesFile = new ImmutableArray<>("SCHEME1", "SCHEME2");
        ImmutableArray<String> locations = new ImmutableArray<>("L1", "L2");

        TestAdaptor t = new TestAdaptor(null, "test", "DESCRIPTION", null, schemesFile,locations, 
                new ImmutableArray<XenonPropertyDescription>(), new XenonProperties());

        String[] tmp = t.getSupportedFileSchemes();
        
        assert (tmp != null);
        assert (Arrays.equals(schemesFile.asArray(), tmp));
    }

    @Test
    public void testFileScheme3() throws XenonException {

        ImmutableArray<String> schemesFile = new ImmutableArray<>("SCHEME1", "SCHEME2");
        ImmutableArray<String> locations = new ImmutableArray<>("L1", "L2");

        TestAdaptor t = new TestAdaptor(null, "test", "DESCRIPTION", null, schemesFile, locations, 
                new ImmutableArray<XenonPropertyDescription>(), new XenonProperties());

        String[] tmp = t.getSupportedFileSchemes();
        
        assert (tmp != null);
        assert (Arrays.equals(new String[0], tmp));
    }

    @Test
    public void testJobAndFileScheme1() throws XenonException {

        ImmutableArray<String> schemesJob = new ImmutableArray<>("SCHEME1");
        ImmutableArray<String> schemesFile = new ImmutableArray<>("SCHEME2");
        ImmutableArray<String> locations = new ImmutableArray<>("L1", "L2");

        TestAdaptor t = new TestAdaptor(null, "test", "DESCRIPTION", schemesJob, schemesFile, locations, 
                new ImmutableArray<XenonPropertyDescription>(), new XenonProperties());

        String[] tmp = t.getSupportedSchemes();
        String[] result = new String[] { "SCHEME1", "SCHEME2" };         
        assert (tmp != null);
        assert (Arrays.equals(result, tmp));
    }

    
    @Test
    public void testJobAndFileScheme2() throws XenonException {

        ImmutableArray<String> schemesJob = new ImmutableArray<>("SCHEME1");
        ImmutableArray<String> schemesFile = new ImmutableArray<>("SCHEME2");
        ImmutableArray<String> locations = new ImmutableArray<>("L1", "L2");

        TestAdaptor t = new TestAdaptor(null, "test", "DESCRIPTION", schemesJob, schemesFile, locations, 
                new ImmutableArray<XenonPropertyDescription>(), new XenonProperties());

        String[] tmp = t.getSupportedJobSchemes();
         
        assert (tmp != null);
        assert (Arrays.equals(schemesJob.asArray(), tmp));
    }

    
    @Test
    public void testJobAndFileScheme3() throws XenonException {

        ImmutableArray<String> schemesJob = new ImmutableArray<>("SCHEME1");
        ImmutableArray<String> schemesFile = new ImmutableArray<>("SCHEME2");
        ImmutableArray<String> locations = new ImmutableArray<>("L1", "L2");

        TestAdaptor t = new TestAdaptor(null, "test", "DESCRIPTION", schemesJob, schemesFile, locations, 
                new ImmutableArray<XenonPropertyDescription>(), new XenonProperties());

        String[] tmp = t.getSupportedFileSchemes();
         
        assert (tmp != null);
        assert (Arrays.equals(schemesFile.asArray(), tmp));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJobAndFileSchemeNull() throws XenonException {

        ImmutableArray<String> locations = new ImmutableArray<>("L1", "L2");

        TestAdaptor t = new TestAdaptor(null, "test", "DESCRIPTION", null, null, locations, 
                new ImmutableArray<XenonPropertyDescription>(), new XenonProperties());
    }
    
    @Test
    public void testSupportedProperties1() throws XenonException {

        ImmutableArray<String> schemes = new ImmutableArray<>("SCHEME1", "SCHEME2");
        ImmutableArray<String> locations = new ImmutableArray<>("L1", "L2");

        ImmutableArray<XenonPropertyDescription> supportedProperties = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescriptionImplementation("xenon.adaptors.test.p1", Type.STRING, EnumSet.of(Component.XENON),
                        "aap2", "test property p1"),

                new XenonPropertyDescriptionImplementation("xenon.adaptors.test.p2", Type.STRING, EnumSet.of(Component.XENON),
                        "noot2", "test property p2"));

        XenonProperties prop = new XenonProperties(supportedProperties, new HashMap<String, String>(0));
        TestAdaptor t = new TestAdaptor(null, "test", "DESCRIPTION", schemes, null, locations, supportedProperties, prop);

        XenonPropertyDescription[] p = t.getSupportedProperties();

        assert (p != null);
        assert (p.length == 2);
        assert (p[0].getName().equals("xenon.adaptors.test.p1"));
        assert (p[0].getDefaultValue().equals("aap2"));
        assert (p[1].getName().equals("xenon.adaptors.test.p2"));
        assert (p[1].getDefaultValue().equals("noot2"));
        XenonProperties props = t.getProperties();
        assertEquals(p[0].getDefaultValue(), props.getProperty(p[0].getName()));
        assertEquals(p[1].getDefaultValue(), props.getProperty(p[1].getName()));
    }

    @Test
    public void testSupportedProperties2() throws XenonException {
        ImmutableArray<String> schemes = new ImmutableArray<>("SCHEME1", "SCHEME2");
        ImmutableArray<String> locations = new ImmutableArray<>("L1", "L2");

        ImmutableArray<XenonPropertyDescription> supportedProperties = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescriptionImplementation("xenon.adaptors.test.p1", Type.STRING, EnumSet.of(Component.XENON),
                        "aap2", "test property p1"),
                new XenonPropertyDescriptionImplementation("xenon.adaptors.test.p2", Type.STRING, EnumSet.of(Component.XENON),
                        "noot2", "test property p2"));

        Map<String, String> m = new HashMap<>(3);
        m.put("xenon.adaptors.test.p1", "mies");
        m.put("xenon.adaptors.test.p2", "zus");

        XenonProperties prop = new XenonProperties(supportedProperties, m);
        TestAdaptor t = new TestAdaptor(null, "test", "DESCRIPTION", schemes, null, locations, supportedProperties, prop);

        XenonPropertyDescription[] p = t.getSupportedProperties();

        assertNotNull(p);
        assertEquals(2, p.length);
        assertEquals("xenon.adaptors.test.p1", p[0].getName());
        assertEquals("aap2", p[0].getDefaultValue());
        assertEquals("xenon.adaptors.test.p2", p[1].getName());
        assertEquals("noot2", p[1].getDefaultValue());

        XenonProperties props = t.getProperties();
        assertEquals("mies", props.getProperty(p[0].getName()));
        assertEquals("zus", props.getProperty(p[1].getName()));
}

    @Test(expected = XenonException.class)
    public void testSupportedPropertiesFails() throws XenonException {

        ImmutableArray<String> schemes = new ImmutableArray<>("SCHEME1", "SCHEME2");
        ImmutableArray<String> locations = new ImmutableArray<>("L1", "L2");

        ImmutableArray<XenonPropertyDescription> supportedProperties = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescriptionImplementation("xenon.adaptors.test.p1", Type.STRING, EnumSet.of(Component.XENON),
                        "aap2", "test property p1"),

                new XenonPropertyDescriptionImplementation("xenon.adaptors.test.p2", Type.STRING, EnumSet.of(Component.XENON),
                        "noot2", "test property p2"));

        Map<String, String> p = new HashMap<>(2);
        p.put("xenon.adaptors.test.p3", "mies");

        XenonProperties prop = new XenonProperties(supportedProperties, p);

        new TestAdaptor(null, "test", "DESCRIPTION", schemes, null, locations, supportedProperties, prop);
    }

}
