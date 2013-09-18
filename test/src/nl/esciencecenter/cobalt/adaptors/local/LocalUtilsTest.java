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

package nl.esciencecenter.cobalt.adaptors.local;

import java.lang.reflect.Constructor;
import java.util.Set;

import nl.esciencecenter.cobalt.Cobalt;
import nl.esciencecenter.cobalt.CobaltException;
import nl.esciencecenter.cobalt.CobaltFactory;
import nl.esciencecenter.cobalt.adaptors.local.LocalUtils;
import nl.esciencecenter.cobalt.files.Files;
import nl.esciencecenter.cobalt.files.Path;
import nl.esciencecenter.cobalt.files.PosixFilePermission;
import nl.esciencecenter.cobalt.files.RelativePath;
import nl.esciencecenter.cobalt.util.Utils;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 */
public class LocalUtilsTest {

    @org.junit.Test
    public void test_new() throws Exception {
        // Test to satisfy coverage.
        Constructor<LocalUtils> constructor = LocalUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @org.junit.Test(expected = CobaltException.class)
    public void test_delete_null() throws Exception {
        LocalUtils.delete(null);
    }

    @org.junit.Test(expected = CobaltException.class)
    public void test_createFile_null() throws Exception {
        LocalUtils.createFile(null);
    }

    @org.junit.Test(expected = CobaltException.class)
    public void test_move_null() throws Exception {
        LocalUtils.move(null, null);
    }

    @org.junit.Test(expected = CobaltException.class)
    public void test_setPosixFilePermissions_null() throws Exception {
        LocalUtils.setPosixFilePermissions(null, null);
    }

    @org.junit.Test(expected = CobaltException.class)
    public void test_newInputStream_null() throws Exception {
        LocalUtils.newInputStream(null);
    }

    @org.junit.Test(expected = CobaltException.class)
    public void test_broken_home_null() throws Exception {
        String originalHome = System.getProperty("user.home");

        System.clearProperty("user.home");

        try {
            Utils.getHome();
        } finally {
            System.setProperty("user.home", originalHome);
        }
    }

    @org.junit.Test(expected = CobaltException.class)
    public void test_broken_cwd_null() throws Exception {
        String originalCWD = System.getProperty("user.dir");

        System.clearProperty("user.dir");

        try {
            Utils.getCWD();
        } finally {
            System.setProperty("user.dir", originalCWD);
        }
    }

    @org.junit.Test(expected = CobaltException.class)
    public void test_broken_home_empty() throws Exception {
        String originalHome = System.getProperty("user.home");

        System.setProperty("user.home", "");

        try {
            Utils.getHome();
        } finally {
            System.setProperty("user.home", originalHome);
        }
    }

    @org.junit.Test(expected = CobaltException.class)
    public void test_broken_cwd_empty() throws Exception {
        String originalCWD = System.getProperty("user.dir");

        System.setProperty("user.dir", "");

        try {
            Utils.getCWD();
        } finally {
            System.setProperty("user.dir", originalCWD);
        }
    }

    
    @org.junit.Test
    public void test_octopusPermissions_null() throws Exception {
        Set<PosixFilePermission> tmp = LocalUtils.cobaltPermissions(null);
        assert (tmp == null);
    }

    @org.junit.Test
    public void test_javaPermissions_null() throws Exception {
        Set<java.nio.file.attribute.PosixFilePermission> tmp = LocalUtils.javaPermissions(null);

        assert (tmp != null);
        assert (tmp.size() == 0);
    }
   
    @org.junit.Test
    public void test_javaPath_home() throws Exception {

        Cobalt octopus = CobaltFactory.newCobalt(null);
        Files files = octopus.files();
        
        Path cwd = Utils.getLocalCWD(files);
        
        String tmp = LocalUtils.javaPath(files.newPath(cwd.getFileSystem(), new RelativePath("~"))).toString();

        CobaltFactory.endCobalt(octopus);

        assert (tmp.equals(System.getProperty("user.dir")));
    }

//    @org.junit.Test
//    public void test_javaPermissionAttribute() throws Exception {
//
//        Set<PosixFilePermission> tmp = new HashSet<>();
//        tmp.add(PosixFilePermission.OWNER_READ);
//
//        FileAttribute<Set<java.nio.file.attribute.PosixFilePermission>> out = LocalUtils.javaPermissionAttribute(tmp);
//
//        assert (out.value().contains(java.nio.file.attribute.PosixFilePermission.OWNER_READ));
//    }
}
