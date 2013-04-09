package nl.esciencecenter.octopus.engine.credentials;

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
    public PasswordCredential(String username, String password) {
        super(username, password);
    }

    /**
     * Check two SecurityContexts for equality.
     * 
     * @param obj
     *            the object to compare this with
     * @return true if the objects are semantically equal
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof PasswordCredential)) {
            return false;
        }
        PasswordCredential other = (PasswordCredential) obj;
        return other.username.equals(username) && other.password.equals(password);
    }

    /**
     * Returns a clone of this context.
     * 
     * @return the clone of this security context
     */
    public Object clone() throws CloneNotSupportedException {
        return new PasswordCredential(username, password);
    }

    public int hashCode() {
        return username.hashCode();
    }

    public String toString() {
        return "PasswordSecurityContext(" + ((username == null) ? "" : ("username = " + username))
                + ((dataObjects == null) ? "" : (" userdata = " + dataObjects)) + ")";
    }
}
