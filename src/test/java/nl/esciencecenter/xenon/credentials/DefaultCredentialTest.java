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
package nl.esciencecenter.xenon.credentials;

import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultCredentialTest {

	@Test
	public void test_username() throws Exception {
		DefaultCredential dc = new DefaultCredential("username");
		assertEquals("username", dc.getUsername());
	}

	@Test
	public void test_username_default_currentuserfromsystem() throws Exception {
		DefaultCredential dc = new DefaultCredential();
		String expected = System.getProperty("user.name");
		assertEquals(expected, dc.getUsername());
	}

	@Test
	public void test_hascode() throws Exception {
		DefaultCredential dc1 = new DefaultCredential("username");
		DefaultCredential dc2 = new DefaultCredential("username");
		assertEquals(dc1.hashCode(), dc2.hashCode());
	}

	@Test
	public void test_equals_sameobj() {
		DefaultCredential dc = new DefaultCredential("username");
		assertTrue(dc.equals(dc));
	}

	@Test
	public void test_equals() {
		DefaultCredential dc1 = new DefaultCredential("username");
		DefaultCredential dc2 = new DefaultCredential("username");
		assertTrue(dc1.equals(dc2));
	}

	@Test
	public void test_equals_diffclass() {
		DefaultCredential dc1 = new DefaultCredential("username");
		String dc2 = "not the same class";
		assertFalse(dc1.equals(dc2));
	}

	@Test
	public void test_equals_null() {
		DefaultCredential dc1 = new DefaultCredential("username");
		assertFalse(dc1.equals(null));
	}

	@Test
	public void test_equals_diffusername() {
		DefaultCredential dc1 = new DefaultCredential("username1");
		DefaultCredential dc2 = new DefaultCredential("username2");
		assertFalse(dc1.equals(dc2));
	}
}
