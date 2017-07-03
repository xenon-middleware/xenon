package nl.esciencecenter.xenon.credentials;

import static org.junit.Assert.*;

public class DefaultCredentialTest {

	@org.junit.Test
	public void test_username() throws Exception {
		DefaultCredential pwc = new DefaultCredential("username");
		assertEquals(pwc.getUsername(), "username");
	}
}
