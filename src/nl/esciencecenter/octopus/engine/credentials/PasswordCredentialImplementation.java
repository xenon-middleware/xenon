package nl.esciencecenter.octopus.engine.credentials;

import nl.esciencecenter.octopus.engine.OctopusProperties;

/**
 * A security context based upon user name, password combination.
 */
public class PasswordCredentialImplementation extends CredentialImplementation {

    /**
     * Constructs a {@link PasswordCredentialImplementation} with the given <code>username</code> and <code>password</code>.
     * 
     * @param username
     *            the username
     * @param password
     *            the password for the given username
     */
    public PasswordCredentialImplementation(String adaptorName, OctopusProperties properties, String username, String password) {
        super(adaptorName, properties, username, password);
    }

    public String toString() {
        return "PasswordSecurityContext(" + ((username == null) ? "" : ("username = " + username));
    }
}
