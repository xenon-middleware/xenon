package nl.esciencecenter.xenon;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import nl.esciencecenter.xenon.XenonPropertyDescription.Type;

public class XenonPropertyDescriptionTest {

	@Test
	public void test_name() {
		XenonPropertyDescription p = new XenonPropertyDescription("NAME", Type.STRING, "EMPTY", "DESC");
		assertEquals("NAME", p.getName());		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test_createFailsNameNull() {
		new XenonPropertyDescription(null, Type.STRING, "EMPTY", "DESC");
	}
	

	@Test(expected=IllegalArgumentException.class)
	public void test_createFailsTypeNull() {
		new XenonPropertyDescription("NAME", null, "EMPTY", "DESC");
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_createFailsDescriptionNull() {
		new XenonPropertyDescription("NAME", Type.STRING, "EMPTY", null);
	}

	@Test
	public void test_valueOfType() {
		Type t = Type.valueOf("STRING");
		assertEquals(Type.STRING, t);
	}
	
	@Test
	public void test_description() {
		XenonPropertyDescription p = new XenonPropertyDescription("NAME", Type.STRING, "EMPTY", "DESC");
		assertEquals("DESC", p.getDescription());		
	}

	@Test
	public void test_default() {
		XenonPropertyDescription p = new XenonPropertyDescription("NAME", Type.STRING, "EMPTY", "DESC");
		assertEquals("EMPTY", p.getDefaultValue());		
	}

	@Test
	public void test_type() {
		XenonPropertyDescription p = new XenonPropertyDescription("NAME", Type.STRING, "EMPTY", "DESC");
		assertEquals(Type.STRING, p.getType());		
	}
	
}
