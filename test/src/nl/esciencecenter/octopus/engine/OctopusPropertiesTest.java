package nl.esciencecenter.octopus.engine;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;

public class OctopusPropertiesTest {

    @Test
    public void testClear_Unsupported() {
        OctopusProperties octprop = new OctopusProperties();
        try {
            octprop.clear();
        } catch (UnsupportedOperationException e) {
            assertThat(e.getMessage(), is("setting properties unsupported in ImmutableTypedProperties"));
        }
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
    public void testGetIntProperty_int_int() {
        Properties props = new Properties();
        props.setProperty("key", "42");
        OctopusProperties octprop = new OctopusProperties(props);
        int expected = 42;
        assertEquals(expected, octprop.getIntProperty("key"));
    }

    @Test
    public void testGetIntProperty_null_UndefinedException() {
        Properties props = new Properties();
        OctopusProperties octprop = new OctopusProperties(props);
        try {
            octprop.getIntProperty("key");
            fail("No NumberFormatException thrown");
        } catch (NumberFormatException e) {
            assertThat(e.getMessage(), is("property undefined: key"));
        }
    }

    @Test
    public void testGetIntProperty_null_IntParseException() {
        Properties props = new Properties();
        props.setProperty("key", "foo");
        OctopusProperties octprop = new OctopusProperties(props);
        try {
            octprop.getIntProperty("key");
            fail("No NumberFormatException thrown");
        } catch (NumberFormatException e) {
            assertThat(e.getMessage(), is("Integer expected for property key, not \"foo\""));
        }
    }

    @Test
    public void testGetIntProperty_withDefault() {
        OctopusProperties octprop = new OctopusProperties();
        int result = octprop.getIntProperty("key", 42);
        int expected = 42;
        assertThat(result, is(expected));
    }

    @Test
    public void testGetIntProperty_SetAndDefault_returnsSet() {
        Properties props = new Properties();
        props.setProperty("key", "13");
        OctopusProperties octprop = new OctopusProperties();
        int result = octprop.getIntProperty("key", 42);
        int expected = 13;
        assertThat(result, is(expected));
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
    public void testGetSizeProperty() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetSizePropertyStringLong() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetStringList_emptyValue_emptyList() {
        OctopusProperties octprop = new OctopusProperties();

        String[] result = octprop.getStringList("key");

        String[] expected = new String[] { };
        assertThat(result, is(expected));
    }

    @Test
    public void testGetStringList_emptyValue_Defaults() {
        OctopusProperties octprop = new OctopusProperties();
        String[] defaults = new String[] { "value1", "value2", "value3" };
        String[] result = octprop.getStringList("key", ", ", defaults);

        String[] expected = new String[] { "value1", "value2", "value3" };
        assertThat(result, is(expected));
    }

    @Test
    public void testGetString_withDefaultDelimiter() {
        Properties props = new Properties();
        props.setProperty("key", "value1, value2, value3");
        OctopusProperties octprop = new OctopusProperties(props);

        String[] result = octprop.getStringList("key");

        String[] expected = new String[] { "value1", "value2", "value3" };
        assertThat(result, is(expected));
    }

    @Test
    public void testGetString_withDelimiter() {
        Properties props = new Properties();
        props.setProperty("key", "value1, value2, value3");
        OctopusProperties octprop = new OctopusProperties(props);

        String[] result = octprop.getStringList("key", ", ");

        String[] expected = new String[] { "value1", "value2", "value3" };
        assertThat(result, is(expected));
    }

    @Test
    public void testFilter_filledWithKeyAndItem_returnsKey() {
        OctopusProperties octprop = getSample();

        OctopusProperties noctprop = octprop.filter("k");

        assertEquals(noctprop.toString(), "key = value\n");
    }

    @Test
    public void testFilter_emptyPrefix_returnsSame() {
        OctopusProperties octprop = getSample();

        OctopusProperties noctprop = octprop.filter("");

        assertEquals(noctprop, octprop);
    }

    @Test
    public void testPrintProperties() {
        fail("Not yet implemented");
    }

    @Test
    public void testToString() {
        OctopusProperties octprop = getSample();

        assertEquals(octprop.toString(), "key = value\nitem = value2\n");
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
        assertThat(names, is(expected_names));
    }

    @Test
    public void testEqualsObject() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetProperty_Unsupported() {
        OctopusProperties octprop = new OctopusProperties();
        try {
            octprop.setProperty("key", "value");
        } catch (UnsupportedOperationException e) {
            assertThat(e.getMessage(), is("setting properties unsupported in ImmutableTypedProperties"));
        }
    }

    @Test
    public void testPutObject_Unsupported() {
        OctopusProperties octprop = new OctopusProperties();
        try {
            octprop.put("key", "value");
        } catch (UnsupportedOperationException e) {
            assertThat(e.getMessage(), is("setting properties unsupported in ImmutableTypedProperties"));
        }
    }

    @Test
    public void testRemove_Unsupported() {
        OctopusProperties octprop = new OctopusProperties();
        try {
            octprop.remove("key");
        } catch (UnsupportedOperationException e) {
            assertThat(e.getMessage(), is("setting properties unsupported in ImmutableTypedProperties"));
        }
    }

    @Test
    public void testPutAllMapOfQextendsObjectQextendsObject() {
        fail("Not yet implemented");
    }

    @Test
    public void testLoad_Reader_Unsupported() throws IOException {
        OctopusProperties octprop = new OctopusProperties();
        try {
            octprop.load(mock(Reader.class));
        } catch (UnsupportedOperationException e) {
            assertThat(e.getMessage(), is("setting properties unsupported in ImmutableTypedProperties"));
        }
    }

    @Test
    public void testLoad_InputStream_Unsupported() throws IOException {
        OctopusProperties octprop = new OctopusProperties();
        try {
            octprop.load(mock(InputStream.class));
        } catch (UnsupportedOperationException e) {
            assertThat(e.getMessage(), is("setting properties unsupported in ImmutableTypedProperties"));
        }
    }

    @Test
    public void testLoadFromXML_Unsupported() throws InvalidPropertiesFormatException, IOException {
        OctopusProperties octprop = new OctopusProperties();
        try {
            octprop.loadFromXML(mock(InputStream.class));
        } catch (UnsupportedOperationException e) {
            assertThat(e.getMessage(), is("setting properties unsupported in ImmutableTypedProperties"));
        }
    }
}
