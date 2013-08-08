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

package nl.esciencecenter.octopus.engine.credentials;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class ProxyCredentialImplementationTest {

    @org.junit.Test
    public void test_getters_and_setters() throws Exception {
        ProxyCredentialImplementation pwc = new ProxyCredentialImplementation("test", "id1", null, "host", 42, "username",
                "password".toCharArray());

        assertEquals(pwc.getAdaptorName(), "test");
        assertEquals(pwc.getUniqueID(), "id1");
        assertEquals(pwc.getUsername(), "username");
        assertEquals(pwc.getHost(), "host");
        assertEquals(pwc.getPort(), 42);
        assertEquals(pwc.getProperties(), new HashMap<String, String>());

        assertTrue(Arrays.equals(pwc.getPassword(), "password".toCharArray()));
        assertTrue(pwc.toString().equals(
                "ProxyCredentialImplementation [adaptorName=test, username=username, host=host, port=42]"));
    }
}
