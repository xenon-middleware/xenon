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

package nl.esciencecenter.octopus.adaptors.local;

import java.util.Map;

import nl.esciencecenter.octopus.adaptors.FileTestConfig;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.util.Utils;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class LocalFileTestConfig extends FileTestConfig {

    private final String scheme;
    private final String correctLocation;
    private final String wrongLocation;
    private final boolean supportPosix;
    
    public LocalFileTestConfig() throws Exception {
        super("local");

        scheme = "file";
        
        String os = System.getProperty("os.name");
        
        if (os.startsWith("Windows")) {
            correctLocation = "C:";
            supportPosix = false;
        } else { 
            correctLocation = "/";
            supportPosix = true;
        }
        wrongLocation = "aap";
    }

    @Override
    public FileSystem getTestFileSystem(Files files, Credentials credentials) throws Exception {
        return Utils.getLocalCWD(files).getFileSystem();
    }

    public boolean supportLocation() {
        return true;
    }

    @Override
    public Credential getDefaultCredential(Credentials c) throws Exception {
        return null;
    }

    @Override
    public Map<String, String> getDefaultProperties() throws Exception {
        return null;
    }

    public boolean supportNullCredential() {
        return true;
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
        return null;
    }

    @Override
    public String getCorrectLocationWithWrongUser() throws Exception {
        return null;
    }

    @Override
    public boolean supportsPosixPermissions() {
        return supportPosix;
    }

    @Override
    public boolean supportsSymboliclinks() {
        return supportPosix;
    }

    @Override
    public Path getWorkingDir(Files files, Credentials c) throws Exception {
        return Utils.resolveWithRoot(files, Utils.getLocalCWD(files), "test");
    }
}
