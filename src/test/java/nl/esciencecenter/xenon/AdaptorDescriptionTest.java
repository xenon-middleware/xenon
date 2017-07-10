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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;

import nl.esciencecenter.xenon.XenonPropertyDescription.Type;

public class AdaptorDescriptionTest {
	
	@Test 
	public void test_name() {
		String [] loc = new String [] { "HERE", "THERE" };
		AdaptorDescription d = new AdaptorDescription("NAME", "DESC", loc, null);
		assertEquals("NAME", d.getName());
	}
	
	@Test 
	public void test_description() {
		String [] loc = new String [] { "HERE", "THERE" };
		AdaptorDescription d = new AdaptorDescription("NAME", "DESC", loc, null);
		assertEquals("DESC", d.getDescription());
	}


	@Test 
	public void test_location() {
		String [] loc = new String [] { "HERE", "THERE" };
		AdaptorDescription d = new AdaptorDescription("NAME", "DESC", loc, null);
		assertArrayEquals(loc, d.getSupportedLocations());
	}
	
	@Test 
	public void test_toString() throws Exception {
		HashMap<String,String> p = new HashMap<>(); 
		p.put("aap", "noot");

		XenonPropertyDescription d = new XenonPropertyDescription("aap", Type.STRING, "empty", "test");
		
		XenonPropertyDescription [] da = new XenonPropertyDescription[] { d };
		
		String [] loc = new String [] { "HERE", "THERE" };
		AdaptorDescription desc = new AdaptorDescription("NAME", "DESC", loc, da);
		
		String expected = "AdaptorDescription [name=" + "NAME" + ", description=" + "DESC" + 
				", supportedLocations=" + Arrays.toString(loc) +
				", supportedProperties=" + Arrays.toString(da) + "]";

		assertEquals(expected, desc.toString());
	}
}
