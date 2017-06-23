package nl.esciencecenter.xenon.credentials;

/**
 * This class represents the default credential that may be used by the various adaptors. 
 * 
 * It mainly serves as a placeholder to indicate that the adaptor must revert to whatever default behavior it defines.   
 */
public class DefaultCredential implements Credential {

	private final String username;
	
	public DefaultCredential() {
		username = System.getProperty("user.name");
	}
	
	public DefaultCredential(String username) {
		this.username = username;
	}
	
	@Override
	public String getUsername() {
		return username;
	}
}
