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

import nl.esciencecenter.xenon.XenonException;
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

    private final String username;
    private final char[] passwd;

    private static final String scheme = "ftp";
    private final String correctLocation;
    private final String wrongLocation;
    private final String correctLocationWrongUser;

    public FTPFileTestConfig(String configfile) throws Exception {

        super(scheme, configfile);

        String location = getPropertyOrFail("test.ftp.location");

        username = getPropertyOrFail("test.ftp.user");
        passwd = getPropertyOrFail("test.ftp.password").toCharArray();

        correctLocation = username + "@" + location;
        wrongLocation = username + "@doesnotexist71093880.com";
        correctLocationWrongUser = "incorrect@" + location;
    }

    @Override
    public FileSystem getTestFileSystem(Files files, Credentials credentials) throws XenonException {
        return files.newFileSystem(scheme, correctLocation, getDefaultCredential(credentials), null);
    }

    @Override
    public Path getWorkingDir(Files files, Credentials credentials) throws XenonException {
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
    public Credential getDefaultCredential(Credentials credentials) throws XenonException {
        return credentials.getDefaultCredential(scheme);
    }

    @Override
    public Map<String, String> getDefaultProperties() throws Exception {
        return null;
    }

    @Override
    public Credential getPasswordCredential(Credentials credentials) throws XenonException {
        return credentials.newPasswordCredential(scheme, username, passwd, new HashMap<String, String>(0));
    }

    @Override
    public Credential getInvalidCredential(Credentials credentials) throws XenonException {
        return credentials.newPasswordCredential(scheme, username, "wrongpassword".toCharArray(), new HashMap<String, String>(0));
    }

    @Override
    public Credential getNonDefaultCredential(Credentials credentials) throws XenonException {
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
