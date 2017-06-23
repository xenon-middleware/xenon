/**
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
package nl.esciencecenter.xenon.adaptors.file.sftp;

import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.FileTestConfig;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;

/**
 *
 */
public class SftpFileTestConfig extends FileTestConfig {
    private final String username;
    private final char[] passwd;

    private final static String scheme = "sftp";
    private final String correctLocation;
    private final String wrongLocation;
    private final String correctLocationWrongUser;

    public SftpFileTestConfig(String configfile) throws Exception {
        super("sftp", configfile);

        String location = getPropertyOrFail("test.ssh.location");

        username = getPropertyOrFail("test.ssh.user");
        passwd = getPropertyOrFail("test.ssh.password").toCharArray();

        correctLocation = /*username + "@" + */ location;
        wrongLocation = /*username + "@*/ "doesnotexist.com";
        correctLocationWrongUser = /*"incorrect@" +*/ location;
    }

    @Override
    public boolean supportLocation() {
        return true;
    }

    @Override
    public boolean supportsAppending() {
        return true;
    }

    @Override
    public boolean supportsResuming() {
        return true;
    }

    @Override
    public boolean supportsAsynchronousCopy() {
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
    public Credential getDefaultCredential() throws XenonException {
        return new DefaultCredential(username);
    }

    @Override
    public Credential getPasswordCredential() throws XenonException {
        return new PasswordCredential(username, passwd);
    }

    @Override
    public Credential getInvalidCredential() throws XenonException {
        return new PasswordCredential(username, "wrongpassword".toCharArray());
    }

    @Override
    public boolean supportNonDefaultCredential() {
        return true;
    }

    @Override
    public Credential getNonDefaultCredential() throws XenonException {
        return getPasswordCredential();
    }

    @Override
    public boolean supportNullCredential() {
        return false;
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
    public FileSystem getTestFileSystem(Files files) throws XenonException {
        return files.newFileSystem("sftp", correctLocation, getDefaultCredential(), null);
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
    public Path getWorkingDir(Files files) throws XenonException {
        return files.newFileSystem("sftp", correctLocation, getDefaultCredential(), null).getEntryPath();
    }
}
