package nl.esciencecenter.xenon.adaptors.gftp;

import java.util.Map;

import nl.esciencecenter.xenon.credentials.Credential;

/**
 * Recent implementations support GridFTP with SSH authentication.
 * 
 * TODO:
 * 
 * @author Piter T. de Boer
 */
public class GridSshCredential implements Credential {

    @Override
    public String getAdaptorName() {
        return GftpAdaptor.ADAPTOR_NAME;
    }

    @Override
    public Map<String, String> getProperties() {
        return null;
    }

}
