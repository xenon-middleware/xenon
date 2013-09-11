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

import nl.esciencecenter.octopus.files.Copy;
import nl.esciencecenter.octopus.files.Path;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class CopyImplementation implements Copy {

    private final String adaptorName;
    private final String uniqueID;
    private final Path source;
    private final Path target;

    public CopyImplementation(String adaptorName, String uniqueID, Path source, Path target) {
        super();
        this.adaptorName = adaptorName;
        this.uniqueID = uniqueID;
        this.source = source;
        this.target = target;
    }

    public String getAdaptorName() {
        return adaptorName;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    @Override
    public Path getSource() {
        return source;
    }

    @Override
    public Path getTarget() {
        return target;
    }
    
    public boolean hasID(String copyID) { 
        return uniqueID.equals(copyID);
    }
}
