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
package nl.esciencecenter.xenon;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.util.Properties;


import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.NoSuchXenonException;

import org.junit.Test;

public class XenonFactoryTest {

    @Test
    public void testNewXenonFactory() throws Exception {
        // Test to satisfy coverage.
        Constructor<XenonFactory> constructor = XenonFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void testNewXenon() throws XenonException {
        Xenon x = XenonFactory.newXenon(null);
        assertTrue(x.getProperties().equals(new Properties()));
    }

    @Test
    public void testEndXenon() throws XenonException {
        Xenon x = XenonFactory.newXenon(null);
        XenonFactory.endXenon(x);
    }

    @Test(expected = NoSuchXenonException.class)
    public void testEndXenon2() throws XenonException {
        Xenon x = XenonFactory.newXenon(null);
        XenonFactory.endXenon(x);
        XenonFactory.endXenon(x);
    }

    @Test(expected = NoSuchXenonException.class)
    public void testEndAll() throws XenonException {
        Xenon x1 = XenonFactory.newXenon(null);
        Xenon x2 = XenonFactory.newXenon(null);

        XenonFactory.endAll();
        XenonFactory.endXenon(x1);
        XenonFactory.endXenon(x2);        
    }
    
    @Test
    public void testProperties() throws Exception {
        XenonPropertyDescription [] tmp = XenonFactory.getSupportedProperties();
   
        assertNotNull(tmp);
       
    }
}
