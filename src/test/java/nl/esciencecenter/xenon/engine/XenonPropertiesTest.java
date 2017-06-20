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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.InvalidPropertyException;
import nl.esciencecenter.xenon.UnknownPropertyException;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;
import nl.esciencecenter.xenon.engine.util.ImmutableArray;

import org.junit.Test;

public class XenonPropertiesTest {

    @Test
    public void testXenonProperties_supportsProperty_propertySet_true() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.STRING, "bla", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "value");

        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.supportsProperty("key"));
    }

    @Test
    public void testXenonProperties_supportsProperty_useDefault_true() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.STRING,  "bla", 
                        "test property"));

        Map<String, String> props = new HashMap<>(0);
        
        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.supportsProperty("key"));
    }

    @Test
    public void testXenonProperties_supportsProperty_propertySet_false() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.STRING,  "bla", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "value");

        XenonProperties xprop = new XenonProperties(valid, props);

        assertFalse(xprop.supportsProperty("aap"));
    }

    @Test
    public void testXenonProperties_supportsProperty_useDefault_false() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.STRING, "bla", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        
        XenonProperties xprop = new XenonProperties(valid, props);

        assertFalse(xprop.supportsProperty("aap"));
    }
    
    @Test
    public void testXenonProperties_propertySet_true() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.STRING, "bla", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "value");

        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.propertySet("key"));
    }

    @Test
    public void testXenonProperties_propertySet_false() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.STRING, "bla", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        
        XenonProperties xprop = new XenonProperties(valid, props);

        assertFalse(xprop.propertySet("key"));
    }
    
    @Test(expected = UnknownPropertyException.class)
    public void testXenonProperties_propertySet_fails() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.STRING, "bla", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        XenonProperties xprop = new XenonProperties(valid, props);

        assertFalse(xprop.propertySet("aap"));
    }
 

    @Test
    public void testXenonProperties_getProperty_propertySet() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.STRING, "bla", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "value");

        XenonProperties xprop = new XenonProperties(valid, props);

        assertEquals("value", xprop.getProperty("key"));
    }

    @Test
    public void testXenonProperties_getProperty_default() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.STRING, "bla", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);

        XenonProperties xprop = new XenonProperties(valid, props);

        assertEquals("bla", xprop.getProperty("key"));
    }

    @Test(expected = UnknownPropertyException.class)
    public void testXenonProperties_getProperty_fails() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.STRING, "bla", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "value");

        XenonProperties xprop = new XenonProperties(valid, props);
        xprop.getProperty("aap"); // throws exception
    }
    
    @Test
    public void testXenonProperties_fromProperties() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.STRING, "bla", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "value");

        XenonProperties xprop = new XenonProperties(valid, props);

        assertEquals("{key=value}", xprop.toString());
    }

    @Test(expected = UnknownPropertyException.class)
    public void testXenonProperties_fromDefaultsAndProperties_noOverlap() throws Exception {
        
        ImmutableArray<XenonPropertyDescription> supportedProperties = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.STRING, "value", 
                        "test property"));
        
        Map<String, String> props = new HashMap<>(2);
        props.put("key2", "value2");

        new XenonProperties(supportedProperties, props);
    }

    @Test
    public void testXenonProperties_fromDefaultsAndProperties_withOverlap() throws Exception {
        
        ImmutableArray<XenonPropertyDescription> supportedProperties = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.STRING, "value",
                        "test property"),
                new XenonPropertyDescription("key2", Type.STRING, "value",
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        props.put("key2", "value2");

        XenonProperties xprop = new XenonProperties(supportedProperties, props);

        assertEquals("{key2=value2, <<key=value>>}", xprop.toString());
    }

    @Test
    public void testGetBooleanProperty_true() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.BOOLEAN, "true", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "true");
        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.getBooleanProperty("key"));
    }

    @Test
    public void testGetBooleanProperty_false() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.BOOLEAN, "true", 
                        "test property"));
        
        Map<String, String> props = new HashMap<>(2);
        props.put("key", "false");
        XenonProperties xprop = new XenonProperties(valid, props);

        assertFalse(xprop.getBooleanProperty("key"));
    }

    @Test
    public void testGetBooleanProperty_default() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.BOOLEAN, "true", 
                        "test property"));
        
        Map<String, String> props = new HashMap<>(2);
        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.getBooleanProperty("key"));
    }

    @Test(expected = InvalidPropertyException.class)
    public void testGetBooleanProperty_emptyString_False() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.BOOLEAN, "true", 
                        "test property"));
        
        Map<String, String> props = new HashMap<>(2);
        props.put("key", "bla");
        new XenonProperties(valid, props);
    }

    @Test(expected = InvalidPropertyException.class)
    public void testGetBooleanProperty_invalidDefault() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.BOOLEAN, "aap", 
                        "test property"));
        
        Map<String, String> props = new HashMap<>(2);
        new XenonProperties(valid, props).getBooleanProperty("key");
    }

    @Test(expected = UnknownPropertyException.class)
    public void testGetBooleanProperty_invalidName() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.BOOLEAN, "true", 
                        "test property"));
        
        Map<String, String> props = new HashMap<>(2);
        new XenonProperties(valid, props).getBooleanProperty("noot");
    }
    
    @Test(expected = PropertyTypeException.class)
    public void testGetBooleanProperty_wrongType() throws Exception {

        ImmutableArray<XenonPropertyDescription> supportedProperties = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.STRING, "value", 
                        "test property"));
        
        Map<String, String> props = new HashMap<>(2);
        XenonProperties xprop = new XenonProperties(supportedProperties, props);
        xprop.getBooleanProperty("key"); // throws exception        
    }
    
    @Test
    public void testGetIntProperty_1() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.INTEGER, "42", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1");
        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.getIntegerProperty("key") == 1);
    }

    @Test
    public void testGetIntProperty_default() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.INTEGER, "42", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.getIntegerProperty("key") == 42);
    }

    @Test(expected = InvalidPropertyException.class)
    public void testGetIntProperty_invalidDefault() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.INTEGER, "aap", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        new XenonProperties(valid, props).getIntegerProperty("key"); // throws exception
    }

    @Test
    public void testGetDoubleProperty_1() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.DOUBLE, "42.0", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1.0");
        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.getDoubleProperty("key") == 1.0);
    }

    @Test
    public void testGetDoubleProperty_default() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.DOUBLE, "42.0", 
                        "test property"));

        Map<String, String> props = new HashMap<>(0);
        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.getDoubleProperty("key") == 42.0);
    }

    @Test(expected = InvalidPropertyException.class)
    public void testGetDoubleProperty_invalidDefault() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.DOUBLE, "aap", 
                        "test property"));

        Map<String, String> props = new HashMap<>(0);
        new XenonProperties(valid, props).getDoubleProperty("key"); // throws exception
    }

    
    @Test
    public void testGetSizeProperty_g() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.SIZE, "42g", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1g");
        XenonProperties xprop = new XenonProperties(valid, props);

        //noinspection PointlessArithmeticExpression
        assertTrue(xprop.getSizeProperty("key") == 1L*1024L*1024L*1024L);
    }
    
    @Test
    public void testGetSizeProperty_G() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.SIZE, "42g", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1G");
        XenonProperties xprop = new XenonProperties(valid, props);

        //noinspection PointlessArithmeticExpression
        assertTrue(xprop.getSizeProperty("key") == 1L*1024L*1024L*1024L);
    }

    @Test
    public void testGetSizeProperty_m() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.SIZE, "42g", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1m");
        XenonProperties xprop = new XenonProperties(valid, props);

        //noinspection PointlessArithmeticExpression
        assertTrue(xprop.getSizeProperty("key") == 1L*1024L*1024L);
    }
    
    @Test
    public void testGetSizeProperty_M() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.SIZE, "42g", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1M");
        XenonProperties xprop = new XenonProperties(valid, props);

        //noinspection PointlessArithmeticExpression
        assertTrue(xprop.getSizeProperty("key") == 1L*1024L*1024L);
    }

    @Test
    public void testGetSizeProperty_k() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.SIZE, "42g", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1k");
        XenonProperties xprop = new XenonProperties(valid, props);

        //noinspection PointlessArithmeticExpression
        assertTrue(xprop.getSizeProperty("key") == 1L*1024L);
    }
    
    @Test
    public void testGetSizeProperty_K() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.SIZE, "42g", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1K");
        XenonProperties xprop = new XenonProperties(valid, props);

        //noinspection PointlessArithmeticExpression
        assertTrue(xprop.getSizeProperty("key") == 1L*1024L);
    }

    @Test
    public void testGetSizeProperty() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.SIZE, "42g", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1");
        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.getSizeProperty("key") == 1L);
    }
    
    @Test(expected = InvalidPropertyException.class)
    public void testGetSizeProperty_X_fails() throws Exception {

        ImmutableArray<XenonPropertyDescription> valid = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("key", Type.SIZE, "42g", 
                        "test property"));

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1X");
        XenonProperties xprop = new XenonProperties(valid, props);
        xprop.getSizeProperty("key"); // throws exception
    }

    @Test
    public void testXenonProperties_filter_withPropertiesSet() throws Exception {
        
        ImmutableArray<XenonPropertyDescription> supportedProperties = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("aap.key", Type.STRING, "aap",
                        "test property"),
                new XenonPropertyDescription("noot.key", Type.STRING, "noot",
                        "test property"));

        Map<String, String> props = new HashMap<>(3);
        props.put("aap.key", "aap2");
        props.put("noot.key", "noot2");
        
        XenonProperties xprop = new XenonProperties(supportedProperties, props).filter("aap");
        
        assertEquals("{aap.key=aap2}", xprop.toString());
    }
    
    @Test
    public void testXenonProperties_filter_noPropertiesSet() throws Exception {
        
        ImmutableArray<XenonPropertyDescription> supportedProperties = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("aap.key", Type.STRING, "aap",
                        "test property"),
                new XenonPropertyDescription("noot.key", Type.STRING, "noot",
                        "test property"));

        Map<String, String> props = new HashMap<>(0);
        
        XenonProperties xprop = new XenonProperties(supportedProperties, props).filter("aap");
        
        assertEquals("{<<aap.key=aap>>}", xprop.toString());
    }

    @Test
    public void testXenonProperties_filter_wrongPrefix() throws Exception {
        
        ImmutableArray<XenonPropertyDescription> supportedProperties = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("aap.key", Type.STRING, "aap",
                        "test property"),
                new XenonPropertyDescription("noot.key", Type.STRING, "noot",
                        "test property"));

        Map<String, String> props = new HashMap<>(0);
        
        XenonProperties xprop = new XenonProperties(supportedProperties, props).filter("bla");
        
        assertEquals("{}", xprop.toString());
    }

    @Test
    public void testXenonProperties_exclude_withPropertiesSet() throws Exception {
        
        ImmutableArray<XenonPropertyDescription> supportedProperties = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("aap.key", Type.STRING, "aap",
                        "test property"),
                new XenonPropertyDescription("noot.key", Type.STRING, "noot",
                        "test property"));

        Map<String, String> props = new HashMap<>(0);
        props.put("aap.key", "aap2");
        props.put("noot.key", "noot2");
        
        XenonProperties xprop = new XenonProperties(supportedProperties, props).exclude("noot");
        
        assertEquals("{aap.key=aap2}", xprop.toString());
    }
    
    @Test
    public void testXenonProperties_exclude_noPropertiesSet() throws Exception {
        
        ImmutableArray<XenonPropertyDescription> supportedProperties = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("aap.key", Type.STRING, "aap",
                        "test property"),
                new XenonPropertyDescription("noot.key", Type.STRING, "noot",
                        "test property"));

        Map<String, String> props = new HashMap<>(0);
        
        XenonProperties xprop = new XenonProperties(supportedProperties, props).exclude("noot");
        
        assertEquals("{<<aap.key=aap>>}", xprop.toString());
    }

    @Test
    public void testXenonProperties_exclude_wrongPrefix() throws Exception {
        
        ImmutableArray<XenonPropertyDescription> supportedProperties = new ImmutableArray<XenonPropertyDescription>(
                new XenonPropertyDescription("aap.key", Type.STRING, "aap",
                        "test property"));

        Map<String, String> props = new HashMap<>(0);
        
        XenonProperties xprop = new XenonProperties(supportedProperties, props).exclude("bla");
        
        assertEquals("{<<aap.key=aap>>}", xprop.toString());
    }
    
    
    
    
    
    
    
    
    /*
     * FIXME!!!
     * 
        @Test(expected = InvalidPropertyException.class) 
        public void testGetIntegerProperty_emptyString_False() throws Exception {

            XenonPropertyDescription [] valid = new XenonPropertyDescription [] { 
                 new XenonPropertyDescription("key", Type.INTEGER, EnumSet.of(Level.XENON), "42", "test property")
            };

            Map<String,String> props = new HashMap<>();
            props.put("key", "bla");
            new XenonProperties(valid, props);
        }

        @Test(expected = PropertyTypeException.class) 
        public void testGetIntegerProperty_emptyString_False() throws Exception {

            XenonPropertyDescription [] valid = new XenonPropertyDescription [] { 
                 new XenonPropertyDescription("key", Type.INTEGER, EnumSet.of(Level.XENON), "42", "test property")
            };

            Map<String,String> props = new HashMap<>();
            props.put("key", "bla");
            new XenonProperties(valid, props);
        }

        
        
        
        
        
        
        
        @Test
        public void testGetIntProperty_int_int() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "42");
            XenonProperties xprop = new XenonProperties(props);

            int result = xprop.getIntProperty("key");

            assertTrue(result == 42);
        }

        @Test
        public void testGetIntProperty_null_UndefinedException() throws Exception {
            Map<String,String> props = new HashMap<>();
            XenonProperties xprop = new XenonProperties(props);

            try {
                xprop.getIntProperty("key");
                fail("No NumberFormatException thrown");
            } catch (NumberFormatException e) {
                assertTrue(e.getMessage().equals("property undefined: key"));
            }
        }

        @Test(expected = NumberFormatException.class)
        public void testGetIntProperty_null_IntParseException() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "foo");
            XenonProperties xprop = new XenonProperties(props);
            xprop.getIntProperty("key");
        }

        @Test(expected = NumberFormatException.class)
        public void testGetIntProperty_null_IntParseException2() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "foo");
            XenonProperties xprop = new XenonProperties(props);
            xprop.getIntProperty("key", 42);
        }

        @Test
        public void testGetIntProperty_withDefault2() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "33");
            XenonProperties xprop = new XenonProperties(props);

            int result = xprop.getIntProperty("key", 42);

            assertTrue(result == 33);
        }

        @Test
        public void testGetIntProperty_withDefault() throws Exception {
            XenonProperties xprop = new XenonProperties();

            int result = xprop.getIntProperty("key", 42);

            assertTrue(result == 42);
        }

        @Test
        public void testGetIntProperty_SetAndDefault_returnsSet() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "13");
            XenonProperties xprop = new XenonProperties(props);

            int result = xprop.getIntProperty("key", 42);

            assertTrue(result == 13);
        }

        @Test
        public void testGetLongProperty_long_long() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "42");
            XenonProperties xprop = new XenonProperties(props);

            long result = xprop.getLongProperty("key");

            assertTrue(result == 42);
        }

        @Test
        public void testGetLongProperty_null_UndefinedException() throws Exception {
            Map<String,String> props = new HashMap<>();
            XenonProperties xprop = new XenonProperties(props);

            try {
                xprop.getLongProperty("key");
                fail("No NumberFormatException thrown");
            } catch (NumberFormatException e) {
                assertTrue(e.getMessage().equals("property undefined: key"));
            }
        }

        @Test(expected = NumberFormatException.class)
        public void testGetLongProperty_null_LongParseException() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "foo");
            XenonProperties xprop = new XenonProperties(props);
            xprop.getLongProperty("key");
        }

        @Test(expected = NumberFormatException.class)
        public void testGetLongProperty_null_LongParseException2() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "foo");
            XenonProperties xprop = new XenonProperties(props);
            xprop.getLongProperty("key", 42L);
        }

        @Test
        public void testGetLongProperty_withDefault() throws Exception {
            XenonProperties xprop = new XenonProperties();

            long result = xprop.getLongProperty("key", 42);

            assertTrue(result == 42);
        }

        @Test
        public void testGetLongProperty_SetAndDefault_returnsSet() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "13");
            XenonProperties xprop = new XenonProperties(props);

            long result = xprop.getLongProperty("key", 42);

            assertTrue(result == 13);
        }

        @Test
        public void testGetShortProperty_short_short() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "42");
            XenonProperties xprop = new XenonProperties(props);

            short result = xprop.getShortProperty("key");

            short expected = 42;
            assertTrue(result == expected);
        }

        @Test
        public void testGetShortProperty_null_UndefinedException() throws Exception {
            Map<String,String> props = new HashMap<>();
            XenonProperties xprop = new XenonProperties(props);

            try {
                xprop.getShortProperty("key");
                fail("No NumberFormatException thrown");
            } catch (NumberFormatException e) {
                assertTrue(e.getMessage().equals("property undefined: key"));
            }
        }

        @Test(expected = NumberFormatException.class)
        public void testGetShortProperty_null_ShortParseException() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "foo");
            XenonProperties xprop = new XenonProperties(props);
            xprop.getShortProperty("key");
        }

        @Test(expected = NumberFormatException.class)
        public void testGetShortProperty_null_ShortParseException2() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "foo");
            XenonProperties xprop = new XenonProperties(props);
            xprop.getShortProperty("key", (short) 42);
        }

        @Test
        public void testGetShortProperty_withDefault() throws Exception {
            XenonProperties xprop = new XenonProperties();
            short default_value = 42;

            short result = xprop.getShortProperty("key", default_value);

            assertTrue(result == 42);
        }

        @Test
        public void testGetShortProperty_SetAndDefault_returnsSet() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "13");
            XenonProperties xprop = new XenonProperties(props);

            short default_value = 42;
            short result = xprop.getShortProperty("key", default_value);

            assertTrue(result == 13);
        }

        @Test
        public void testGetDoubleProperty_double_double() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "42.123");
            XenonProperties xprop = new XenonProperties(props);

            double result = xprop.getDoubleProperty("key");

            assertTrue(result == 42.123);
        }

        @Test
        public void testGetDoubleProperty_null_UndefinedException() throws Exception {
            Map<String,String> props = new HashMap<>();
            XenonProperties xprop = new XenonProperties(props);

            try {
                xprop.getDoubleProperty("key");
                fail("No NumberFormatException thrown");
            } catch (NumberFormatException e) {
                assertTrue(e.getMessage().equals("property undefined: key"));
            }
        }

        @Test(expected = NumberFormatException.class)
        public void testGetDoubleProperty_null_DoubleParseException() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "foo");
            XenonProperties xprop = new XenonProperties(props);
            xprop.getDoubleProperty("key");
        }

        @Test
        public void testGetDoubleProperty_withDefault() throws Exception {
            XenonProperties xprop = new XenonProperties();
            double default_value = 42.123;

            double result = xprop.getDoubleProperty("key", default_value);

            assertTrue(result == 42.123);
        }

        @Test(expected = NumberFormatException.class)
        public void testGetDoubleProperty_withDefault_DoubleParseException() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "foo");
            XenonProperties xprop = new XenonProperties(props);
            xprop.getDoubleProperty("key", 42.0);
        }

        @Test
        public void testGetDoubleProperty_SetAndDefault_returnsSet() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "13.456");
            XenonProperties xprop = new XenonProperties(props);

            double default_value = 42.123;
            double result = xprop.getDoubleProperty("key", default_value);

            assertTrue(result == 13.456);
        }

        @Test
        public void testGetFloatProperty_float_float() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "42.123f");
            XenonProperties xprop = new XenonProperties(props);

            float result = xprop.getFloatProperty("key");

            assertTrue(result == 42.123f);
        }

        @Test(expected = NumberFormatException.class)
        public void testGetFloatProperty_null_UndefinedException() throws Exception {
            Map<String,String> props = new HashMap<>();
            new XenonProperties(props).getFloatProperty("key");
        }

        @Test(expected = NumberFormatException.class)
        public void testGetFloatProperty_null_FloatParseException() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "foo");
            new XenonProperties(props).getFloatProperty("key");

        }

        @Test
        public void testGetFloatProperty_withDefault() throws Exception {
            XenonProperties xprop = new XenonProperties();
            float default_value = 42.123f;

            float result = xprop.getFloatProperty("key", default_value);

            assertTrue(result == 42.123f);
        }

        @Test(expected = NumberFormatException.class)
        public void testGetFloatProperty_withDefault_FloatParseException() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "foo");
            new XenonProperties(props).getFloatProperty("key", 42.0f);
        }

        @Test
        public void testGetFloatProperty_SetAndDefault_returnsSet() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "13.456f");
            XenonProperties xprop = new XenonProperties(props);

            float default_value = 42.123f;
            float result = xprop.getFloatProperty("key", default_value);

            assertTrue(result == 13.456f);
        }

        @Test
        public void testGetSizeProperty() throws Exception {

            Map<String,String> props = new HashMap<>();
            props.put("B", "100");
            props.put("K", "100K");
            props.put("M", "100M");
            props.put("G", "100G");

            XenonProperties xprop = new XenonProperties(props);

            long result = xprop.getSizeProperty("B");

            assertTrue(result == 100L);

            result = xprop.getSizeProperty("K");

            assertTrue(result == (100L * 1024L));

            result = xprop.getSizeProperty("M");

            assertTrue(result == (100L * 1024L * 1024L));

            result = xprop.getSizeProperty("G");

            assertTrue(result == (100L * 1024L * 1024L * 1024L));
        }

        @Test
        public void testGetSizeProperty1() throws Exception {

            Map<String,String> props = new HashMap<>();
            props.put("B", "100");
            props.put("K", "100k");
            props.put("M", "100m");
            props.put("G", "100g");

            XenonProperties xprop = new XenonProperties(props);

            long result = xprop.getSizeProperty("B");

            assertTrue(result == 100L);

            result = xprop.getSizeProperty("K");

            assertTrue(result == (100L * 1024L));

            result = xprop.getSizeProperty("M");

            assertTrue(result == (100L * 1024L * 1024L));

            result = xprop.getSizeProperty("G");

            assertTrue(result == (100L * 1024L * 1024L * 1024L));
        }

        @Test(expected = NumberFormatException.class)
        public void testGetSizeProperty2() throws Exception {

            Map<String,String> props = new HashMap<>();
            props.put("K", "100Q");

            XenonProperties xprop = new XenonProperties(props);

            xprop.getSizeProperty("K");
        }

        @Test(expected = NumberFormatException.class)
        public void testGetSizeProperty3() throws Exception {

            Map<String,String> props = new HashMap<>();
            XenonProperties xprop = new XenonProperties(props);

            xprop.getSizeProperty("K");
        }

        @Test
        public void testGetSizePropertyStringLong() throws Exception {

            Map<String,String> props = new HashMap<>();
            props.put("B", "100");

            XenonProperties xprop = new XenonProperties(props);

            long result = xprop.getSizeProperty("B", 999);

            assertTrue(result == 100L);

            result = xprop.getSizeProperty("X", 999);

            assertTrue(result == 999L);
        }

        @Test
        public void testGetStringList_emptyValue_emptyList() throws Exception {
            XenonProperties xprop = new XenonProperties();

            String[] result = xprop.getStringList("key");
            String[] expected = new String[] {};
            assertTrue(Arrays.equals(result, expected));
        }

        @Test
        public void testGetStringList_emptyValue_Defaults() throws Exception {
            XenonProperties xprop = new XenonProperties();
            String[] defaults = new String[] { "value1", "value2", "value3" };
            String[] result = xprop.getStringList("key", ", ", defaults);

            String[] expected = new String[] { "value1", "value2", "value3" };
            assertTrue(Arrays.equals(result, expected));
        }

        @Test
        public void testGetString_withDefaultDelimiter() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "value1, value2, value3");
            XenonProperties xprop = new XenonProperties(props);

            String[] result = xprop.getStringList("key");

            String[] expected = new String[] { "value1", "value2", "value3" };
            assertTrue(Arrays.equals(result, expected));
        }

        @Test
        public void testGetString_withDelimiter() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "value1, value2, value3");
            XenonProperties xprop = new XenonProperties(props);

            String[] result = xprop.getStringList("key", ", ");

            String[] expected = new String[] { "value1", "value2", "value3" };
            assertTrue(Arrays.equals(result, expected));
        }

        @Test
        public void testFilter_filledWithKeyAndItem_returnsKey() throws Exception {
            XenonProperties xprop = getSample();

            XenonProperties nxprop = xprop.filter("k");

            assertEquals(nxprop.toString(), "XenonProperties [properties={key=value}]");
        }

        @Test
        public void testFilter_null() throws Exception {
            XenonProperties xprop = getSample();
            XenonProperties nxprop = xprop.filter(null);
            assertEquals(nxprop.toString(), "XenonProperties [properties={key=value, item=value2}]");
        }

        @Test
        public void testFilter_emptyPrefix_returnsSame() throws Exception {
            XenonProperties xprop = getSample();

            XenonProperties nxprop = xprop.filter("");

            assertEquals(nxprop, xprop);
        }

        @Test
        public void testPrintProperties() throws Exception {

            Map<String,String> props = new HashMap<>();
            props.put("key", "value");
            props.put("item", "value2");
            XenonProperties xprop = new XenonProperties(props);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            xprop.printProperties(new PrintStream(out), "");

            String s = out.toString();

            assertTrue(s.equals("key = value\nitem = value2\n"));

            out = new ByteArrayOutputStream();
            xprop.printProperties(new PrintStream(out), "NOOT");

            s = out.toString();

            assertTrue(s.equals(""));

            out = new ByteArrayOutputStream();
            xprop.printProperties(new PrintStream(out), "key");

            s = out.toString();

            assertTrue(s.equals("key = value\n"));

            out = new ByteArrayOutputStream();
            xprop.printProperties(new PrintStream(out), null);

            s = out.toString();

            assertTrue(s.equals("key = value\nitem = value2\n"));
        }

        @Test
        public void testToString() throws Exception {
            XenonProperties xprop = getSample();

            assertEquals(xprop.toString(), "XenonProperties [properties={key=value, item=value2}]");
        }

        public XenonProperties getSample() throws Exception {
            Map<String,String> props = new HashMap<>();
            props.put("key", "value");
            props.put("item", "value2");
            XenonProperties xprop = new XenonProperties(props);
            return xprop;
        }

        @Test
        public void testGetPropertyNames() throws Exception {
            XenonProperties xprop = getSample();
            String[] names = xprop.getPropertyNames();
            String[] expected_names = new String[] { "item", "key" };
            assertTrue(Arrays.equals(names, expected_names));
        }

        @Test
        public void testEqualsObject() throws Exception {

            XenonProperties xprop1 = getSample();

            boolean b = xprop1.equals(xprop1);

            assertTrue(b);

            XenonProperties xprop2 = getSample();

            b = xprop1.equals(xprop2);

            assertTrue(b);

            Map<String,String> props = new HashMap<>();
            props.put("key", "value");

            XenonProperties xprop3 = new XenonProperties(props);
            b = xprop1.equals(xprop3);

            assertFalse(b);
        }

        @Test
        public void testSetProperty_Unsupported() throws Exception {
            XenonProperties xprop = new XenonProperties();
            try {
                xprop.setProperty("key", "value");
            } catch (UnsupportedOperationException e) {
                assertTrue(e.getMessage().equals("setting properties unsupported in ImmutableTypedProperties"));
            }
        }

        @Test
        public void testPutObject_Unsupported() throws Exception {
            XenonProperties xprop = new XenonProperties();
            try {
                xprop.put("key", "value");
            } catch (UnsupportedOperationException e) {
                assertTrue(e.getMessage().equals("setting properties unsupported in ImmutableTypedProperties"));
            }
        }

        @Test
        public void testRemove_Unsupported() throws Exception {
            XenonProperties xprop = new XenonProperties();
            try {
                xprop.remove("key");
            } catch (UnsupportedOperationException e) {
                assertTrue(e.getMessage().equals("setting properties unsupported in ImmutableTypedProperties"));
            }
        }

        @Test
        public void testPutAllMapOfQextendsObjectQextendsObject() throws Exception {

            XenonProperties xprop = new XenonProperties();

            Map<String, String> tmp = new HashMap<String, String>();
            tmp.put("key", "value");

            try {
                xprop.putAll(tmp);
            } catch (UnsupportedOperationException e) {
                assertTrue(e.getMessage().equals("setting properties unsupported in ImmutableTypedProperties"));
            }
        }

        @Test
        public void testLoad_Reader_Unsupported() throws Exception {
            XenonProperties xprop = new XenonProperties();
            try {
                xprop.load(mock(Reader.class));
            } catch (UnsupportedOperationException e) {
                assertTrue(e.getMessage().equals("setting properties unsupported in ImmutableTypedProperties"));
            }
        }

        @Test
        public void testLoad_InputStream_Unsupported() throws Exception {
            XenonProperties xprop = new XenonProperties();
            try {
                xprop.load(mock(InputStream.class));
            } catch (UnsupportedOperationException e) {
                assertTrue(e.getMessage().equals("setting properties unsupported in ImmutableTypedProperties"));
            }
        }

        @Test
        public void testLoadFromXML_Unsupported() throws Exception {
            XenonProperties xprop = new XenonProperties();
            try {
                xprop.loadFromXML(mock(InputStream.class));
            } catch (UnsupportedOperationException e) {
                assertTrue(e.getMessage().equals("setting properties unsupported in ImmutableTypedProperties"));
            }
        }

    *
    */
}
