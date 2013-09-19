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

package nl.esciencecenter.xenon.examples;

import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.XenonFactory;

/**
 * A simple example of how to configure an Cobalt with properties.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public class CreatingXenonWithProperties {

    public static void main(String[] args) {
        try {

            // We create some properties here to configure Cobalt. In this example 
            // we set the polling delay of the local adaptor to 1000 ms. We also set 
            // the strictHostKeyChecking property of the ssh adaptor to true. 
            Map<String, String> p = new HashMap<>();
            p.put("cobalt.adaptors.local.queue.pollingDelay", "1000");
            p.put("cobalt.adaptors.ssh.loadKnownHosts", "true");

            // We now create a new Cobalt with the properties using the CobaltFactory.
            Xenon xenon = XenonFactory.newXenon(p);

            // We can now uses the Cobalt to get some work done!
            // ....

            // Finally, we end Cobalt to release all resources 
            XenonFactory.endXenon(xenon);

        } catch (XenonException e) {
            System.out.println("CreatingCobaltWithProperties example failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
