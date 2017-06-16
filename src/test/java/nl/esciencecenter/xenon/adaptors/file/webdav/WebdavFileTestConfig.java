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
package nl.esciencecenter.xenon.adaptors.file.webdav;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.FileTestConfig;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.DefaultCredential;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
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

        username = getPropertyOrFail("test.webdav.user");
        password = getPropertyOrFail("test.webdav.password").toCharArray();

        privateLocation = getPropertyOrFail("test.webdav.privatelocation");
        correctLocation = getPropertyOrFail("test.webdav.publiclocation");
        wrongLocation = username + "@doesnotexist71093880.com";
        correctLocationWrongUser = "incorrect@" + getPropertyOrFail("test.webdav.publiclocation");
        correctLocationWithUser = username + "@" + getPropertyOrFail("test.webdav.publiclocation");
    }

    @Override
    public FileSystem getTestFileSystem(Files files) throws XenonException {
        return files.newFileSystem(scheme, correctLocation, getDefaultCredential(), null);
    }

    @Override
    public boolean supportsAppending() {
        return false;
    }

    @Override
    public boolean supportsResuming() {
        return false;
    }

    @Override
    public boolean supportsAsynchronousCopy() {
        return false;
    }

    @Override
    public Path getWorkingDir(Files files) throws XenonException {
        return files.newFileSystem(scheme, correctLocation, getNonDefaultCredential(), null).getEntryPath();
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
    public Credential getDefaultCredential() throws XenonException {
        return new DefaultCredential();
    }

    @Override
    public Map<String, String> getDefaultProperties() throws Exception {
        return null;
    }

    @Override
    public Credential getNonDefaultCredential() throws XenonException {
        return getPasswordCredential();
    }

    @Override
    public Credential getPasswordCredential() throws XenonException {
        return new PasswordCredential(username, password);
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
