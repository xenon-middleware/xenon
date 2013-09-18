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
import java.net.URISyntaxException;

import nl.esciencecenter.cobalt.Cobalt;
import nl.esciencecenter.cobalt.CobaltException;
import nl.esciencecenter.cobalt.CobaltFactory;
import nl.esciencecenter.cobalt.files.FileAttributes;
import nl.esciencecenter.cobalt.files.FileSystem;
import nl.esciencecenter.cobalt.files.Files;
import nl.esciencecenter.cobalt.files.NoSuchPathException;
import nl.esciencecenter.cobalt.files.Path;
import nl.esciencecenter.cobalt.files.RelativePath;

/**
 * An example of how to retrieve file attributes.
 * 
 * This example assumes the user provides a URI with a file location on the command line.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class ShowFileAttributes {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Example requires an URI a parameter!");
            System.exit(1);
        }

        try {
            // We first turn the user provided argument into a URI.
            URI uri = new URI(args[0]);

            // We create a new octopus using the OctopusFactory (without providing any properties).
            Cobalt octopus = CobaltFactory.newOctopus(null);

            // Next, we retrieve the Files and Credentials interfaces
            Files files = octopus.files();

            // Next we create a FileSystem 
            FileSystem fs = files.newFileSystem(uri.getScheme(), uri.getAuthority(), null, null);

            // We now create an Path representing the file.
            Path path = files.newPath(fs, new RelativePath(uri.getPath()));

            try {
                // Retrieve the attributes of the file
                FileAttributes attributes = files.getAttributes(path);

                System.out.println("File " + uri + " exists and has the following attributes:");
                System.out.println("  isDirectory: " + attributes.isDirectory());
                System.out.println("  isSymbolicLink: " + attributes.isSymbolicLink());
                System.out.println("  size: " + attributes.size());
                System.out.println("  owner: " + attributes.owner());
                System.out.println("  group: " + attributes.group());
                System.out.println("  permissions: " + attributes.permissions());

            } catch (NoSuchPathException e) {
                System.out.println("File " + uri + " does not exist!");

            } catch (Exception e) {
                System.out.println("Failed to retrieve attributes of " + uri + " " + e.getMessage());
                e.printStackTrace();
            }

            // If we are done we need to close the FileSystem
            files.close(fs);

            // Finally, we end octopus to release all resources 
            CobaltFactory.endOctopus(octopus);

        } catch  (URISyntaxException | CobaltException e) {
            System.out.println("ShowFileAttributes example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
