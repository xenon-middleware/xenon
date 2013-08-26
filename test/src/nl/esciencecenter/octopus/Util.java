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

package nl.esciencecenter.octopus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.Path;
import nl.esciencecenter.octopus.files.Pathname;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * 
 */
public class Util {

    /**
     * Invoke the private constructor of OctopusEngine. Needed in some tests.
     * 
     * @param properties
     *            the properties to pass to the OctopusEngine.
     * @return a new OctopusEngine.
     * @throws Exception
     *             if the OctopusEngine could not be created or throws an exception.
     */
    public static OctopusEngine createOctopusEngine(HashMap<String, String> properties) throws Exception {
        Constructor<OctopusEngine> constructor = OctopusEngine.class.getDeclaredConstructor(Map.class);
        constructor.setAccessible(true);
        return constructor.newInstance(properties);
    }

    /**
     * Invoke the private end method of OctopusEngine. Needed in some tests.
     * 
     * @param e
     *            the OctopusEngine to end.
     * @throws Exception
     *             if the method could not be invoked.
     */
    public static void endOctopusEngine(OctopusEngine e) throws Exception {
        Method method = OctopusEngine.class.getDeclaredMethod("end");
        method.setAccessible(true);
        method.invoke(e);
    }
    
    public static Path resolve(Files files, Path root, String... path) throws OctopusIOException { 
        return files.newPath(root.getFileSystem(), root.getPathname().resolve(new Pathname(path)));
    }
    
    public static Path resolve(Files files, FileSystem fs, String ... path) throws OctopusIOException {
        return resolve(files, fs.getEntryPath(), path);
    }
    
    public static String readFileToString(File file) throws FileNotFoundException, IOException {
        return readFully(new FileInputStream(file));
    }
    
    public static String readFully(InputStream in) throws IOException {

        byte[] buffer = new byte[1024];

        int offset = 0;

        int tmp = in.read(buffer, 0, buffer.length - offset);

        while (tmp != -1) {

            offset += tmp;

            if (offset == buffer.length) {
                buffer = Arrays.copyOf(buffer, buffer.length * 2);
            }

            tmp = in.read(buffer, offset, buffer.length - offset);
        }

        in.close();
        return new String(buffer, 0, offset);
    }
    
}
