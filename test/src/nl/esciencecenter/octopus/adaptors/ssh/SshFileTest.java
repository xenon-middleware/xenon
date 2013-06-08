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
package nl.esciencecenter.octopus.adaptors.ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import junit.framework.Assert;
import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.PathAttributesPair;
import nl.esciencecenter.octopus.files.RelativePath;

public class SshFileTest {
    
    Octopus octopus;
    Credentials credentials;
    Credential credential;
    
    Files files;
    FileSystem fileSystem;
    
    AbsolutePath root;
    
    private void prepare(String username, boolean useCredential) throws Exception { 
        
        Octopus octopus = OctopusFactory.newOctopus(null);
        files = octopus.files();
        credentials = octopus.credentials();
    
        if (useCredential) { 
            credential = credentials.getDefaultCredential("ssh");
        }
        
        FileSystem fileSystem = files.newFileSystem(new URI("ssh://" + username + "@localhost"), credential, null);

        root = fileSystem.getEntryPath();
    }
    
    private AbsolutePath createLocalTmpDir() throws Exception {

        AbsolutePath tmpDir = files.newPath(files.getLocalHomeFileSystem(), 
                new RelativePath(System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID().toString()));
        
        Assert.assertFalse(files.exists(tmpDir));
        
        files.createDirectory(tmpDir);
        
        return tmpDir;
    }
    
    private void cleanup() throws Exception { 
        octopus.end();
    }
    
    
    @org.junit.Test
    public void testExists() throws Exception {
        
        prepare(System.getProperty("user.name"), true);
        
        Assert.assertTrue(files.exists(root));
        
        cleanup();
    }

    @org.junit.Test
    public void testExistsNoCredential() throws Exception {
        
        prepare(System.getProperty("user.name"), false);
        
        Assert.assertTrue(files.exists(root));
        
        cleanup();
    }

    private void readFully(InputStream in) throws IOException { 
        
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        while (true) {
            String line = br.readLine();
            if (line == null)
                break;
            System.err.println(line);
        }
    }
    
    
    @org.junit.Test
    public void testInputStream() throws Exception {

        prepare(System.getProperty("user.name"), true);

        AbsolutePath path = root.resolve(new RelativePath(".bashrc"));

        Assert.assertTrue(files.exists(path));

        readFully(files.newInputStream(path));
        
        cleanup();
    }
    
    
    @org.junit.Test
    public void testCopyDownload() throws Exception {
        
        prepare(System.getProperty("user.name"), true);

        AbsolutePath source = root.resolve(new RelativePath(".bashrc"));

        Assert.assertTrue(files.exists(source));

        AbsolutePath tmpDir = createLocalTmpDir();
        AbsolutePath target = tmpDir.resolve(new RelativePath("test"));
        
        Assert.assertFalse(files.exists(target));
        
        System.err.println("Absolute src path = " + source.getPath());
        System.err.println("Absolute target path = " + target.getPath());

        octopus.files().copy(source, target);
        
        Assert.assertTrue(files.exists(target));
        Assert.assertTrue(files.size(source) == files.size(target));

        files.delete(target);
        files.delete(tmpDir);
        
        octopus.end();
    }

    @org.junit.Test
    public void testCopyUpload() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);
        Credentials c = octopus.credentials();
        Credential credential = c.getDefaultCredential("ssh");

        String username = System.getProperty("user.name");

        FileSystem localFileSystem = octopus.files().newFileSystem(new URI("file:///"), null, null);
        AbsolutePath src = octopus.files().newPath(localFileSystem, new RelativePath("/home/" + username + "/.bashrc"));
        System.err.println("absolute src path = " + src.getPath());

        FileSystem sshFileSystem = octopus.files().newFileSystem(new URI("ssh://" + username + "@localhost"), credential, null);
        AbsolutePath target = octopus.files().newPath(sshFileSystem, new RelativePath("/tmp/aap"));
        System.err.println("absolute target path = " + target.getPath());

        if (octopus.files().exists(target)) {
            octopus.files().delete(target);
        }

        octopus.files().copy(src, target);

        // FIXME diff files

        octopus.end();
    }

    @org.junit.Test
    public void testLs() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);
        Credentials c = octopus.credentials();
        Credential credential = c.getDefaultCredential("ssh");

        String username = System.getProperty("user.name");

        Properties props = new Properties();
        props.put(" octopus.adaptors.ssh.strictHostKeyChecking", "false");
        
        FileSystem sshFileSystem = octopus.files().newFileSystem(new URI("ssh://" + username + "@localhost"), credential, props);
        AbsolutePath target = octopus.files().newPath(sshFileSystem, new RelativePath("/bin"));
        System.err.println("absolute target path = " + target.getPath());

        DirectoryStream<AbsolutePath> stream = octopus.files().newDirectoryStream(target);

        while (stream.iterator().hasNext()) {
            AbsolutePath path = stream.iterator().next();
            System.err.println(path.getPath());
        }
        stream.close();

        octopus.end();
    }

    @org.junit.Test
    public void testLslong() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);
        Credentials c = octopus.credentials();
        Credential credential = c.getDefaultCredential("ssh");
        
        String username = System.getProperty("user.name");

        Properties props = new Properties();
        props.put(" octopus.adaptors.ssh.strictHostKeyChecking", "false");
        
        FileSystem sshFileSystem = octopus.files().newFileSystem(new URI("ssh://" + username + "@localhost"), credential, props);
        AbsolutePath target = octopus.files().newPath(sshFileSystem, new RelativePath("/bin"));
        System.err.println("absolute target path = " + target.getPath());

        DirectoryStream<PathAttributesPair> stream = octopus.files().newAttributesDirectoryStream(target);

        while (stream.iterator().hasNext()) {
            PathAttributesPair pair =stream.iterator().next(); 
            AbsolutePath path = pair.path();
            FileAttributes attrs = pair.attributes();
            System.err.println(path + " " + attrs + ", mtime = " + new Date(attrs.lastModifiedTime()) + ", atime = " + new Date(attrs.lastAccessTime()));
        }
        stream.close();

        octopus.end();
    }
    // test connection refused
    // test com.jcraft.jsch.JSchException: UnknownHostKey: 
}
