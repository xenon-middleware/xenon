package nl.esciencecenter.xenon.adaptors.webdav;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.adaptors.FileTestConfig;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;

public class WebdavFileTestConfig extends FileTestConfig {

    private String username;
    private char[] password;

    private static String scheme = "http";
    private String correctLocation;
    private String wrongLocation;
    private String correctLocationWrongUser;
    private String correctLocationWithUser;
    private String privateLocation;

    protected WebdavFileTestConfig(String adaptorName, String configfile) throws FileNotFoundException, IOException {
        super(adaptorName, configfile);
    }

    public WebdavFileTestConfig(String configfile) throws Exception {
        super(scheme, configfile);

        username = getPropertyOrFail(p, "test.webdav.user");
        password = getPropertyOrFail(p, "test.webdav.password").toCharArray();

        privateLocation = getPropertyOrFail(p, "test.webdav.privatelocation");
        correctLocation = getPropertyOrFail(p, "test.webdav.publiclocation");
        wrongLocation = username + "@doesnotexist71093880.com";
        correctLocationWrongUser = "incorrect@" + getPropertyOrFail(p, "test.webdav.publiclocation");
        correctLocationWithUser = username + "@" + getPropertyOrFail(p, "test.webdav.publiclocation");
    }

    @Override
    public FileSystem getTestFileSystem(Files files, Credentials credentials) throws Exception {
        return files.newFileSystem(scheme, correctLocation, getDefaultCredential(credentials), null);
    }

    @Override
    public Path getWorkingDir(Files files, Credentials credentials) throws Exception {
        return files.newFileSystem(scheme, correctLocation, getNonDefaultCredential(credentials), null).getEntryPath();
    }

    @Override
    public boolean supportsPosixPermissions() {
        return false;
    }

    @Override
    public boolean supportsSymboliclinks() {
        return false;
    }

    @Override
    public String getScheme() throws Exception {
        return scheme;
    }

    @Override
    public String getCorrectLocation() throws Exception {
        return correctLocation;
    }

    @Override
    public String getWrongLocation() throws Exception {
        return wrongLocation;
    }

    @Override
    public String getCorrectLocationWithUser() throws Exception {
        return correctLocationWithUser;
    }

    @Override
    public String getCorrectLocationWithWrongUser() throws Exception {
        return correctLocationWrongUser;
    }

    @Override
    public String getNonDefaultCredentialLocation() throws Exception {
        return privateLocation;
    }

    @Override
    public Credential getDefaultCredential(Credentials credentials) throws Exception {
        return credentials.getDefaultCredential(scheme);
    }

    @Override
    public Map<String, String> getDefaultProperties() throws Exception {
        return null;
    }

    @Override
    public Credential getNonDefaultCredential(Credentials credential) throws Exception {
        return getPasswordCredential(credential);
    }

    @Override
    public Credential getPasswordCredential(Credentials credentials) throws Exception {
        return credentials.newPasswordCredential(scheme, username, password, new HashMap<String, String>());
    }

    @Override
    public boolean supportNullCredential() {
        return true;
    }

    @Override
    public boolean supportNonDefaultCredential() {
        return true;
    }
}
