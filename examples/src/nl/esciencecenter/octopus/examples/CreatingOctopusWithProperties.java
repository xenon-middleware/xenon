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

package nl.esciencecenter.octopus.examples;

import java.util.Properties;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.exceptions.OctopusException;

/**
 * A simple example of how to configure an octopus with properties. 
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class CreatingOctopusWithProperties {

    public static void main(String [] args) { 
        try { 
            
            // We create some properties here to configure octopus. In this example 
            // we set the polling delay of the local adaptor to 1000ms. We also set 
            // the strictHostKeyChecking property of the ssh adaptor to true. 
            Properties p = new Properties();
            p.put("octopus.adaptors.local.queue.pollingDelay", "1000");
            p.put("octopus.adaptors.ssh.strictHostKeyChecking", "true");
            
            // We now create a new octopus with the properties using the OctopusFactory.
            Octopus octopus = OctopusFactory.newOctopus(p);
           
            // We can now uses the octopus to get some work done!
            // ....
            
            // Finally, we end octopus to release all resources 
            OctopusFactory.endOctopus(octopus);

        } catch (OctopusException e) { 
            System.out.println("CreatingOctopusWithProperties example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
