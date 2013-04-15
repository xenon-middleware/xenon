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
package nl.esciencecenter.octopus.files;

import java.net.URI;
import java.util.Properties;

/**
 * FileSystem represent a (possibly remote) file system that can be used to access data.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public interface FileSystem {

    /**
     * Get the name of the adaptor attached to this FileSystem.
     * 
     * @return the name of the adaptor.
     */
    public String getAdaptorName();

    /**
     * Get the URI representing the location of the FileSystem.
     * 
     * @return the location of the FileSystem.
     */
    public URI getUri();

    /**
     * Get the properties used to create this FileSystem.
     * 
     * @return the properties used to create this FileSystem.
     */
    public Properties getProperties();

    /**
     * Get the entry path of this file system.
     * 
     * The entry path is the initial path when the FileSystem is first accessed, for example <code>"/home/username"</code>.
     * 
     * @return the entry path of this file system.
     */
    public RelativePath getEntryPath();
}
