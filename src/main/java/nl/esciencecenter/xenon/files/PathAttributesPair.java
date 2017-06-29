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
package nl.esciencecenter.xenon.files;

/**
 * Path with its associated Attributes.
 */
public class PathAttributesPair {

	private final Path path;
    private final FileAttributes attributes;

    public PathAttributesPair(Path path, FileAttributes attributes) {
        this.path = path;
        this.attributes = attributes;
    }
	
    /**
     * Get the Path in this PathAttributesPair.
     * 
     * @return the Path.
     */
    public Path path() {
        return path;
    }

    /**
     * Get the FileAttributes in this PathAttributesPair.
     * 
     * @return the FileAttributes.
     */
    public FileAttributes attributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return "PathAttributesPair [path=" + path + ", attributes=" + attributes + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
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

        PathAttributesPair other = (PathAttributesPair) obj;

        if (attributes == null) {
            if (other.attributes != null) {
                return false;
            }
        } else if (!attributes.equals(other.attributes)) {
            return false;
        }

        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }
    
}
