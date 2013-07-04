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
package nl.esciencecenter.octopus.engine.files;

import java.net.URI;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.RelativePath;

public class FileSystemImplementation implements FileSystem {

    private final String adaptorName;
    private final String uniqueID;

    private final URI uri;
    private final Credential credential;
    private final OctopusProperties properties;
    private final RelativePath entryPath;

    public FileSystemImplementation(String adaptorName, String identifier, URI uri, RelativePath entryPath,
            Credential credential, OctopusProperties properties) {

        if (adaptorName == null) {
            throw new IllegalArgumentException("AdaptorName may not be null!");
        }

        if (identifier == null) {
            throw new IllegalArgumentException("Identifier may not be null!");
        }

        if (uri == null) {
            throw new IllegalArgumentException("URI may not be null!");
        }

        if (entryPath == null) {
            throw new IllegalArgumentException("EntryPath may not be null!");
        }

        this.adaptorName = adaptorName;
        this.uniqueID = identifier;
        this.uri = uri;
        this.entryPath = entryPath;
        this.credential = credential;
        this.properties = properties;
    }

    public Credential getCredential() {
        return credential;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    @Override
    public AbsolutePath getEntryPath() {
        return new AbsolutePathImplementation(this, entryPath);
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public OctopusProperties getProperties() {
        return properties;
    }

    @Override
    public String getAdaptorName() {
        return adaptorName;
    }

    @Override
    public String toString() {
        return "FileSystemImplementation [adaptorName=" + adaptorName + ", uri=" + uri + ", entryPath=" + entryPath
                + ", properties=" + properties + "]";
    }

    @Override
    public int hashCode() {
        int result = 31 + adaptorName.hashCode();
        return 31 * result + uniqueID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        FileSystemImplementation other = (FileSystemImplementation) obj;
        return adaptorName.equals(other.adaptorName) && uniqueID.equals(other.uniqueID);
    }

}
