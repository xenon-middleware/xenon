package nl.esciencecenter.octopus.engine;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

public class OctopusPropertiesTest {

    @Test
    public void testClear() {
        fail("Not yet implemented");
    }

    @Test
    public void testOctopusProperties_fromProperties() {
        Properties props = new Properties();
        props.setProperty("key", "value");

        OctopusProperties octprop = new OctopusProperties(props);

        assertEquals(octprop.toString(), "key = value\n");
    }

    @Test
    public void testOctopusProperties_fromMultipleProperties_noOverlap() {
        Properties props = new Properties();
        props.setProperty("key", "value");
        Properties props2 = new Properties();
        props2.setProperty("key2", "value2");

        OctopusProperties octprop = new OctopusProperties(props, props2);

        assertEquals(octprop.toString(), "key = value\nkey2 = value2\n");
    }

    @Test
    public void testOctopusProperties_fromMultipleProperties_withOverlap() {
        Properties props = new Properties();
        props.setProperty("key", "value");
        Properties props2 = new Properties();
        props2.setProperty("key", "value2");

        OctopusProperties octprop = new OctopusProperties(props, props2);

        assertEquals(octprop.toString(), "key = value2\n");
    }

    @Test
    public void testOctopusProperties_fromDefaultsAndProperties_noOverlap() {
        String[][] defaults = new String[][] {
                { "key", "value" } };
        Properties props = new Properties();
        props.setProperty("key2", "value2");

        OctopusProperties octprop = new OctopusProperties(defaults, props);

        assertEquals(octprop.toString(), "key = value\nkey2 = value2\n");
    }

    @Test
    public void testOctopusProperties_fromDefaultsAndProperties_withOverlap() {
        String[][] defaults = new String[][] {
                { "key", "value" } };
        Properties props = new Properties();
        props.setProperty("key", "value2");

        OctopusProperties octprop = new OctopusProperties(defaults, props);

        assertEquals(octprop.toString(), "key = value2\n");
    }

    @Test
    public void testLoadFromClassPath() {
        fail("Not yet implemented");
    }

    @Test
    public void testLoadFromFile() {
        fail("Not yet implemented");
    }

    @Test
    public void testLoadFromHomeFile() {
        fail("Not yet implemented");
    }

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
    public void testGetBooleanPropertyStringBoolean() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetIntPropertyString() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetIntPropertyStringInt() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetLongPropertyString() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetLongPropertyStringLong() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetShortPropertyString() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetShortPropertyStringShort() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetDoublePropertyString() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetDoublePropertyStringDouble() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetFloatPropertyString() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetFloatPropertyStringFloat() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetSizePropertyString() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetSizePropertyStringLong() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetStringListString() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetStringListStringString() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetStringListStringStringStringArray() {
        fail("Not yet implemented");
    }

    @Test
    public void testFilter() {
        fail("Not yet implemented");
    }

    @Test
    public void testPrintProperties() {
        fail("Not yet implemented");
    }

    @Test
    public void testToString() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetPropertyNames() {
        fail("Not yet implemented");
    }

    @Test
    public void testEqualsObject() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetPropertyStringString() {
        fail("Not yet implemented");
    }

    @Test
    public void testPutObjectObject() {
        fail("Not yet implemented");
    }

    @Test
    public void testRemoveObject() {
        fail("Not yet implemented");
    }

    @Test
    public void testPutAllMapOfQextendsObjectQextendsObject() {
        fail("Not yet implemented");
    }

    @Test
    public void testLoadReader() {
        fail("Not yet implemented");
    }

    @Test
    public void testLoadInputStream() {
        fail("Not yet implemented");
    }

    @Test
    public void testLoadFromXMLInputStream() {
        fail("Not yet implemented");
    }

}
