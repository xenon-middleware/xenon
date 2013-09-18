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

package nl.esciencecenter.cobalt.examples;

import nl.esciencecenter.cobalt.Cobalt;
import nl.esciencecenter.cobalt.CobaltException;
import nl.esciencecenter.cobalt.CobaltFactory;
import nl.esciencecenter.cobalt.credentials.Credentials;
import nl.esciencecenter.cobalt.files.Files;
import nl.esciencecenter.cobalt.jobs.Jobs;

/**
 * A simple example of how to create an octopus and how to retrieve the various interfaces.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class CreatingCobalt {

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        try {
            // We create a new Cobalt using the CobaltFactory (without providing any properties).
            Cobalt cobalt = CobaltFactory.newCobalt(null);

            // Next, we retrieve the Files, Jobs and Credentials API
            Files files = cobalt.files();
            Jobs jobs = cobalt.jobs();
            Credentials credentials = cobalt.credentials();

            // We can now uses the interfaces to get some work done!
            // ....

            // Finally, we end Cobalt to release all resources 
            CobaltFactory.endCobalt(cobalt);

        } catch (CobaltException e) {
            System.out.println("CreatingCobalt example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
