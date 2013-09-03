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

package nl.esciencecenter.octopus.adaptors;

import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.Path;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public abstract class FileTestConfig extends GenericTestConfig {

    protected FileTestConfig(String adaptorName) {
        super(adaptorName);
    }

    public abstract FileSystem getTestFileSystem(Files files, Credentials credentials) throws Exception;

    public abstract Path getWorkingDir(Files files, Credentials credentials) throws Exception;
    
    public abstract boolean supportsPosixPermissions();

    public abstract boolean supportsSymboliclinks();
    
    public boolean supportsClose() {
        return false;
    }

    public boolean supportsLocalCWD() {
        return false;
    }

    public boolean supportsLocalHome() {
        return false;
    }



}
