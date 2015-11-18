/**
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

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.NoSuchPathException;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;

/**
 * An example of how to retrieve file attributes.
 * 
 * This example assumes the user provides a URI with a file location on the command line.
 * 
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

            // We create a new Xenon using the XenonFactory (without providing any properties).
            Xenon xenon = XenonFactory.newXenon(null);

            // Next, we retrieve the Files and Credentials interfaces
            Files files = xenon.files();

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

            // Finally, we end Xenon to release all resources 
            XenonFactory.endXenon(xenon);

        } catch  (URISyntaxException | XenonException e) {
            System.out.println("ShowFileAttributes example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
