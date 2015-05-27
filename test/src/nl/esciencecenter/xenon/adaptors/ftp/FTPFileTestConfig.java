/*
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.esciencecenter.xenon.adaptors.ftp;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import nl.esciencecenter.xenon.adaptors.FileTestConfig;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;

/**
 * @author Christiaan Meijer <C.Meijer@esciencecenter.nl>
 *
 */
public class FTPFileTestConfig extends FileTestConfig {

    private String username;
    private char[] passwd;

    private static String scheme = "ftp";
    private String correctLocation;
    private String wrongLocation;
    private String correctLocationWrongUser;

    public FTPFileTestConfig(String configfile) throws Exception {

        super(scheme, configfile);

        String location = getPropertyOrFail(p, "test.ftp.location");

        username = getPropertyOrFail(p, "test.ftp.user");
        passwd = getPropertyOrFail(p, "test.ftp.password").toCharArray();

        correctLocation = username + "@" + location;
        wrongLocation = username + "@doesnotexist71093880.com";
        correctLocationWrongUser = "incorrect@" + location;
    }

    private String getPropertyOrFail(Properties p, String property) throws Exception {

        String tmp = p.getProperty(property);

        if (tmp == null) {
            throw new Exception("Failed to retrieve property " + property);
        }

        return tmp;
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
    public boolean supportsLocalCWD() {
        return true;
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
        return correctLocation;
    }

    @Override
    public String getCorrectLocationWithWrongUser() throws Exception {
        return correctLocationWrongUser;
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
    public Credential getPasswordCredential(Credentials credentials) throws Exception {
        return credentials.newPasswordCredential(scheme, username, passwd, new HashMap<String, String>());
    }

    @Override
    public Credential getInvalidCredential(Credentials credentials) throws Exception {
        return credentials.newPasswordCredential(scheme, username, "wrongpassword".toCharArray(), new HashMap<String, String>());
    }

    @Override
    public Credential getNonDefaultCredential(Credentials credentials) throws Exception {
        return getPasswordCredential(credentials);
    }

    @Override
    public boolean supportLocation() {
        return true;
    }

    @Override
    public boolean supportUserInUri() {
        return false;
    }

    @Override
    public boolean supportsCredentials() {
        return true;
    }

    @Override
    public boolean supportNonDefaultCredential() {
        return true;
    }

    @Override
    public boolean supportNullCredential() {
        return false;
    }

    @Override
    public boolean supportsClose() {
        return true;
    }

}
