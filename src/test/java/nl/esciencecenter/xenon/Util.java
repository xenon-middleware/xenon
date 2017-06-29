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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.xenon.engine.files.FilesEngine;

/**
 * 
 */
public class Util {
    /**
     * Invoke the private constructor of XenonEngine. Needed in some tests.
     * 
     * @param properties
     *            the properties to pass to the XenonEngine.
     * @return a new XenonEngine.
     * @throws Exception
     *             if the XenonEngine could not be created or throws an exception.
     */
	public static FilesEngine createFileEngine() throws Exception {
        return new FilesEngine();
    }

    
    /**
     * Invoke the private end method of XenonEngine. Needed in some tests.
     * 
     * @param e
     *            the XenonEngine to end.
     * @throws Exception
     *             if the method could not be invoked.
     */
    public static void endFilesEngine(FilesEngine e) throws Exception {
    	e.end();
    }

    private Util() {
        // utility class only
    }
}
