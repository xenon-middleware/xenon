package nl.esciencecenter.octopus.engine.credentials;

import nl.esciencecenter.octopus.engine.OctopusProperties;

/**
 * A security context based upon user name, password combination.
 */
public class PasswordCredential extends Credential {

    /**
     * Constructs a {@link PasswordCredential} with the given <code>username</code> and <code>password</code>.
     * 
     * @param username
     *            the username
     * @param password
     *            the password for the given username
     */
    public PasswordCredential(String adaptorName, OctopusProperties properties, String username, String password) {
        super(adaptorName, properties, username, password);
    }

    public String toString() {
        return "PasswordSecurityContext(" + ((username == null) ? "" : ("username = " + username));
    }
}
