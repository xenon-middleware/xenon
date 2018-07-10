package nl.esciencecenter.xenon.adaptors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;

public class TestAdaptor {

    @Test
    public void test_adaptor_name() {
        Adaptor a = new MockAdaptor("NAME", "DESC", new String[] { "loc" }, null);
        assertEquals("NAME", a.getName());
    }

    @Test
    public void test_adaptor_description() {
        Adaptor a = new MockAdaptor("NAME", "DESC", new String[] { "loc" }, null);
        assertEquals("DESC", a.getDescription());
    }

    @Test
    public void test_adaptor_locations() {
        String[] locations = new String[] { "loc" };
        Adaptor a = new MockAdaptor("NAME", "DESC", locations, null);
        assertArrayEquals(locations, a.getSupportedLocations());
    }

    @Test
    public void test_adaptor_() {
        String[] locations = new String[] { "loc" };
        XenonPropertyDescription[] props = new XenonPropertyDescription[] { new XenonPropertyDescription("TEST", Type.BOOLEAN, "false", "bla") };
        Adaptor a = new MockAdaptor("NAME", "DESC", locations, props);
        assertArrayEquals(props, a.getSupportedProperties());
    }
}
