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

package nl.esciencecenter.octopus.examples.files;

import java.net.URI;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;

/**
 * A simple example of how to create a filesystem.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class CreateFileSystem {

    public static void main(String [] args) { 
        try { 
            // We create a new octopus using the OctopusFactory (without providing any properties).
            Octopus octopus = OctopusFactory.newOctopus(null);

            // Next, we retrieve the Files and Credentials interfaces
            Files files = octopus.files();
            Credentials credentials = octopus.credentials();
            
            // To create a new FileSystem we need a URI indicating its location.
            URI uri = new URI("file://localhost/");
            
            // We also need a Credential that enable us to access the location. 
            Credential c = credentials.getDefaultCredential("file");  
            
            // Now we can create a FileSystem (we don't provide any properties). 
            FileSystem fs = files.newFileSystem(uri, c, null);
            
            // We can now uses the FileSystem to access files!
            // ....
            
            // If we are done we need to close the FileSystem and the credential
            credentials.close(c);
            files.close(fs);
            
            // Finally, we end octopus to release all resources 
            OctopusFactory.endOctopus(octopus);

        } catch (Exception e) { 
            System.out.println("CreatingFileSystem example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
