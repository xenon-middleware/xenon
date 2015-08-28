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

package nl.esciencecenter.xenon.adaptors.gftp;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import nl.esciencecenter.xenon.adaptors.FileTestConfig;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;

public class GftpFileTestConfig extends FileTestConfig {

    private String scheme = GftpUtil.GFTP_SCHEME;

    private String correctLocation;
    private String wrongLocation;
    private String userCertFile;
    private String userKeyFile;
    private String proxyFile;
    private char[] passphrase = null;

    private GlobusProxyCredential tempProxy = null;

    public GftpFileTestConfig(String configfile) throws Exception {

        super(GftpUtil.GFTP_SCHEME, configfile);

        correctLocation = getPropertyOrFail(p, "test.gftp.location");

        // Either test proxy file must be defined or userkey file + passphrase !
        userCertFile = p.getProperty("test.gftp.usercert");
        userKeyFile = p.getProperty("test.gftp.userkey");
        proxyFile = p.getProperty("test.gftp.proxyfile");

        String str = p.getProperty("test.gftp.passphrase");
        if (str != null) {
            passphrase = str.toCharArray();
        }
        wrongLocation = "doesnotexist.nodomain";

        debugPrintf(" - correctLocation  =%s\n", correctLocation);
        debugPrintf(" - usercert file    =%s\n", userCertFile);
        debugPrintf(" - userKeyFile file =%s\n", userKeyFile);
        debugPrintf(" - proxyFile file   =%s\n", proxyFile);
        debugPrintf(" - passphrase file  =%s\n", (passphrase != null) ? "<Not Null" : "<NULL>");

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
    public Credential getDefaultCredential(Credentials credentials) throws Exception {

        debugPrintf("creds=" + credentials);

        if (tempProxy == null) {
            initTempProxy(credentials);
        }

        return tempProxy;
    }

    private void initTempProxy(Credentials credentials) throws Exception {

        if (proxyFile != null) {
            //            try {
            //                // check proxy file:
            //                GlobusProxyCredential proxy = credentials.loadProxy(proxyFile);
            //                if (proxy.isValid()) {
            //                    tempProxy = proxy;
            //                }
            //            } catch (Exception e) {
            //                e.printStackTrace();
            //            }
        }

        // Create new Proxy using test account: 

        Map<String, String> props = new Hashtable<String, String>();

        props.put(GlobusProxyCredentials.PROPERTY_USER_CERT_FILE, userCertFile);
        props.put(GlobusProxyCredentials.PROPERTY_USER_KEY_FILE, userKeyFile);

        Credential cred = credentials.newCertificateCredential(scheme, null, null, passphrase, props);

        if (cred instanceof GlobusProxyCredential) {
            tempProxy = (GlobusProxyCredential) cred;
            debugPrintf("Created new proxy:%s\n", tempProxy);
        } else {
            throw new Exception("Couldn't create/get default Credential. Created Credential is not a GlobusProxyCredential!");
        }

    }

    @Override
    public Credential getPasswordCredential(Credentials credentials) throws Exception {
        return credentials.newPasswordCredential(scheme, null, null, new HashMap<String, String>());
    }

    @Override
    public Credential getInvalidCredential(Credentials credentials) throws Exception {
        return credentials.newPasswordCredential(scheme, null, null, new HashMap<String, String>());
    }

    @Override
    public boolean supportNonDefaultCredential() {
        return false;
    }

    @Override
    public Credential getNonDefaultCredential(Credentials credentials) throws Exception {
        return getPasswordCredential(credentials);
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
    public FileSystem getTestFileSystem(Files files, Credentials credentials) throws Exception {
        return files.newFileSystem(scheme, correctLocation, getDefaultCredential(credentials), null);
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
        return null;
    }

    @Override
    public boolean supportsPosixPermissions() {
        // Depends on actual GridFTP FileSystem but API supports it.
        // Disable testing for now:
        return false;
    }

    @Override
    public boolean supportsSymboliclinks() {
        return false;
    }

    @Override
    public Path getWorkingDir(Files files, Credentials credentials) throws Exception {
        return files.newFileSystem("gftp", correctLocation, getDefaultCredential(credentials), null).getEntryPath();
    }

    public static void debugPrintf(String format, Object... args) {
        System.out.printf("GftpFileTextConfig:" + format, args);
    }

}
