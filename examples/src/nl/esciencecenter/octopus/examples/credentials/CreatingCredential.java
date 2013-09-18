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

package nl.esciencecenter.octopus.examples.credentials;

import nl.esciencecenter.cobalt.Cobalt;
import nl.esciencecenter.cobalt.CobaltException;
import nl.esciencecenter.cobalt.CobaltFactory;
import nl.esciencecenter.cobalt.credentials.Credential;
import nl.esciencecenter.cobalt.credentials.Credentials;

/**
 * A simple example of how to create credentials.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class CreatingCredential {

    public static void main(String[] args) {
        try {
            // First, we create a new octopus using the OctopusFactory (without providing any properties).
            Cobalt octopus = CobaltFactory.newCobalt(null);

            // Next, we retrieve the Credentials API
            Credentials credentials = octopus.credentials();

            // We can now retrieve the default credential for a certain scheme
            Credential credential1 = credentials.getDefaultCredential("ssh");

            // We can also create other types of credentials
            Credential credential2 = credentials.newPasswordCredential("ssh", "username", "password".toCharArray(), null);

            // Close the credentials once we are done.
            credentials.close(credential1);
            credentials.close(credential2);

            // Finally, we end octopus to release all resources 
            CobaltFactory.endCobalt(octopus);

        } catch (CobaltException e) {
            System.out.println("CreatingCredential example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
