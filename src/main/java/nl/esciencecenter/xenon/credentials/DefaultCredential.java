package nl.esciencecenter.xenon.credentials;

/**
 * This class represents the default credential that may be used by the various adaptors. 
 * 
 * It mainly serves as a placeholder to indicate that the adaptor must revert to whatever default behavior it defines.   
 */
public class DefaultCredential implements Credential {

	@Override
	public String getUsername() {
		return System.getProperty("user.name");
	}
}
