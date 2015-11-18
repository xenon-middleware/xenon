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
import nl.esciencecenter.xenon.files.CopyOption;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;

/**
 * An example of how to copy a file.
 * 
 * This example assumes the user provides the source URI and target URI on the command line. The target file must not exists yet!
 * 
 * @version 1.0
 * @since 1.0
 */
public class CopyFile {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Example requires source and target URI as parameters!");
            System.exit(1);
        }

		Xenon xenon = null;
        try {
            // We first turn the user provided arguments into a URI.
            URI source = new URI(args[0]);
            URI target = new URI(args[1]);

            // Next, we create a new Xenon using the XenonFactory (without providing any properties).
            xenon = XenonFactory.newXenon(null);

            // Next, we retrieve the Files and Credentials interfaces
            Files files = xenon.files();

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
        } catch (URISyntaxException | XenonException e) {
            System.out.println("CopyFile example failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
			if (xenon != null) {
				try {
					// Finally, we end Xenon to release all resources
					XenonFactory.endXenon(xenon);
				} catch (XenonException ex) {
					System.exit(1);
				}
			}
		}
    }
}
