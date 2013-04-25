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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Date;
import java.util.Properties;

import junit.framework.Assert;
import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.credentials.Credential;
import nl.esciencecenter.octopus.credentials.Credentials;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileAttributes;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.PathAttributesPair;
import nl.esciencecenter.octopus.files.RelativePath;

public class SshFileTests {

    @org.junit.Test
    public void testExists() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);

        Credentials c = octopus.credentials();

        String username = System.getProperty("user.name");

        Credential credential =
                c.newCertificateCredential("ssh", null, "/home/" + username + "/.ssh/id_rsa", "/home/" + username
                        + "/.ssh/id_rsa.pub", username, "");

        FileSystem fileSystem = octopus.files().newFileSystem(new URI("ssh://" + username + "@localhost"), credential, null);

        AbsolutePath path = octopus.files().newPath(fileSystem, new RelativePath(System.getProperty("java.io.tmpdir")));

        System.err.println("path = " + path.getPath());

        Assert.assertTrue(octopus.files().exists(path));

        octopus.end();
    }

    @org.junit.Test
    public void testInputStream() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);
        Credentials c = octopus.credentials();
        String username = System.getProperty("user.name");
        Credential credential =
                c.newCertificateCredential("ssh", null, "/home/" + username + "/.ssh/id_rsa", "/home/" + username
                        + "/.ssh/id_rsa.pub", username, "");

        FileSystem fileSystem = octopus.files().newFileSystem(new URI("ssh://" + username + "@localhost"), credential, null);

        AbsolutePath path = octopus.files().newPath(fileSystem, new RelativePath("/home/" + username + "/.bashrc"));

        System.err.println("absolute path = " + path);

        InputStream in = octopus.files().newInputStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        while (true) {
            String line = br.readLine();
            if (line == null)
                break;
            System.err.println(line);
        }

        octopus.end();
    }

    @org.junit.Test
    public void testCopyDownload() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);
        Credentials c = octopus.credentials();
        String username = System.getProperty("user.name");
        Credential credential =
                c.newCertificateCredential("ssh", null, "/home/" + username + "/.ssh/id_rsa", "/home/" + username
                        + "/.ssh/id_rsa.pub", username, "");

        FileSystem sshFileSystem = octopus.files().newFileSystem(new URI("ssh://" + username + "@localhost"), credential, null);
        AbsolutePath src = octopus.files().newPath(sshFileSystem, new RelativePath("/home/" + username + "/.bashrc"));
        System.err.println("absolute src path = " + src.getPath());

        FileSystem localFileSystem = octopus.files().newFileSystem(new URI("file:///"), null, null);
        AbsolutePath target = octopus.files().newPath(localFileSystem, new RelativePath("/tmp/aap"));
        System.err.println("absolute target path = " + target.getPath());

        if (octopus.files().exists(target)) {
            octopus.files().delete(target);
        }

        octopus.files().copy(src, target);

        octopus.end();
    }

    @org.junit.Test
    public void testCopyUpload() throws Exception {
        Octopus octopus = OctopusFactory.newOctopus(null);
        Credentials c = octopus.credentials();
        String username = System.getProperty("user.name");
        Credential credential =
                c.newCertificateCredential("ssh", null, "/home/" + username + "/.ssh/id_rsa", "/home/" + username
                        + "/.ssh/id_rsa.pub", username, "");

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
        String username = System.getProperty("user.name");
        Credential credential =
                c.newCertificateCredential("ssh", null, "/home/" + username + "/.ssh/id_rsa", "/home/" + username
                        + "/.ssh/id_rsa.pub", username, "");

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
        String username = System.getProperty("user.name");
        Credential credential =
                c.newCertificateCredential("ssh", null, "/home/" + username + "/.ssh/id_rsa", "/home/" + username
                        + "/.ssh/id_rsa.pub", username, "");

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
