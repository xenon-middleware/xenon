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

import java.net.URI;
import java.nio.file.attribute.FileAttribute;
import java.util.HashSet;
import java.util.Set;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.exceptions.OctopusRuntimeException;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.PosixFilePermission;
import nl.esciencecenter.octopus.files.RelativePath;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 *
 */
public class LocalUtilsTest {

    @org.junit.Test
    public void test_new() throws Exception {
        // Useless operation!
        new LocalUtils();
    }

    @org.junit.Test(expected = OctopusIOException.class)
    public void test_delete_null() throws Exception {
        LocalUtils.delete(null);
    }

    @org.junit.Test(expected = OctopusIOException.class)
    public void test_size_null() throws Exception {
        LocalUtils.size(null);
    }

    @org.junit.Test(expected = OctopusIOException.class)
    public void test_createFile_null() throws Exception {
        LocalUtils.createFile(null);
    }
    
    @org.junit.Test(expected = OctopusIOException.class)
    public void test_move_null() throws Exception {
        LocalUtils.move(null, null);
    }
    
    @org.junit.Test(expected = OctopusIOException.class)
    public void test_setPosixFilePermissions_null() throws Exception {
        LocalUtils.setPosixFilePermissions(null, null);
    }

    @org.junit.Test(expected = OctopusIOException.class)
    public void test_newByteChannel_null() throws Exception { 
        LocalUtils.newByteChannel(null);
    }

    @org.junit.Test(expected = OctopusIOException.class)
    public void test_newInputStream_null() throws Exception { 
        LocalUtils.newInputStream(null);
    }

    @org.junit.Test
    public void test_exists_null() throws Exception { 
        boolean v = LocalUtils.exists(null);
        assert(!v);
    }

    @org.junit.Test(expected = OctopusException.class)
    public void test_broken_home() throws Exception {
        System.setProperty("user.home", "/home/aap");        
        LocalUtils.getHome();
    }
    
    @org.junit.Test(expected = OctopusException.class)
    public void test_broken_cwd() throws Exception {
        System.setProperty("user.dir", "/home/aap/noot");        
        LocalUtils.getCWD();
    }

    @org.junit.Test
    public void test_octopusPermissions_null() throws Exception {
        Set<PosixFilePermission> tmp = LocalUtils.octopusPermissions(null);        
        assert(tmp == null);        
    }

    @org.junit.Test
    public void test_javaPermissions_null() throws Exception {
        Set<java.nio.file.attribute.PosixFilePermission> tmp = LocalUtils.javaPermissions(null);
        
        assert(tmp != null);
        assert(tmp.size() == 0);
    }

    @org.junit.Test
    public void test_exists_home() throws Exception {
        boolean v = LocalUtils.exists("~");
        assert(v);
    }

    @org.junit.Test
    public void test_exists_bashrc() throws Exception {
        // Assumes "aap" does not exist
        boolean v = LocalUtils.exists("~/aap");
        assert(!v);
    }
    
    @org.junit.Test(expected = OctopusException.class)
    public void test_javaPath_home() throws Exception {
        
        Octopus octopus = OctopusFactory.newOctopus(null);
        Files files = octopus.files();
        
        String tmp = LocalUtils.javaPath(files.newPath(files.getLocalCWDFileSystem(), new RelativePath("~"))).toString();
        
        OctopusFactory.endOctopus(octopus);

        assert(tmp.equals(System.getProperty("user.dir")));        
    }

    @org.junit.Test(expected = OctopusException.class)
    public void test_javaPermissionAttribute() throws Exception {
        
        Set<PosixFilePermission> tmp = new HashSet<>();
        tmp.add(PosixFilePermission.OWNER_READ);
        
        FileAttribute<Set<java.nio.file.attribute.PosixFilePermission>> out = LocalUtils.javaPermissionAttribute(tmp);
        
        assert(out.value().contains(java.nio.file.attribute.PosixFilePermission.OWNER_READ));         
    }

    @org.junit.Test(expected = OctopusRuntimeException.class)
    public void test_getURI() throws Exception {
        LocalUtils.getURI("AAP@@:\\");
    }

    @org.junit.Test
    public void test_getURI2() throws Exception {
        URI uri = LocalUtils.getURI(LocalUtils.LOCAL_FILE_URI);
        assert(uri.getScheme().equals("file"));        
    }
    
    @org.junit.Test
    public void test_getURI3() throws Exception {
        URI uri = LocalUtils.getURI(LocalUtils.LOCAL_JOB_URI);
        assert(uri.getScheme().equals("local"));        
    }
    

    
    
    
}
