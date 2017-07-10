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
