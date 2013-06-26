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

package nl.esciencecenter.octopus.adaptors.local;

import static org.junit.Assert.*;

import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.RelativePath;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class LocalFileAttributesTest {

    @org.junit.Test(expected = NullPointerException.class)
    public void testPathIsNull() throws Exception {
        new LocalFileAttributes(null);
    }
    
    @org.junit.Test(expected = OctopusIOException.class)
    public void testNonExistingFile() throws Exception {
        Octopus o = OctopusFactory.newOctopus(null);
        Files files = o.files();
        FileSystem fs = files.getLocalCWDFileSystem();
        
        AbsolutePath path = fs.getEntryPath().resolve(new RelativePath("noot.txt"));
        new LocalFileAttributes(path);
    }

    @org.junit.Test
    public void testCreationTime() throws Exception {
        Octopus o = OctopusFactory.newOctopus(null);
        Files files = o.files();
        FileSystem fs = files.getLocalCWDFileSystem();
        AbsolutePath path = fs.getEntryPath().resolve(new RelativePath("aap.txt"));
        
        long now = System.currentTimeMillis();
        
        files.createFile(path);
        
        FileAttributes att = new LocalFileAttributes(path);
        
        long time = att.creationTime();
        
        System.out.println("NOW " + now + " CREATE " + time);
        
        files.delete(path);
        OctopusFactory.endOctopus(o);
        
        assertTrue(time >= now - 5000);
        assertTrue(time <= now + 5000);
    }

    @org.junit.Test
    public void testHashCode() throws Exception {
        Octopus o = OctopusFactory.newOctopus(null);
        Files files = o.files();
        FileSystem fs = files.getLocalCWDFileSystem();
        AbsolutePath path = fs.getEntryPath().resolve(new RelativePath("aap.txt"));
        files.createFile(path);
        
        FileAttributes att = new LocalFileAttributes(path);
      
        int hash = att.hashCode();
        
        // TODO: check hashcode ?
        
        files.delete(path);
        OctopusFactory.endOctopus(o);
    }

    @org.junit.Test
    public void testEquals() throws Exception {
        Octopus o = OctopusFactory.newOctopus(null);
        Files files = o.files();
        FileSystem fs = files.getLocalCWDFileSystem();
        AbsolutePath path1 = fs.getEntryPath().resolve(new RelativePath("aap.txt"));
        files.createFile(path1);
        
        AbsolutePath path2 = fs.getEntryPath().resolve(new RelativePath("noot.txt"));
        files.createFile(path2);
        
        files.setPosixFilePermissions(path1, LocalUtils.octopusPermissions(PosixFilePermissions.fromString("rwxr--r--")));
        files.setPosixFilePermissions(path2, LocalUtils.octopusPermissions(PosixFilePermissions.fromString("---r--r--")));
        
        FileAttributes att1 = new LocalFileAttributes(path1);
        FileAttributes att2 = new LocalFileAttributes(path2);
        
        assertTrue(att1.equals(att1));
        assertFalse(att1.equals(null));
        assertFalse(att1.equals("aap"));
        assertFalse(att1.equals(att2));
        
        files.setPosixFilePermissions(path2, LocalUtils.octopusPermissions(PosixFilePermissions.fromString("--xr--r--")));
        att2 = new LocalFileAttributes(path2);
        
        assertFalse(att1.equals(att2));
        
        files.setPosixFilePermissions(path2, LocalUtils.octopusPermissions(PosixFilePermissions.fromString("r-xr--r--")));
        att2 = new LocalFileAttributes(path2);
        
        assertFalse(att1.equals(att2));
       
        files.setPosixFilePermissions(path2, LocalUtils.octopusPermissions(PosixFilePermissions.fromString("rwxr--r--")));
        att2 = new LocalFileAttributes(path2);
        
        assertTrue(att1.equals(att2));

        files.delete(path1);
        files.delete(path2);
        
        OctopusFactory.endOctopus(o);
    }

    
    
    
}
