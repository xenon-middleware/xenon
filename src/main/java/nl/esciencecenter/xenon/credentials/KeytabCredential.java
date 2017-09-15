package nl.esciencecenter.xenon.credentials;

public class KeytabCredential implements Credential {

    private final String username;
    private final String keytabFile;

    public KeytabCredential(String username, String keytabFile) {
        this.username = username;
        this.keytabFile = keytabFile;
    }


    @Override
    public String getUsername() {
        return username;
    }

    public String getKeytabFile() {
        return keytabFile;
    }
}
