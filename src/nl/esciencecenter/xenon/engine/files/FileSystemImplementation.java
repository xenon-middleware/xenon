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
package nl.esciencecenter.xenon.engine.files;

import java.util.Map;

import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.engine.CobaltProperties;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;

public class FileSystemImplementation implements FileSystem {

    private final String adaptorName;
    private final String scheme;
    private final String location;
    private final String uniqueID;

    private final Credential credential;
    private final CobaltProperties properties;
    private final RelativePath entryPath;

    public FileSystemImplementation(String adaptorName, String identifier, String scheme, String location, RelativePath entryPath,
            Credential credential, CobaltProperties properties) {

        if (adaptorName == null) {
            throw new IllegalArgumentException("AdaptorName may not be null!");
        }

        if (identifier == null) {
            throw new IllegalArgumentException("Identifier may not be null!");
        }

        if (scheme == null) {
            throw new IllegalArgumentException("Scheme may not be null!");
        }

        if (location == null) {
            throw new IllegalArgumentException("Location may not be null!");
        }
        
        if (entryPath == null) {
            throw new IllegalArgumentException("EntryPath may not be null!");
        }

        this.adaptorName = adaptorName;
        this.uniqueID = identifier;
        this.scheme = scheme;
        this.location = location;
        this.entryPath = entryPath;
        this.credential = credential;

        if (properties == null) {
            this.properties = new CobaltProperties();
        } else {
            this.properties = properties;
        }
    }

    public Credential getCredential() {
        return credential;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    @Override
    public Path getEntryPath() {
        return new PathImplementation(this, entryPath);
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public String getLocation() {
        return location;
    }
    
    @Override
    public Map<String, String> getProperties() {
        return properties.toMap();
    }

    @Override
    public String getAdaptorName() {
        return adaptorName;
    }

    @Override
    public String toString() {
        return "FileSystemImplementation [adaptorName=" + adaptorName + ", scheme=" + scheme + ", location=" + location 
                + ", entryPath=" + entryPath + ", properties=" + properties + "]";
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
