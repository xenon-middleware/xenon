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
package nl.esciencecenter.xenon.utils;

import java.io.File;

import nl.esciencecenter.xenon.InvalidLocationException;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.filesystems.FileSystem;

public class LocalFileSystemUtils {

	public static String NAME = "LocalFileSystemUtils";
	
	/**
     * Returns if we are currently running on Windows.
     *
     * @return if we are currently running on Window.
     */
    public static boolean isWindows() {
        String os = System.getProperty("os.name");
        return (os != null && os.startsWith("Windows"));
    }
	
	/**
     * Return the locally valid root element of an <code>String</code> representation of an absolute path.
     *
     * Examples of a root elements are "/" or "C:". If the provided path does not contain a locally valid root element, an
     * exception will be thrown. For example, providing "/user/local" will return "/" on Linux or OSX, but throw an exception on
     * Windows; providing "C:\test" will return "C:" on Windows but throw an exception on Linux or OSX.
     *
     * @param p
     *            The absolute path for which to determine the root element.
     * @return The locally valid root element.
     * @throws XenonException
     *             If the provided <code>path</code> is not absolute, or does not contain a locally valid root.
     */
    public static String getLocalRoot(String p) throws XenonException {
        
        String path = p;
        
        if (isWindows()) {
            if (path == null || path.isEmpty()) {
                return "";
            }
            if (!path.contains("/") && !path.contains("\\")) {
                // Windows URS, network drive
                return path;
            }
            if (path.charAt(0) == '/') {
                path = path.substring(1);
            }
            if (path.length() >= 2 && (path.charAt(1) == ':') && Character.isLetter(path.charAt(0))) {
                return path.substring(0, 2).toUpperCase();
            }

            throw new InvalidLocationException(NAME, "Path does not include drive name! " + path);
        }

        if (path == null || path.isEmpty() || (path.length() >= 1 && path.charAt(0) == '/')) {
            return "/";
        }

        throw new InvalidLocationException(NAME, "Path is not absolute! " + path);
    }
	
	 /**
     * Returns all local FileSystems.
     * 
     * This method detects all local file system roots, and returns one or more <code>FileSystems</code> representing each of
     * these roots.
     * 
     * @return all local FileSystems.
     *
     * @throws XenonException
     *             If the creation of the FileSystem failed.
     */
    public static FileSystem[] getLocalFileSystems() throws XenonException {

        File[] roots = File.listRoots();

        FileSystem[] result = new FileSystem[roots.length];

        for (int i = 0; i < result.length; i++) {
            result[i] = FileSystem.create("file", getLocalRoot(roots[i].getPath()));
        }

        return result;
    }
}
