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

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.util.Utils;

/**
 * An example of how to check if a local file exists.
 * 
 * This example is hard coded to use the local file system. A more generic example is shown in {@link FileExists}. 
 * 
 * This example assumes the user provides a path to check.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class LocalFileExists {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Example required an absolute file path as a parameter!");
            System.exit(1);
        }

        // This should be a valid local path!
        String filename = args[0];

        try {
            // We create a new octopus using the OctopusFactory (without providing any properties).
            Octopus octopus = OctopusFactory.newOctopus(null);

            // Next, we retrieve the Files interfaces
            Files files = octopus.files();

            // We now create an Path representing the local file
            Path path = Utils.fromLocalPath(files, filename);

            // Check if the file exists 
            if (files.exists(path)) {
                System.out.println("File " + filename + " exists!");
            } else {
                System.out.println("File " + filename + " does not exist!");
            }

            // If we are done we need to close the FileSystem ad the credential
            files.close(path.getFileSystem());

            // Finally, we end octopus to release all resources 
            OctopusFactory.endOctopus(octopus);

        } catch (OctopusException | OctopusIOException e) {
            System.out.println("LocalFileExists example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
