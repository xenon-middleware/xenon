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
package nl.esciencecenter.octopus.adaptors.gridengine;

import java.util.Arrays;

import javax.annotation.Generated;

import nl.esciencecenter.octopus.exceptions.OctopusException;

/**
 * Class that holds some info on queues used in Grid Engine.
 * 
 * @author Niels Drost
 * 
 */
class QueueInfo {
    private final String name;
    private final int slots;
    private final String[] parallelEnvironments;

    QueueInfo(String name, int slots, String[] parallelEnvironments) throws OctopusException {
        this.name = name;
        this.slots = slots;
        this.parallelEnvironments = parallelEnvironments.clone();
    }

    @Override
    public String toString() {
        return "QueueInfo [name=" + name + ", slots=" + slots + ", parallelEnvironments=" + Arrays.toString(parallelEnvironments)
                + "]";
    }

    public String getName() {
        return name;
    }

    public int getSlots() {
        return slots;
    }

    public String[] getParallelEnvironments() {
        return parallelEnvironments;
    }

    @Override
    @Generated("Eclipse")
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + Arrays.hashCode(parallelEnvironments);
        result = prime * result + slots;
        return result;
    }

    @Override
    @Generated("Eclipse")
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
        QueueInfo other = (QueueInfo) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (!Arrays.equals(parallelEnvironments, other.parallelEnvironments)) {
            return false;
        }
        if (slots != other.slots) {
            return false;
        }
        return true;
    }
    
    
}