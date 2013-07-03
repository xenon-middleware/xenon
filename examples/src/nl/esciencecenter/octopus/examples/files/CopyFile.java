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
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.CopyOption;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.RelativePath;
import nl.esciencecenter.octopus.util.URIUtils;

/**
 * A simple example of how to copy a file. 
 * 
 * This example assumes the user provides the source URI and target URI command line. The target file must not exists yet!
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class CopyFile {

    private static AbsolutePath createPath(Files files, FileSystem fs, URI file) throws OctopusIOException, OctopusException {  

        String filepath = file.getPath();
        
        if (filepath.startsWith("//")) { 
            // Path is absolute from file system root
            return files.newPath(fs, new RelativePath(filepath));
        } else { 
            // Path is relative to entry point 
            return fs.getEntryPath().resolve(new RelativePath(filepath));
        }
    }
    
    public static void main(String [] args) { 
        
        if (args.length != 2) { 
            System.out.println("Example requires an source and target URI a parameters!");
            System.exit(1);
        }
                
        try {
            // We first turn the user provided argument into a URI.
            URI source = new URI(args[0]);
            URI target = new URI(args[1]);
            
            // We create a new octopus using the OctopusFactory (without providing any properties).
            Octopus octopus = OctopusFactory.newOctopus(null);

            // Next, we retrieve the Files and Credentials interfaces
            Files files = octopus.files();
            
            // Next we create a FileSystem 
            FileSystem sourceFS = files.newFileSystem(URIUtils.getFileSystemURI(source), null, null);
            FileSystem targetFS = files.newFileSystem(URIUtils.getFileSystemURI(target), null, null);
            
            // We now create an AbsolutePath representing both files.
            AbsolutePath sourcePath = createPath(files, sourceFS, source);
            AbsolutePath targetPath = createPath(files, targetFS, target);

            // Copy the file. The CREATE options ensures the target does not exist yet. 
            files.copy(sourcePath, targetPath, CopyOption.CREATE);
                
            // If we are done we need to close the FileSystems
            files.close(sourceFS);
            files.close(targetFS);
            
            // Finally, we end octopus to release all resources 
            OctopusFactory.endOctopus(octopus);

        } catch (Exception e) { 
            System.out.println("CreatingFileSystem example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
