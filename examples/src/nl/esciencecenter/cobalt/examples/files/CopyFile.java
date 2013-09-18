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

package nl.esciencecenter.cobalt.examples.files;

import java.net.URI;
import java.net.URISyntaxException;

import nl.esciencecenter.cobalt.Cobalt;
import nl.esciencecenter.cobalt.CobaltException;
import nl.esciencecenter.cobalt.CobaltFactory;
import nl.esciencecenter.cobalt.files.CopyOption;
import nl.esciencecenter.cobalt.files.FileSystem;
import nl.esciencecenter.cobalt.files.Files;
import nl.esciencecenter.cobalt.files.Path;
import nl.esciencecenter.cobalt.files.RelativePath;

/**
 * An example of how to copy a file.
 * 
 * This example assumes the user provides the source URI and target URI on the command line. The target file must not exists yet!
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class CopyFile {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Example requires source and target URI as parameters!");
            System.exit(1);
        }

        try {
            // We first turn the user provided arguments into a URI.
            URI source = new URI(args[0]);
            URI target = new URI(args[1]);

            // Next, we create a new octopus using the OctopusFactory (without providing any properties).
            Cobalt octopus = CobaltFactory.newCobalt(null);

            // Next, we retrieve the Files and Credentials interfaces
            Files files = octopus.files();

            // Next we create a FileSystem 
            FileSystem sourceFS = files.newFileSystem(source.getScheme(), source.getAuthority(), null, null);
            FileSystem targetFS = files.newFileSystem(target.getScheme(), target.getAuthority(), null, null);

            // We now create an Path representing both files.
            Path sourcePath = files.newPath(sourceFS, new RelativePath(source.getPath()));
            Path targetPath = files.newPath(targetFS, new RelativePath(target.getPath()));

            // Copy the file. The CREATE options ensures the target does not exist yet. 
            files.copy(sourcePath, targetPath, CopyOption.CREATE);

            // If we are done we need to close the FileSystems
            files.close(sourceFS);
            files.close(targetFS);

            // Finally, we end octopus to release all resources 
            CobaltFactory.endCobalt(octopus);

        } catch (URISyntaxException | CobaltException e) {
            System.out.println("CopyFile example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
