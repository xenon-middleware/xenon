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
