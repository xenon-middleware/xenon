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
package nl.esciencecenter.xenon.adaptors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import nl.esciencecenter.xenon.InvalidPropertyException;
import nl.esciencecenter.xenon.PropertyTypeException;
import nl.esciencecenter.xenon.UnknownPropertyException;
import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;

public class XenonPropertiesTest {

    @Test
    public void testXenonProperties_supportsProperty_propertySet_true() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.STRING, "bla", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "value");

        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.supportsProperty("key"));
    }

    @Test
    public void testXenonProperties_supportsProperty_useDefault_true() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.STRING, "bla", "test property") };

        Map<String, String> props = new HashMap<>(0);

        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.supportsProperty("key"));
    }

    @Test
    public void testXenonProperties_supportsProperty_propertySet_false() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.STRING, "bla", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "value");

        XenonProperties xprop = new XenonProperties(valid, props);

        assertFalse(xprop.supportsProperty("aap"));
    }

    @Test
    public void testXenonProperties_supportsProperty_useDefault_false() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.STRING, "bla", "test property") };

        Map<String, String> props = new HashMap<>(2);

        XenonProperties xprop = new XenonProperties(valid, props);

        assertFalse(xprop.supportsProperty("aap"));
    }

    @Test
    public void testXenonProperties_propertySet_true() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.STRING, "bla", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "value");

        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.propertySet("key"));
    }

    @Test
    public void testXenonProperties_propertySet_false() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.STRING, "bla", "test property") };

        Map<String, String> props = new HashMap<>(2);

        XenonProperties xprop = new XenonProperties(valid, props);

        assertFalse(xprop.propertySet("key"));
    }

    @Test(expected = UnknownPropertyException.class)
    public void testXenonProperties_propertySet_fails() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.STRING, "bla", "test property") };

        Map<String, String> props = new HashMap<>(2);
        XenonProperties xprop = new XenonProperties(valid, props);

        assertFalse(xprop.propertySet("aap"));
    }

    @Test
    public void testXenonProperties_getProperty_propertySet() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.STRING, "bla", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "value");

        XenonProperties xprop = new XenonProperties(valid, props);

        assertEquals("value", xprop.getProperty("key"));
    }

    @Test
    public void testXenonProperties_getProperty_default() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.STRING, "bla", "test property") };

        Map<String, String> props = new HashMap<>(2);

        XenonProperties xprop = new XenonProperties(valid, props);

        assertEquals("bla", xprop.getProperty("key"));
    }

    @Test(expected = UnknownPropertyException.class)
    public void testXenonProperties_getProperty_fails() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.STRING, "bla", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "value");

        XenonProperties xprop = new XenonProperties(valid, props);
        xprop.getProperty("aap"); // throws exception
    }

    @Test
    public void testXenonProperties_fromProperties() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.STRING, "bla", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "value");

        XenonProperties xprop = new XenonProperties(valid, props);

        assertEquals("{key=value}", xprop.toString());
    }

    @Test(expected = UnknownPropertyException.class)
    public void testXenonProperties_fromDefaultsAndProperties_noOverlap() throws Exception {

        XenonPropertyDescription[] supportedProperties = new XenonPropertyDescription[] {
                new XenonPropertyDescription("key", Type.STRING, "value", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key2", "value2");

        new XenonProperties(supportedProperties, props);
    }

    @Test
    public void testXenonProperties_fromDefaultsAndProperties_withOverlap() throws Exception {

        XenonPropertyDescription[] supportedProperties = new XenonPropertyDescription[] {
                new XenonPropertyDescription("key", Type.STRING, "value", "test property"),
                new XenonPropertyDescription("key2", Type.STRING, "value", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key2", "value2");

        XenonProperties xprop = new XenonProperties(supportedProperties, props);

        assertEquals("{key2=value2, <<key=value>>}", xprop.toString());
    }

    @Test
    public void testGetBooleanProperty_true() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.BOOLEAN, "true", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "true");
        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.getBooleanProperty("key"));
    }

    @Test
    public void testGetBooleanProperty_false() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.BOOLEAN, "true", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "false");
        XenonProperties xprop = new XenonProperties(valid, props);

        assertFalse(xprop.getBooleanProperty("key"));
    }

    @Test
    public void testGetBooleanProperty_default() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.BOOLEAN, "true", "test property") };

        Map<String, String> props = new HashMap<>(2);
        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.getBooleanProperty("key"));
    }

    @Test(expected = InvalidPropertyException.class)
    public void testGetBooleanProperty_emptyString_False() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.BOOLEAN, "true", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "bla");
        new XenonProperties(valid, props);
    }

    @Test(expected = InvalidPropertyException.class)
    public void testGetBooleanProperty_invalidDefault() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.BOOLEAN, "aap", "test property") };

        Map<String, String> props = new HashMap<>(2);
        new XenonProperties(valid, props).getBooleanProperty("key");
    }

    @Test(expected = UnknownPropertyException.class)
    public void testGetBooleanProperty_invalidName() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.BOOLEAN, "true", "test property") };

        Map<String, String> props = new HashMap<>(2);
        new XenonProperties(valid, props).getBooleanProperty("noot");
    }

    @Test(expected = PropertyTypeException.class)
    public void testGetBooleanProperty_wrongType() throws Exception {

        XenonPropertyDescription[] supportedProperties = new XenonPropertyDescription[] {
                new XenonPropertyDescription("key", Type.STRING, "value", "test property") };

        Map<String, String> props = new HashMap<>(2);
        XenonProperties xprop = new XenonProperties(supportedProperties, props);
        xprop.getBooleanProperty("key"); // throws exception
    }

    @Test
    public void testGetIntProperty_1() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.INTEGER, "42", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1");
        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.getIntegerProperty("key") == 1);
    }

    @Test
    public void testGetIntProperty_default() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.INTEGER, "42", "test property") };

        Map<String, String> props = new HashMap<>(2);
        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.getIntegerProperty("key") == 42);
    }

    @Test(expected = InvalidPropertyException.class)
    public void testGetIntProperty_invalidDefault() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.INTEGER, "aap", "test property") };

        Map<String, String> props = new HashMap<>(2);
        new XenonProperties(valid, props).getIntegerProperty("key"); // throws exception
    }

    @Test
    public void testGetDoubleProperty_1() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.DOUBLE, "42.0", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1.0");
        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.getDoubleProperty("key") == 1.0);
    }

    @Test
    public void testGetDoubleProperty_default() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.DOUBLE, "42.0", "test property") };

        Map<String, String> props = new HashMap<>(0);
        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.getDoubleProperty("key") == 42.0);
    }

    @Test(expected = InvalidPropertyException.class)
    public void testGetDoubleProperty_invalidDefault() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.DOUBLE, "aap", "test property") };

        Map<String, String> props = new HashMap<>(0);
        new XenonProperties(valid, props).getDoubleProperty("key"); // throws exception
    }

    @Test
    public void testGetSizeProperty_g() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.SIZE, "42g", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1g");
        XenonProperties xprop = new XenonProperties(valid, props);

        // noinspection PointlessArithmeticExpression
        assertTrue(xprop.getSizeProperty("key") == 1L * 1024L * 1024L * 1024L);
    }

    @Test
    public void testGetSizeProperty_G() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.SIZE, "42g", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1G");
        XenonProperties xprop = new XenonProperties(valid, props);

        // noinspection PointlessArithmeticExpression
        assertTrue(xprop.getSizeProperty("key") == 1L * 1024L * 1024L * 1024L);
    }

    @Test
    public void testGetSizeProperty_m() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.SIZE, "42g", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1m");
        XenonProperties xprop = new XenonProperties(valid, props);

        // noinspection PointlessArithmeticExpression
        assertTrue(xprop.getSizeProperty("key") == 1L * 1024L * 1024L);
    }

    @Test
    public void testGetSizeProperty_M() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.SIZE, "42g", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1M");
        XenonProperties xprop = new XenonProperties(valid, props);

        // noinspection PointlessArithmeticExpression
        assertTrue(xprop.getSizeProperty("key") == 1L * 1024L * 1024L);
    }

    @Test
    public void testGetSizeProperty_k() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.SIZE, "42g", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1k");
        XenonProperties xprop = new XenonProperties(valid, props);

        // noinspection PointlessArithmeticExpression
        assertTrue(xprop.getSizeProperty("key") == 1L * 1024L);
    }

    @Test
    public void testGetSizeProperty_K() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.SIZE, "42g", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1K");
        XenonProperties xprop = new XenonProperties(valid, props);

        // noinspection PointlessArithmeticExpression
        assertTrue(xprop.getSizeProperty("key") == 1L * 1024L);
    }

    @Test
    public void testGetSizeProperty() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.SIZE, "42g", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1");
        XenonProperties xprop = new XenonProperties(valid, props);

        assertTrue(xprop.getSizeProperty("key") == 1L);
    }

    @Test(expected = InvalidPropertyException.class)
    public void testGetSizeProperty_X_fails() throws Exception {

        XenonPropertyDescription[] valid = new XenonPropertyDescription[] { new XenonPropertyDescription("key", Type.SIZE, "42g", "test property") };

        Map<String, String> props = new HashMap<>(2);
        props.put("key", "1X");
        XenonProperties xprop = new XenonProperties(valid, props);
        xprop.getSizeProperty("key"); // throws exception
    }

    @Test
    public void testXenonProperties_filter_withPropertiesSet() throws Exception {

        XenonPropertyDescription[] supportedProperties = new XenonPropertyDescription[] {
                new XenonPropertyDescription("aap.key", Type.STRING, "aap", "test property"),
                new XenonPropertyDescription("noot.key", Type.STRING, "noot", "test property") };

        Map<String, String> props = new HashMap<>(3);
        props.put("aap.key", "aap2");
        props.put("noot.key", "noot2");

        XenonProperties xprop = new XenonProperties(supportedProperties, props).filter("aap");

        assertEquals("{aap.key=aap2}", xprop.toString());
    }

    @Test
    public void testXenonProperties_filter_noPropertiesSet() throws Exception {

        XenonPropertyDescription[] supportedProperties = new XenonPropertyDescription[] {
                new XenonPropertyDescription("aap.key", Type.STRING, "aap", "test property"),
                new XenonPropertyDescription("noot.key", Type.STRING, "noot", "test property") };

        Map<String, String> props = new HashMap<>(0);

        XenonProperties xprop = new XenonProperties(supportedProperties, props).filter("aap");

        assertEquals("{<<aap.key=aap>>}", xprop.toString());
    }

    @Test
    public void testXenonProperties_filter_wrongPrefix() throws Exception {

        XenonPropertyDescription[] supportedProperties = new XenonPropertyDescription[] {
                new XenonPropertyDescription("aap.key", Type.STRING, "aap", "test property"),
                new XenonPropertyDescription("noot.key", Type.STRING, "noot", "test property") };

        Map<String, String> props = new HashMap<>(0);

        XenonProperties xprop = new XenonProperties(supportedProperties, props).filter("bla");

        assertEquals("{}", xprop.toString());
    }

    @Test
    public void testXenonProperties_exclude_withPropertiesSet() throws Exception {

        XenonPropertyDescription[] supportedProperties = new XenonPropertyDescription[] {
                new XenonPropertyDescription("aap.key", Type.STRING, "aap", "test property"),
                new XenonPropertyDescription("noot.key", Type.STRING, "noot", "test property") };

        Map<String, String> props = new HashMap<>(0);
        props.put("aap.key", "aap2");
        props.put("noot.key", "noot2");

        XenonProperties xprop = new XenonProperties(supportedProperties, props).exclude("noot");

        assertEquals("{aap.key=aap2}", xprop.toString());
    }

    @Test
    public void testXenonProperties_exclude_noPropertiesSet() throws Exception {

        XenonPropertyDescription[] supportedProperties = new XenonPropertyDescription[] {
                new XenonPropertyDescription("aap.key", Type.STRING, "aap", "test property"),
                new XenonPropertyDescription("noot.key", Type.STRING, "noot", "test property") };

        Map<String, String> props = new HashMap<>(0);

        XenonProperties xprop = new XenonProperties(supportedProperties, props).exclude("noot");

        assertEquals("{<<aap.key=aap>>}", xprop.toString());
    }

    @Test
    public void testXenonProperties_exclude_wrongPrefix() throws Exception {

        XenonPropertyDescription[] supportedProperties = new XenonPropertyDescription[] {
                new XenonPropertyDescription("aap.key", Type.STRING, "aap", "test property") };

        Map<String, String> props = new HashMap<>(0);

        XenonProperties xprop = new XenonProperties(supportedProperties, props).exclude("bla");

        assertEquals("{<<aap.key=aap>>}", xprop.toString());
    }

}
