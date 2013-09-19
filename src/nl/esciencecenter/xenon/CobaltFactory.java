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
package nl.esciencecenter.xenon;

import java.util.Map;

import nl.esciencecenter.xenon.engine.CobaltEngine;

/**
 * CobaltFactory is used to create and end Cobalt instances.
 * 
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 */
public final class CobaltFactory {

    /**
     * Constructor of CobaltFactory should never be used.
     */
    private CobaltFactory() {
        // DO NOT USE
    }

    /**
     * Create a new Cobalt using the given properties.
     * 
     * The properties provided will be passed to the Cobalt and its adaptors on creation time. Note that an
     * {@link CobaltException} will be thrown if properties contains any unknown keys.
     * 
     * @param properties
     *            (optional) properties used to configure the newly created Cobalt.
     * 
     * @return a new Cobalt instance.
     * 
     * @throws UnknownPropertyException
     *             If an unknown property was passed.
     * @throws InvalidPropertyException
     *             If a known property was passed with an illegal value.
     * @throws CobaltException
     *             If the Cobalt failed initialize.
     */
    public static Cobalt newCobalt(Map<String, String> properties) throws CobaltException {
        return CobaltEngine.newCobalt(properties);
    }

    /**
     * Ends a Cobalt.
     * 
     * Ending an Cobalt will automatically close local resources, such as <code>Schedulers</code>, <code>FileSystems</code> and 
     * <code>Credentials</code>. In addition, all non online <code>Jobs</code> it has creates will be killed (for example jobs 
     * that run locally).
     * 
     * @param cobalt
     *            the Cobalt to end.
     * 
     * @throws NoSuchCobaltException
     *             If the Cobalt was not found
     * @throws CobaltException
     *             If the Cobalt failed to end.
     */
    public static void endCobalt(Cobalt cobalt) throws CobaltException {
        CobaltEngine.closeCobalt(cobalt);
    }

    /**
     * End all Cobalt instances created by this factory.
     * 
     * All exceptions throw during endAll are ignored.
     */
    public static void endAll() {
        CobaltEngine.endAll();
    }
}
