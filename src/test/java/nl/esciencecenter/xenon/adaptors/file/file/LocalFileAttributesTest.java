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
package nl.esciencecenter.xenon.adaptors.file.file;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.attribute.PosixFilePermissions;

import nl.esciencecenter.xenon.Xenon;
import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.file.file.LocalFileAttributes;
import nl.esciencecenter.xenon.files.FileAttributes;
import nl.esciencecenter.xenon.files.Files;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.util.Utils;

/**
 * 
 */
public class LocalFileAttributesTest {

    private static Path resolve(Files files, Path root, String path) throws XenonException { 
        return files.newPath(root.getFileSystem(), root.getRelativePath().resolve(path));
    }
    
    @org.junit.Test(expected = NullPointerException.class)
    public void testPathIsNull() throws Exception {
        new LocalFileAttributes(null);
    }

    @org.junit.Test(expected = XenonException.class)
    public void testNonExistingFile() throws Exception {
        Files files = Xenon.files();
        Path path = resolve(files, Utils.getLocalCWD(files), "noot" + System.currentTimeMillis() + ".txt");
        new LocalFileAttributes(path);
    }

    @org.junit.Test
    public void testCreationTime() throws Exception {
        Files files = Xenon.files();
        
        long now = System.currentTimeMillis();

        Path path = resolve(files, Utils.getLocalCWD(files), "aap" + now + ".txt");
        
        files.createFile(path);

        FileAttributes att = new LocalFileAttributes(path);

        long time = att.creationTime();

        System.out.println("NOW " + now + " CREATE " + time);

        files.delete(path);
        
        Xenon.endAll();
        
        assertTrue(time >= now - 5000);
        assertTrue(time <= now + 5000);
    }

    @org.junit.Test
    public void testHashCode() throws Exception {
        Files files = Xenon.files();
        Path path = resolve(files, Utils.getLocalCWD(files), "aap.txt");
        
        if (!files.exists(path)) { 
            files.createFile(path);
        }

        FileAttributes att = new LocalFileAttributes(path);

        att.hashCode();

        // TODO: check hashcode ?

        files.delete(path);
        Xenon.endAll();
    }

    @org.junit.Test
    public void testEquals() throws Exception {
        
        if (Utils.isWindows()) { 
            return;
        }
        
        Files files = Xenon.files();
        Path cwd = Utils.getLocalCWD(files);
        Path path1 = resolve(files, cwd, "aap.txt");

        if (files.exists(path1)) { 
            files.delete(path1);
        }

        files.createFile(path1);
        Path path2 = resolve(files, cwd, "noot.txt");
        
        if (files.exists(path2)) { 
            files.delete(path2);
        }

        files.createFile(path2);

        files.setPosixFilePermissions(path1, LocalUtils.xenonPermissions(PosixFilePermissions.fromString("rwxr--r--")));
        files.setPosixFilePermissions(path2, LocalUtils.xenonPermissions(PosixFilePermissions.fromString("---r--r--")));

        FileAttributes att1 = new LocalFileAttributes(path1);
        FileAttributes att2 = new LocalFileAttributes(path2);

        assertTrue(att1.equals(att1));
        assertFalse(att1.equals(null));
        assertFalse(att1.equals("aap"));
        assertFalse(att1.equals(att2));

        files.setPosixFilePermissions(path2, LocalUtils.xenonPermissions(PosixFilePermissions.fromString("--xr--r--")));
        att2 = new LocalFileAttributes(path2);

        assertFalse(att1.equals(att2));

        files.setPosixFilePermissions(path2, LocalUtils.xenonPermissions(PosixFilePermissions.fromString("r-xr--r--")));
        att2 = new LocalFileAttributes(path2);

        assertFalse(att1.equals(att2));

        files.setPosixFilePermissions(path2, LocalUtils.xenonPermissions(PosixFilePermissions.fromString("rwxr--r--")));
                
        att2 = new LocalFileAttributes(path2);

        System.out.println("path1: " + att1);
        System.out.println("path2: " + att2);
        
        assertTrue(att1.equals(att2));

        files.delete(path1);
        files.delete(path2);

        Xenon.endAll();
    }

}
