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

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;
import nl.esciencecenter.xenon.credentials.Credential;
import nl.esciencecenter.xenon.credentials.Credentials;
import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.util.Utils;

/**
 * A simple example of how to create a local {@link FileSystem}.
 * 
 * This example is hard coded to use the local file system. A more generic example is shown in {@link CreateFileSystem}. 
 * 
 * @version 1.0
 * @since 1.0
 */
public class CreateLocalFileSystem {

    public static void main(String[] args) {
        try {
            // First , we create a new Xenon using the XenonFactory (without providing any properties).
            Xenon xenon = XenonFactory.newXenon(null);

            // Next, we retrieve the Files and Credentials interfaces
            Files files = xenon.files();
            Credentials credentials = xenon.credentials();

            // To create a new FileSystem we need a Credential that enable us to access the location. 
            Credential c = credentials.getDefaultCredential("file");

            // We need to know the OS to determine the root of the file system 
            String root = null;
            
            if (Utils.isWindows()) { 
                root = "C:";
            } else { 
                root = "/";
            }
            
            // Now we can create a FileSystem (we don't provide any properties). 
            FileSystem fs = files.newFileSystem("file", root, c, null);

            // We can now uses the FileSystem to access files!
            // ....

            // If we are done we need to close the FileSystem and the credential
            credentials.close(c);
            files.close(fs);

            // Finally, we end Xenon to release all resources 
            XenonFactory.endXenon(xenon);

        } catch (XenonException e) {
            System.out.println("CreateLocalFileSystem example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
