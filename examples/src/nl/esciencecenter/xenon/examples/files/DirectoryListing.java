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

package nl.esciencecenter.xenon.examples.files;

import java.net.URI;
import java.net.URISyntaxException;

import nl.esciencecenter.xenon.Cobalt;
import nl.esciencecenter.xenon.CobaltException;
import nl.esciencecenter.xenon.CobaltFactory;
import nl.esciencecenter.xenon.files.DirectoryStream;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;

/**
 * An example of how to list a directory.
 * 
 * This example assumes the user provides the URI with the location of the directory on the command line.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class DirectoryListing {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Example requires a URI as parameter!");
            System.exit(1);
        }

        try {
            // We first turn the user provided argument into a URI.
            URI uri = new URI(args[0]);

            // We create a new Cobalt using the CobaltFactory (without providing any properties).
            Cobalt cobalt = CobaltFactory.newCobalt(null);

            // Next, we retrieve the Files and Credentials interfaces
            Files files = cobalt.files();

            // Next we create a FileSystem. Note that both credential and properties are null (which means: use default)
            FileSystem fs = files.newFileSystem(uri.getScheme(), uri.getAuthority(), null, null);

            // We now create an Path representing the directory we want to list.
            Path path = files.newPath(fs, new RelativePath(uri.getPath()));

            // Retrieve the attributes of the file.
            FileAttributes att = files.getAttributes(path);

            // Retrieve the attributes of the files in the directory.
            if (att.isDirectory()) {

                System.out.println("Directory " + uri + " exists and contains the following:");

                DirectoryStream<Path> stream = files.newDirectoryStream(path);

                for (Path p : stream) {
                    System.out.println("   " + p.getRelativePath().getFileNameAsString());
                }

            } else {
                System.out.println("Directory " + uri + " does not exists or is not a directory.");
            }

            // If we are done we need to close the FileSystem
            files.close(fs);

            // Finally, we end Cobalt to release all resources 
            CobaltFactory.endCobalt(cobalt);

        } catch (URISyntaxException | CobaltException e) {
            System.out.println("DirectoryListing example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
