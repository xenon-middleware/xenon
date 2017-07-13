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
package nl.esciencecenter.xenon.filesystems;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;

import nl.esciencecenter.xenon.XenonPropertyDescription;
import nl.esciencecenter.xenon.XenonPropertyDescription.Type;

public class FileSystemDescriptionTest {

	@Test 
	public void test_name() {
		String [] loc = new String [] { "HERE", "THERE" };
		FileSystemAdaptorDescription d = new FileSystemAdaptorDescription("NAME", "DESC", loc, null, true);
		assertEquals("NAME", d.getName());
	}


	@Test 
	public void test_description() {
		String [] loc = new String [] { "HERE", "THERE" };
		FileSystemAdaptorDescription d = new FileSystemAdaptorDescription("NAME", "DESC", loc, null, true);
		assertEquals("DESC", d.getDescription());
	}


	@Test 
	public void test_location() {
		String [] loc = new String [] { "HERE", "THERE" };
		FileSystemAdaptorDescription d = new FileSystemAdaptorDescription("NAME", "DESC", loc, null, true);
		assertArrayEquals(loc, d.getSupportedLocations());
	}
	
	@Test 
	public void test_3rdPartyTrue() {
		String [] loc = new String [] { "HERE", "THERE" };
		FileSystemAdaptorDescription d = new FileSystemAdaptorDescription("NAME", "DESC", loc, null, true);
		assertTrue(d.supportsThirdPartyCopy());
	}
	
	@Test 
	public void test_3rdPartyFalse() {
		String [] loc = new String [] { "HERE", "THERE" };
		FileSystemAdaptorDescription d = new FileSystemAdaptorDescription("NAME", "DESC", loc, null, false);
		assertFalse(d.supportsThirdPartyCopy());
	}
	
	@Test 
	public void test_toString() throws Exception {
		HashMap<String,String> p = new HashMap<>(); 
		p.put("aap", "noot");

		XenonPropertyDescription d = new XenonPropertyDescription("aap", Type.STRING, "empty", "test");
		
		XenonPropertyDescription [] da = new XenonPropertyDescription[] { d };
		
		String [] loc = new String [] { "HERE", "THERE" };
		FileSystemAdaptorDescription desc = new FileSystemAdaptorDescription("NAME", "DESC", loc, da, false);
		
		String expected = "FileAdaptorDescription [name=" + "NAME" + ", description=" + "DESC" + 
				", supportedLocations=" + Arrays.toString(loc) +
				", supportedProperties=" + Arrays.toString(da) +  
				", supportsThirdPArtyCopy=" + false + "]";

		assertEquals(expected, desc.toString());
	}
	
}
