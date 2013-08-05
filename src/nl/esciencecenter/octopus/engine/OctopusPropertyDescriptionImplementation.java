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

package nl.esciencecenter.octopus.engine;

import java.util.HashSet;
import java.util.Set;

import nl.esciencecenter.octopus.OctopusPropertyDescription;

/**
 * OctopusPropertyDescription contains a description of an octopus property.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class OctopusPropertyDescriptionImplementation implements OctopusPropertyDescription {

    private final String name;    
    private final OctopusPropertyDescription.Type type;
    private final Set<OctopusPropertyDescription.Level> levels;
    private final String defaultValue;
    private final String description;
    
    public OctopusPropertyDescriptionImplementation(String name, Type type, Set<Level> levels, String defaultValue, String description) {
        
        if (name == null) { 
            throw new IllegalArgumentException("Name is null!");
        }
        
        this.name = name;
        
        if (type == null) { 
            throw new IllegalArgumentException("Type is null!");
        }
        
        this.type = type;
        
        if (levels == null) { 
            throw new IllegalArgumentException("Levels is null!");
        }
        
        if (levels.size() == 0) { 
            throw new IllegalArgumentException("No level specified!");
        }
        
        this.levels = new HashSet<Level>(levels);
        
        this.defaultValue = defaultValue;
        
        if (description == null) { 
            throw new IllegalArgumentException("Description is null!");
        }
        
        this.description = description;
    }
  
    public String getName() { 
        return name;
    }
    
    public Type getType() { 
        return type;
    }
    
    public Set<Level> getLevels() { 
        return levels;
    }

    public String getDefaultValue() { 
        return defaultValue;
    }
    
    public String getDescription() { 
        return description;
    }

    @Override
    public String toString() {
        return "OctopusPropertyDescriptionImplementation [name=" + name + ", type=" + type + ", levels=" + levels
                + ", defaultValue=" + defaultValue + ", description=" + description + "]";
    }
}