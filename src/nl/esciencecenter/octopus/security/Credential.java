package nl.esciencecenter.octopus.security;

import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * A container for security Information.
 * <p>
 * Each context has a data object associated with it. The data object is opaque to the Octopus API and is used and manipulated by
 * adaptors based upon their interpretation of the context. A globus adaptor could store a GSI credential in it, while an SSH
 * adaptor could store the private key in the data object.
 * <p>
 * Notes restrict the access to the context because it is a bad idea to broadcast passwords / credentials, so it is useful to
 * restrict those to a set of hosts or adaptors.
 * 
 */
public abstract class Credential implements Cloneable {

    /** the user name to use for this context */
    protected String username;

    /**
     * This member variables holds the passphrase of the SecurityContext
     */
    protected String password = null;

    /**
     * The data objects are used to store adaptor-specific information in the security context. They are transparent for Octopus
     * users, and should not be touched.
     */
    protected HashMap<String, Object> dataObjects = new HashMap<String, Object>();

    protected HashMap<String, String> notes = new HashMap<String, String>();

    public abstract boolean equals(Object obj);

    public abstract int hashCode();

    protected Credential(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Returns a clone of this context.
     * 
     * @return the clone of this security context
     */
    public abstract Object clone() throws CloneNotSupportedException;

    /**
     * This method is used by adaptors to get adaptor-specific data associated with this context.
     * 
     * @param key
     *            the key that was used to store the data
     * @return the data object
     */
    public synchronized Object getDataObject(String key) {
        return dataObjects.get(key);
    }

    /**
     * This method is used by adaptors to get adaptor-specific data associated with this context.
     * 
     * @param key
     *            the key that should be used to store the data
     * @param data
     *            the data object that has to be associated with the context
     */
    public synchronized void putDataObject(String key, Object data) {
        dataObjects.put(key, data);
    }

    /**
     * This method is used by adaptors to remove adaptor-specific data associated with this context.
     * 
     * @param key
     *            the key that was used to store the data
     */
    public synchronized void removeDataObject(String key) {
        dataObjects.remove(key);
    }

    /**
     * Add notes to this context. Notes are used to give information to the adaptors. An example is:
     * <p>
     * <code>addNote("host", "machine1.cs.vu.nl")</code>
     * <p>
     * to indicate that this context is only valid for the machine above. Notes restrict and precise the usage of the
     * authentication data. So an adaptor can verify in advance the usefulness of the authentication data for the host it want to
     * access (e.g. for job submission).
     * <p>
     * If no notes are defined, adaptors are allowed to use the context for any machine. If at least one note is defined, access
     * is restricted to the adaptor/machine defined in the note(s)
     * <p>
     * the set of notes known so far is:
     * <p>
     * <TABLE border="2" frame="box" rules="groups" summary="Minimum set of * supported notes">
     * <CAPTION>Minimum set of supported key/value pairs </CAPTION> <COLGROUP align="left"> <COLGROUP align="center"> <COLGROUP
     * align="left" > <THEAD valign="top">
     * <TR>
     * <TH>Key
     * <TH>Value
     * <TH>Example <TBODY>
     * <TR>
     * <TD><code>hosts</code>
     * <TD>{@link String}
     * <TD>"hostname1:port1,hostname2:port2", where the port is optional
     * <TR>
     * <TD><code>adaptors</code>
     * <TD>{@link String}
     * <TD>"adaptorName1,adaptorName2,..."
     * <TR>
     * </TABLE>
     * <p>
     * There may be more notes that are understood by a particular adaptor.
     * 
     * @param key
     *            the name of the note
     * @param value
     *            the value of the note
     */
    public final void addNote(String key, String value) {
        notes.put(key, value);
    }

    /**
     * Checks whether there exists a note with the given <code>key</code>.
     * 
     * @param key
     *            the key to look for
     * @return true if a note exists with the given key
     */
    public final boolean containsNoteKey(String key) {
        return this.notes.containsKey(key);
    }

    /**
     * Gets the value belonging to the note with the give <code>key</code>.
     * 
     * @param key
     *            the key to look for
     * @return the note attached to this key
     */
    public final String getNoteValue(String key) {
        return this.notes.get(key);
    }

    /**
     * This method checks the notes associated with this security context, and sees if the context is usable by a particular
     * adaptor.
     * 
     * @param adaptorName
     *            the name of the adaptor that wants to use this context
     * @param host
     *            the destination hostname
     * @param port
     *            the destination port
     * @return true: the context is valid
     */
    public boolean isValidFor(String adaptorName, String host, int port) {
        if (notes.size() == 0) {
            return true;
        }

        // check if the context is limited to a set of adaptors.
        boolean allowed = false;
        String adaptorList = notes.get("adaptors");

        if (adaptorList != null) {
            StringTokenizer tokens = new StringTokenizer(adaptorList, ",");
            int count = tokens.countTokens();

            for (int i = 0; i < count; i++) {
                String allowedAdaptor = tokens.nextToken();

                if (allowedAdaptor.equalsIgnoreCase(adaptorName)) {
                    allowed = true;

                    break;
                }
            }
        } else { // it is not limited so a set of adaptors
            allowed = true;
        }

        if (!allowed) {
            /*
             * if (OctopusEngine.DEBUG) { System.err .println("securityContext is
             * not allowed for adaptor " + adaptorName + " for host " + host + "
             * and port " + port); }
             */
            return false; // it was limited, and the adaptor calling this

            // method was not on the list.
        }

        // this adaptor is allowed to use this context.
        // now check if this host is
        allowed = false;

        String hostList = notes.get("hosts");

        if (hostList != null) {
            StringTokenizer tokens = new StringTokenizer(hostList, ",");

            for (int i = 0; i < tokens.countTokens(); i++) {
                int allowedPort = -1;
                String allowedHost = tokens.nextToken();
                int pos = allowedHost.indexOf(":");

                if (pos > 0) { // there is a port specified
                    allowedPort = Integer.parseInt(allowedHost.substring(pos + 1));
                    allowedHost = allowedHost.substring(0, pos);
                }

                if (allowedHost.equalsIgnoreCase(host)) {
                    if ((allowedPort < 0) || (allowedPort == port)) {
                        allowed = true;
                    }

                    break;
                }
            }
        } else {
            allowed = true;
        }

        if (!allowed) {
            /*
             * if (OctopusEngine.DEBUG) { System.err .println("securityContext is
             * not allowed for adaptor " + adaptorName + " for host " + host + "
             * and port " + port); }
             */
        }

        return allowed;
    }

    /**
     * Returns the user name.
     * 
     * @return the user name
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the user name.
     * 
     * @param username
     *            the user name to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password.
     * 
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     * 
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
