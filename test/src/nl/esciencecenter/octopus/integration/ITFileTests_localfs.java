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
package nl.esciencecenter.octopus.integration;

import java.net.URI;

import org.junit.Assert;

import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.RelativePath;

public class ITFileTests_localfs extends AbstractFileTests {
    /**
     * Local File Adaptor test location.
     */
    public java.net.URI getTestLocation() throws Exception {

        String tmpdir = System.getProperty("java.io.tmpdir");// "/testLocalAdaptor/";
        return new URI("file", null, tmpdir, null);
    }

    // =====================
    // Local Adaptor tests. 
    // =====================

    @org.junit.Test
    public void testIsLocal() throws Exception {
        // local file 
        FileSystem fs = getFileSystem();
        AbsolutePath cwd = getFiles().newPath(fs, new RelativePath("."));
        Assert.assertTrue("Local Path must return true for isLocal().", cwd.isLocal());

    }

    @Override
    Credential getCredentials() throws OctopusException {
        return null;
    }

}
