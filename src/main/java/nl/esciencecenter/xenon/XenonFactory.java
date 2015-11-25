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
package nl.esciencecenter.xenon;

import java.util.Map;

import nl.esciencecenter.xenon.engine.XenonEngine;

/**
 * XenonFactory is used to create and end Xenon instances. Make sure to always 
 * end instances when you no longer need them, otherwise they remain allocated.
 * 
 * @version 1.0
 * @since 1.0
 */
public final class XenonFactory {

    /**
     * Constructor of XenonFactory should never be used.
     */
    private XenonFactory() {
        // DO NOT USE
    }

    /**
     * Create a new Xenon instance using the given properties.
     * 
     * The properties provided will be passed to the Xenon instance and its adaptors on creation time. Note that an
     * {@link XenonException} will be thrown if properties contains any unknown keys.
     * 
     * @param properties
     *            (optional) properties used to configure the newly created Xenon instance.
     * 
     * @return a new Xenon instance.
     * 
     * @throws UnknownPropertyException
     *             If an unknown property was passed.
     * @throws InvalidPropertyException
     *             If a known property was passed with an illegal value.
     * @throws XenonException
     *             If the Xenon failed initialize.
     */
    public static Xenon newXenon(Map<String, String> properties) throws XenonException {
        return XenonEngine.newXenon(properties);
    }

    /**
     * Ends a Xenon instance.
     * 
     * Ending a Xenon instance will automatically close local resources, such as <code>Schedulers</code>, 
     * <code>FileSystems</code> and <code>Credentials</code>. In addition, all non online <code>Jobs</code> it has creates will 
     * be killed (for example jobs that run locally).
     * 
     * @param xenon
     *            the Xenon to end.
     * 
     * @throws NoSuchXenonException
     *             If the Xenon was not found
     * @throws XenonException
     *             If the Xenon failed to end.
     */
    public static void endXenon(Xenon xenon) throws XenonException {
        XenonEngine.closeXenon(xenon);
    }

    /**
     * End all Xenon instances created by this factory.
     * 
     * All exceptions throw during endAll are ignored.
     */
    public static void endAll() {
        XenonEngine.endAll();
    }
}
