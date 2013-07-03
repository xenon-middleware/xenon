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
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.util.URIUtils;

/**
 * A simple example of how to list a directory. 
 * 
 * This example assumes the user provides the URI of the directory on the command line. 
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class DirectoryListing {

    public static void main(String [] args) { 
        
        if (args.length != 1) { 
            System.out.println("Example requires an URI a parameter!");
            System.exit(1);
        }
                
        try {
            // We first turn the user provided argument into a URI.
            URI uri = new URI(args[0]);
            
            // Next, extract the parts from the URI we need to access the FileSystem, the file, and the Credential.
            URI fsURI = URIUtils.getFileSystemURI(uri);
            String filepath = uri.getPath();
            
            // We create a new octopus using the OctopusFactory (without providing any properties).
            Octopus octopus = OctopusFactory.newOctopus(null);

            // Next, we retrieve the Files and Credentials interfaces
            Files files = octopus.files();
            
            // Next we create a FileSystem. Note that both credential and properties are null (which means: use default)
            FileSystem fs = files.newFileSystem(fsURI, null, null);
            
            // We now create an AbsolutePath representing the file.
            //
            // Note we can either create a path that is relative to our entry point into the file system 
            // (for example, the current working directory), or relative to the root of the file system.
            //
            // We can tell the difference by checking if the path starts with '//' (absolute) or '/' (relative).   
            
            AbsolutePath path = null;
            
            if (filepath.startsWith("//")) { 
                // Path is absolute from file system root
                path = files.newPath(fs, new RelativePath(filepath));
            } else { 
                // Path is relative to entry point 
                path = fs.getEntryPath().resolve(new RelativePath(filepath));
            }
            
            // Retrieve the attributes of the file
            if (files.isDirectory(path)) { 
                
                System.out.println("Directory " + uri + " exists and contains the following:");
                
                DirectoryStream<AbsolutePath> stream = files.newDirectoryStream(path);
                
                for (AbsolutePath p : stream) { 
                    System.out.println("   " + p.getFileName());
                }
                
            } else { 
                System.out.println("Directory " + uri + " does not exists or is not a directory.");
            }
                
            // If we are done we need to close the FileSystem
            files.close(fs);
            
            // Finally, we end octopus to release all resources 
            OctopusFactory.endOctopus(octopus);

        } catch (Exception e) { 
            System.out.println("CreatingFileSystem example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
