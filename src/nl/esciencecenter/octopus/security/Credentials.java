package nl.esciencecenter.octopus.security;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.esciencecenter.octopus.exceptions.DeployRuntimeException;

/**
 * Container class for Credentials.
 * 
 * @author Niels Drost
 * 
 */
public class Credentials {

    private static final Logger logger = LoggerFactory.getLogger(Credentials.class);

    private final HashSet<Credential> credentials;

    private final boolean readOnly;

    public Credentials(boolean readOnly, Credentials... initialContent) {
        this.readOnly = readOnly;
        credentials = new HashSet<Credential>();

        logger.debug("new credentials, read-only = " + readOnly + " initial content = " + initialContent);

        if (initialContent != null) {
            for (Credentials element : initialContent) {
                if (element != null) {
                    logger.debug("adding new element = " + element);
                    credentials.addAll(element.credentials);
                }
            }
        }
    }

    public Credentials(boolean readOnly, Credential... initialContent) {
        this.readOnly = readOnly;
        credentials = new HashSet<Credential>();

        for (Credential element : initialContent) {
            credentials.add(element);
        }
    }

    public Credentials() {
        credentials = new HashSet<Credential>();
        readOnly = false;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public synchronized void add(Credential credential) {
        if (credential == null) {
            return;
        }
        if (readOnly) {
            throw new DeployRuntimeException("cannot add credential to read only credentials object", null, null);
        }
        credentials.add(credential);
    }

    public synchronized void remove(Credential credential) {
        if (readOnly) {
            throw new DeployRuntimeException("cannot remove credential from read only credentials object", null, null);
        }

        credentials.remove(credential);
    }

    public synchronized void addAll(Credentials moreCredentials) {
        if (moreCredentials == null) {
            return;
        }
        if (readOnly) {
            throw new DeployRuntimeException("cannot add credentials to read only credentials object", null, null);
        }
        this.credentials.addAll(moreCredentials.credentials);
    }

    public String toString() {
        return "credentials, read-only = " + readOnly + " content = " + credentials;
    }

}
