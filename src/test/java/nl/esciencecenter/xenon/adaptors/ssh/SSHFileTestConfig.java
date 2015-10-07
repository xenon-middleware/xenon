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

package nl.esciencecenter.xenon.adaptors.ssh;

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
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class SSHFileTestConfig extends FileTestConfig {
    private final String username;
    private final char[] passwd;

    private final static String scheme = "sftp";
    private final String correctLocation;
    private final String wrongLocation;
    private final String correctLocationWrongUser;

    public SSHFileTestConfig(String configfile) throws Exception {
        super("ssh", configfile);

        String location = getPropertyOrFail(p, "test.ssh.location");

        username = getPropertyOrFail(p, "test.ssh.user");
        passwd = getPropertyOrFail(p, "test.ssh.password").toCharArray();

        correctLocation = username + "@" + location;
        wrongLocation = username + "@doesnotexist.com";
        correctLocationWrongUser = "incorrect@" + location;
    }

    @Override
    public boolean supportLocation() {
        return true;
    }

    @Override
    public boolean supportUserInUri() {
        return true;
    }

    @Override
    public boolean supportsCredentials() {
        return true;
    }

    @Override
    public Credential getDefaultCredential(Credentials credentials) throws XenonException {
        return credentials.getDefaultCredential("ssh");
    }

    @Override
    public Credential getPasswordCredential(Credentials credentials) throws XenonException {
        return credentials.newPasswordCredential("ssh", username, passwd, new HashMap<String, String>(0));
    }

    @Override
    public Credential getInvalidCredential(Credentials credentials) throws XenonException {
        return credentials.newPasswordCredential("ssh", username, "wrongpassword".toCharArray(), new HashMap<String, String>(0));
    }

    @Override
    public boolean supportNonDefaultCredential() {
        return true;
    }

    @Override
    public Credential getNonDefaultCredential(Credentials credentials) throws XenonException {
        return getPasswordCredential(credentials);
    }

    @Override
    public boolean supportNullCredential() {
        return true;
    }

    @Override
    public boolean supportsClose() {
        return true;
    }

    @Override
    public Map<String, String> getDefaultProperties() throws Exception {
        return null;
    }

    @Override
    public FileSystem getTestFileSystem(Files files, Credentials credentials) throws XenonException {
        return files.newFileSystem("sftp", correctLocation, getDefaultCredential(credentials), null);
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
    public boolean supportsPosixPermissions() {
        // Assumes an SSH connection to a posix machine!
        return true;
    }

    @Override
    public boolean supportsSymboliclinks() {
        // Assumes an SSH connection to a posix machine!
        return true;
    }

    @Override
    public Path getWorkingDir(Files files, Credentials credentials) throws XenonException {
        return files.newFileSystem("sftp", correctLocation, getDefaultCredential(credentials), null).getEntryPath();
    }
}
