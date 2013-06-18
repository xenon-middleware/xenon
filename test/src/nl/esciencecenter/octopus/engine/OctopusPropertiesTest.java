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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;
import static org.mockito.Mockito.*;

public class OctopusPropertiesTest {

    @Test
    public void testClear_Unsupported() {
        OctopusProperties octprop = new OctopusProperties();
        try {
            octprop.clear();
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().equals("setting properties unsupported in ImmutableTypedProperties"));
        }
    }

    @Test
    public void testOctopusProperties_fromProperties() {
        Properties props = new Properties();
        props.setProperty("key", "value");

        OctopusProperties octprop = new OctopusProperties(props);

        assertEquals(octprop.toString(), "OctopusProperties [properties={key=value}]");
    }

    @Test
    public void testOctopusProperties_fromMultipleProperties_noOverlap() {
        Properties props = new Properties();
        props.setProperty("key", "value");
        Properties props2 = new Properties();
        props2.setProperty("key2", "value2");

        OctopusProperties octprop = new OctopusProperties(props, props2);

        assertEquals(octprop.toString(), "OctopusProperties [properties={key=value, key2=value2}]");
    }

    @Test
    public void testOctopusProperties_fromMultipleProperties_withOverlap() {
        Properties props = new Properties();
        props.setProperty("key", "value");
        Properties props2 = new Properties();
        props2.setProperty("key", "value2");

        OctopusProperties octprop = new OctopusProperties(props, props2);

        assertEquals(octprop.toString(), "OctopusProperties [properties={key=value2}]");
    }

    @Test
    public void testOctopusProperties_fromDefaultsAndProperties_noOverlap() {
        String[][] defaults = new String[][] { { "key", "value" } };
        Properties props = new Properties();
        props.setProperty("key2", "value2");

        OctopusProperties octprop = new OctopusProperties(defaults, props);

        assertEquals(octprop.toString(), "OctopusProperties [properties={key=value, key2=value2}]");
    }

    @Test
    public void testOctopusProperties_fromDefaultsAndProperties_withOverlap() {
        String[][] defaults = new String[][] { { "key", "value" } };
        Properties props = new Properties();
        props.setProperty("key", "value2");

        OctopusProperties octprop = new OctopusProperties(defaults, props);

        assertEquals(octprop.toString(), "OctopusProperties [properties={key=value2}]");
    }

    //    @Test
    //    public void testLoadFromClassPath() {
    //        fail("Not yet implemented");
    //    }
    //
    //    @Test
    //    public void testLoadFromFile() {
    //        fail("Not yet implemented");
    //    }
    //
    //    @Test
    //    public void testLoadFromHomeFile() {
    //        fail("Not yet implemented");
    //    }

    @Test
    public void testGetBooleanProperty_1_True() {
        Properties props = new Properties();
        props.setProperty("key", "1");
        OctopusProperties octprop = new OctopusProperties(props);

        assertTrue(octprop.getBooleanProperty("key"));
    }

    @Test
    public void testGetBooleanProperty_on_True() {
        Properties props = new Properties();
        props.setProperty("key", "on");
        OctopusProperties octprop = new OctopusProperties(props);

        assertTrue(octprop.getBooleanProperty("key"));
    }

    @Test
    public void testGetBooleanProperty_true_True() {
        Properties props = new Properties();
        props.setProperty("key", "true");
        OctopusProperties octprop = new OctopusProperties(props);

        assertTrue(octprop.getBooleanProperty("key"));
    }

    @Test
    public void testGetBooleanProperty_True_True() {
        Properties props = new Properties();
        props.setProperty("key", "True");
        OctopusProperties octprop = new OctopusProperties(props);

        assertTrue(octprop.getBooleanProperty("key"));
    }

    @Test
    public void testGetBooleanProperty_yes_True() {
        Properties props = new Properties();
        props.setProperty("key", "yes");
        OctopusProperties octprop = new OctopusProperties(props);

        assertTrue(octprop.getBooleanProperty("key"));
    }

    @Test
    public void testGetBooleanProperty_emptyString_False() {
        Properties props = new Properties();
        props.setProperty("key", "");
        OctopusProperties octprop = new OctopusProperties(props);

        assertFalse(octprop.getBooleanProperty("key"));
    }

    @Test
    public void testGetBooleanProperty_false_False() {
        Properties props = new Properties();
        props.setProperty("key", "false");
        OctopusProperties octprop = new OctopusProperties(props);

        assertFalse(octprop.getBooleanProperty("key"));
    }

    @Test
    public void testGetBooleanProperty_unset() {
        OctopusProperties octprop = new OctopusProperties();
        boolean v = octprop.getBooleanProperty("key");
        assertFalse(v);
    }

    @Test
    public void testGetBooleanProperty_unset2() {
        OctopusProperties octprop = new OctopusProperties();
        boolean v = octprop.getBooleanProperty("key", true);
        assertTrue(v);
    }
    
    @Test
    public void testGetIntProperty_int_int() {
        Properties props = new Properties();
        props.setProperty("key", "42");
        OctopusProperties octprop = new OctopusProperties(props);

        int result = octprop.getIntProperty("key");

        assertTrue(result == 42);
    }

    @Test
    public void testGetIntProperty_null_UndefinedException() {
        Properties props = new Properties();
        OctopusProperties octprop = new OctopusProperties(props);

        try {
            octprop.getIntProperty("key");
            fail("No NumberFormatException thrown");
        } catch (NumberFormatException e) {
            assertTrue(e.getMessage().equals("property undefined: key"));
        }
    }

    @Test(expected = NumberFormatException.class)
    public void testGetIntProperty_null_IntParseException() {
        Properties props = new Properties();
        props.setProperty("key", "foo");
        OctopusProperties octprop = new OctopusProperties(props);
        octprop.getIntProperty("key");        
    }

    @Test(expected = NumberFormatException.class)
    public void testGetIntProperty_null_IntParseException2() {
        Properties props = new Properties();
        props.setProperty("key", "foo");
        OctopusProperties octprop = new OctopusProperties(props);
        octprop.getIntProperty("key", 42);        
    }
    
    @Test
    public void testGetIntProperty_withDefault2() {
        Properties props = new Properties();
        props.setProperty("key", "33");
        OctopusProperties octprop = new OctopusProperties(props);

        int result = octprop.getIntProperty("key", 42);

        assertTrue(result == 33);
    }

    @Test
    public void testGetIntProperty_withDefault() {
        OctopusProperties octprop = new OctopusProperties();

        int result = octprop.getIntProperty("key", 42);

        assertTrue(result == 42);
    }

    
    
    @Test
    public void testGetIntProperty_SetAndDefault_returnsSet() {
        Properties props = new Properties();
        props.setProperty("key", "13");
        OctopusProperties octprop = new OctopusProperties(props);

        int result = octprop.getIntProperty("key", 42);

        assertTrue(result == 13);
    }

    @Test
    public void testGetLongProperty_long_long() {
        Properties props = new Properties();
        props.setProperty("key", "42");
        OctopusProperties octprop = new OctopusProperties(props);

        long result = octprop.getLongProperty("key");

        assertTrue(result == 42);
    }

    @Test
    public void testGetLongProperty_null_UndefinedException() {
        Properties props = new Properties();
        OctopusProperties octprop = new OctopusProperties(props);

        try {
            octprop.getLongProperty("key");
            fail("No NumberFormatException thrown");
        } catch (NumberFormatException e) {
            assertTrue(e.getMessage().equals("property undefined: key"));
        }
    }

    @Test(expected = NumberFormatException.class)
    public void testGetLongProperty_null_LongParseException() {
        Properties props = new Properties();
        props.setProperty("key", "foo");
        OctopusProperties octprop = new OctopusProperties(props);
        octprop.getLongProperty("key");
    }

    @Test(expected = NumberFormatException.class)
    public void testGetLongProperty_null_LongParseException2() {
        Properties props = new Properties();
        props.setProperty("key", "foo");
        OctopusProperties octprop = new OctopusProperties(props);
        octprop.getLongProperty("key", 42L);
    }
    
    @Test
    public void testGetLongProperty_withDefault() {
        OctopusProperties octprop = new OctopusProperties();

        long result = octprop.getLongProperty("key", 42);

        assertTrue(result == 42);
    }

    @Test
    public void testGetLongProperty_SetAndDefault_returnsSet() {
        Properties props = new Properties();
        props.setProperty("key", "13");
        OctopusProperties octprop = new OctopusProperties(props);

        long result = octprop.getLongProperty("key", 42);

        assertTrue(result == 13);
    }

    @Test
    public void testGetShortProperty_short_short() {
        Properties props = new Properties();
        props.setProperty("key", "42");
        OctopusProperties octprop = new OctopusProperties(props);

        short result = octprop.getShortProperty("key");

        short expected = 42;
        assertTrue(result== expected);
    }

    @Test
    public void testGetShortProperty_null_UndefinedException() {
        Properties props = new Properties();
        OctopusProperties octprop = new OctopusProperties(props);

        try {
            octprop.getShortProperty("key");
            fail("No NumberFormatException thrown");
        } catch (NumberFormatException e) {
            assertTrue(e.getMessage().equals("property undefined: key"));
        }
    }

    @Test(expected = NumberFormatException.class)
    public void testGetShortProperty_null_ShortParseException() {
        Properties props = new Properties();
        props.setProperty("key", "foo");
        OctopusProperties octprop = new OctopusProperties(props);
        octprop.getShortProperty("key");
    }

    @Test(expected = NumberFormatException.class)
    public void testGetShortProperty_null_ShortParseException2() {
        Properties props = new Properties();
        props.setProperty("key", "foo");
        OctopusProperties octprop = new OctopusProperties(props);
        octprop.getShortProperty("key", (short) 42);
    }
    
    @Test
    public void testGetShortProperty_withDefault() {
        OctopusProperties octprop = new OctopusProperties();
        short default_value = 42;

        short result = octprop.getShortProperty("key", default_value);

        assertTrue(result == 42);
    }

    @Test
    public void testGetShortProperty_SetAndDefault_returnsSet() {
        Properties props = new Properties();
        props.setProperty("key", "13");
        OctopusProperties octprop = new OctopusProperties(props);

        short default_value = 42;
        short result = octprop.getShortProperty("key", default_value);

        assertTrue(result == 13);
    }

    @Test
    public void testGetDoubleProperty_double_double() {
        Properties props = new Properties();
        props.setProperty("key", "42.123");
        OctopusProperties octprop = new OctopusProperties(props);

        double result = octprop.getDoubleProperty("key");

        assertTrue(result == 42.123);
    }

    @Test
    public void testGetDoubleProperty_null_UndefinedException() {
        Properties props = new Properties();
        OctopusProperties octprop = new OctopusProperties(props);

        try {
            octprop.getDoubleProperty("key");
            fail("No NumberFormatException thrown");
        } catch (NumberFormatException e) {
            assertTrue(e.getMessage().equals("property undefined: key"));
        }
    }

    @Test(expected = NumberFormatException.class)
    public void testGetDoubleProperty_null_DoubleParseException() {
        Properties props = new Properties();
        props.setProperty("key", "foo");
        OctopusProperties octprop = new OctopusProperties(props);
        octprop.getDoubleProperty("key");
    }

    @Test
    public void testGetDoubleProperty_withDefault() {
        OctopusProperties octprop = new OctopusProperties();
        double default_value = 42.123;

        double result = octprop.getDoubleProperty("key", default_value);

        assertTrue(result == 42.123);
    }

    @Test(expected = NumberFormatException.class)
    public void testGetDoubleProperty_withDefault_DoubleParseException() {
        Properties props = new Properties();
        props.setProperty("key", "foo");
        OctopusProperties octprop = new OctopusProperties(props);
        octprop.getDoubleProperty("key", 42.0);
    }
    
    @Test
    public void testGetDoubleProperty_SetAndDefault_returnsSet() {
        Properties props = new Properties();
        props.setProperty("key", "13.456");
        OctopusProperties octprop = new OctopusProperties(props);

        double default_value = 42.123;
        double result = octprop.getDoubleProperty("key", default_value);

        assertTrue(result == 13.456);
    }

    @Test
    public void testGetFloatProperty_float_float() {
        Properties props = new Properties();
        props.setProperty("key", "42.123f");
        OctopusProperties octprop = new OctopusProperties(props);

        float result = octprop.getFloatProperty("key");

        assertTrue(result == 42.123f);
    }

    @Test(expected = NumberFormatException.class)
    public void testGetFloatProperty_null_UndefinedException() {
        Properties props = new Properties();
        new OctopusProperties(props).getFloatProperty("key");        
    }

    @Test(expected = NumberFormatException.class)
    public void testGetFloatProperty_null_FloatParseException() {
        Properties props = new Properties();
        props.setProperty("key", "foo");
        new OctopusProperties(props).getFloatProperty("key");
        
    }

    @Test
    public void testGetFloatProperty_withDefault() {
        OctopusProperties octprop = new OctopusProperties();
        float default_value = 42.123f;

        float result = octprop.getFloatProperty("key", default_value);

        assertTrue(result == 42.123f);
    }

    @Test(expected = NumberFormatException.class)
    public void testGetFloatProperty_withDefault_FloatParseException() {
        Properties props = new Properties();
        props.setProperty("key", "foo");
        new OctopusProperties(props).getFloatProperty("key", 42.0f);
    }

    
    @Test
    public void testGetFloatProperty_SetAndDefault_returnsSet() {
        Properties props = new Properties();
        props.setProperty("key", "13.456f");
        OctopusProperties octprop = new OctopusProperties(props);

        float default_value = 42.123f;
        float result = octprop.getFloatProperty("key", default_value);

        assertTrue(result ==13.456f);
    }

    @Test
    public void testGetSizeProperty() {

        Properties props = new Properties();
        props.setProperty("B", "100");
        props.setProperty("K", "100K");
        props.setProperty("M", "100M");
        props.setProperty("G", "100G");

        OctopusProperties octprop = new OctopusProperties(props);

        long result = octprop.getSizeProperty("B");

        assertTrue(result == 100L);

        result = octprop.getSizeProperty("K");

        assertTrue(result == (100L * 1024L));

        result = octprop.getSizeProperty("M");

        assertTrue(result == (100L * 1024L * 1024L));

        result = octprop.getSizeProperty("G");

        assertTrue(result == (100L * 1024L * 1024L * 1024L));
    }

    @Test
    public void testGetSizeProperty1() {

        Properties props = new Properties();
        props.setProperty("B", "100");
        props.setProperty("K", "100k");
        props.setProperty("M", "100m");
        props.setProperty("G", "100g");

        OctopusProperties octprop = new OctopusProperties(props);

        long result = octprop.getSizeProperty("B");

        assertTrue(result == 100L);

        result = octprop.getSizeProperty("K");

        assertTrue(result == (100L * 1024L));

        result = octprop.getSizeProperty("M");

        assertTrue(result == (100L * 1024L * 1024L));

        result = octprop.getSizeProperty("G");

        assertTrue(result == (100L * 1024L * 1024L * 1024L));
    }
    
    @Test(expected = NumberFormatException.class)
    public void testGetSizeProperty2() {

        Properties props = new Properties();
        props.setProperty("K", "100Q");

        OctopusProperties octprop = new OctopusProperties(props);
        
        octprop.getSizeProperty("K");
    }

    @Test(expected = NumberFormatException.class)
    public void testGetSizeProperty3() {

        Properties props = new Properties();
        OctopusProperties octprop = new OctopusProperties(props);
        
        octprop.getSizeProperty("K");
    }
    
    @Test
    public void testGetSizePropertyStringLong() {

        Properties props = new Properties();
        props.setProperty("B", "100");

        OctopusProperties octprop = new OctopusProperties(props);

        long result = octprop.getSizeProperty("B", 999);

        assertTrue(result == 100L);

        result = octprop.getSizeProperty("X", 999);

        assertTrue(result == 999L);
    }

    @Test
    public void testGetStringList_emptyValue_emptyList() {
        OctopusProperties octprop = new OctopusProperties();

        String[] result = octprop.getStringList("key");
        String[] expected = new String[] {};
        assertTrue(Arrays.equals(result, expected));
    }

    @Test
    public void testGetStringList_emptyValue_Defaults() {
        OctopusProperties octprop = new OctopusProperties();
        String[] defaults = new String[] { "value1", "value2", "value3" };
        String[] result = octprop.getStringList("key", ", ", defaults);

        String[] expected = new String[] { "value1", "value2", "value3" };
        assertTrue(Arrays.equals(result, expected));
    }

    @Test
    public void testGetString_withDefaultDelimiter() {
        Properties props = new Properties();
        props.setProperty("key", "value1, value2, value3");
        OctopusProperties octprop = new OctopusProperties(props);

        String[] result = octprop.getStringList("key");

        String[] expected = new String[] { "value1", "value2", "value3" };
        assertTrue(Arrays.equals(result, expected));
    }

    @Test
    public void testGetString_withDelimiter() {
        Properties props = new Properties();
        props.setProperty("key", "value1, value2, value3");
        OctopusProperties octprop = new OctopusProperties(props);

        String[] result = octprop.getStringList("key", ", ");

        String[] expected = new String[] { "value1", "value2", "value3" };
        assertTrue(Arrays.equals(result, expected));
    }

    @Test
    public void testFilter_filledWithKeyAndItem_returnsKey() {
        OctopusProperties octprop = getSample();

        OctopusProperties noctprop = octprop.filter("k");

        assertEquals(noctprop.toString(), "OctopusProperties [properties={key=value}]");
    }

    @Test
    public void testFilter_null() {
        OctopusProperties octprop = getSample();
        OctopusProperties noctprop = octprop.filter(null);
        assertEquals(noctprop.toString(), "OctopusProperties [properties={key=value, item=value2}]");
    }
    
    @Test
    public void testFilter_emptyPrefix_returnsSame() {
        OctopusProperties octprop = getSample();

        OctopusProperties noctprop = octprop.filter("");

        assertEquals(noctprop, octprop);
    }

    @Test
    public void testPrintProperties() {

        Properties props = new Properties();
        props.setProperty("key", "value");
        props.setProperty("item", "value2");
        OctopusProperties octprop = new OctopusProperties(props);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        octprop.printProperties(new PrintStream(out), "");

        String s = out.toString();

        assertTrue(s.equals("key = value\nitem = value2\n"));

        out = new ByteArrayOutputStream();
        octprop.printProperties(new PrintStream(out), "NOOT");

        s = out.toString();

        assertTrue(s.equals(""));

        out = new ByteArrayOutputStream();
        octprop.printProperties(new PrintStream(out), "key");

        s = out.toString();

        assertTrue(s.equals("key = value\n"));

        out = new ByteArrayOutputStream();
        octprop.printProperties(new PrintStream(out), null);

        s = out.toString();

        assertTrue(s.equals("key = value\nitem = value2\n"));
    }

    @Test
    public void testToString() {
        OctopusProperties octprop = getSample();

        assertEquals(octprop.toString(), "OctopusProperties [properties={key=value, item=value2}]");
    }

    public OctopusProperties getSample() {
        Properties props = new Properties();
        props.setProperty("key", "value");
        props.setProperty("item", "value2");
        OctopusProperties octprop = new OctopusProperties(props);
        return octprop;
    }

    @Test
    public void testGetPropertyNames() {
        OctopusProperties octprop = getSample();
        String[] names = octprop.getPropertyNames();
        String[] expected_names = new String[] { "item", "key" };
        assertTrue(Arrays.equals(names, expected_names));
    }

    @Test
    public void testEqualsObject() {

        OctopusProperties octprop1 = getSample();

        boolean b = octprop1.equals(octprop1);

        assertTrue(b);

        OctopusProperties octprop2 = getSample();

        b = octprop1.equals(octprop2);

        assertTrue(b);

        Properties props = new Properties();
        props.setProperty("key", "value");

        OctopusProperties octprop3 = new OctopusProperties(props);
        b = octprop1.equals(octprop3);

        assertFalse(b);
    }

    @Test
    public void testSetProperty_Unsupported() {
        OctopusProperties octprop = new OctopusProperties();
        try {
            octprop.setProperty("key", "value");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().equals("setting properties unsupported in ImmutableTypedProperties"));
        }
    }

    @Test
    public void testPutObject_Unsupported() {
        OctopusProperties octprop = new OctopusProperties();
        try {
            octprop.put("key", "value");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().equals("setting properties unsupported in ImmutableTypedProperties"));
        }
    }

    @Test
    public void testRemove_Unsupported() {
        OctopusProperties octprop = new OctopusProperties();
        try {
            octprop.remove("key");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().equals("setting properties unsupported in ImmutableTypedProperties"));
        }
    }

    @Test
    public void testPutAllMapOfQextendsObjectQextendsObject() {

        OctopusProperties octprop = new OctopusProperties();

        Map<String, String> tmp = new HashMap<String, String>();
        tmp.put("key", "value");

        try {
            octprop.putAll(tmp);
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().equals("setting properties unsupported in ImmutableTypedProperties"));
        }
    }

    @Test
    public void testLoad_Reader_Unsupported() throws IOException {
        OctopusProperties octprop = new OctopusProperties();
        try {
            octprop.load(mock(Reader.class));
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().equals("setting properties unsupported in ImmutableTypedProperties"));
        }
    }

    @Test
    public void testLoad_InputStream_Unsupported() throws IOException {
        OctopusProperties octprop = new OctopusProperties();
        try {
            octprop.load(mock(InputStream.class));
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().equals("setting properties unsupported in ImmutableTypedProperties"));
        }
    }

    @Test
    public void testLoadFromXML_Unsupported() throws InvalidPropertiesFormatException, IOException {
        OctopusProperties octprop = new OctopusProperties();
        try {
            octprop.loadFromXML(mock(InputStream.class));
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().equals("setting properties unsupported in ImmutableTypedProperties"));
        }
    }
}
