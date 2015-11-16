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

package nl.esciencecenter.xenon.adaptors.ssh;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import nl.esciencecenter.xenon.files.FileSystem;
import nl.esciencecenter.xenon.files.Path;
import nl.esciencecenter.xenon.files.RelativePath;

import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.SftpATTRS;

/**
 * @author Jason Maassen <J.Maassen@esciencecenter.nl>
 * @version 1.0
 * @since 1.0
 *
 */
@SuppressWarnings("OctalInteger")
public class SSHFileAttributeTest {
    
    // Copied from SftpATTRS, needed to set correct flags.
    public static final int S_IFMT=0xf000;
    public static final int S_IFIFO=0x1000;
    public static final int S_IFCHR=0x2000;
    public static final int S_IFDIR=0x4000;
    public static final int S_IFBLK=0x6000;
    public static final int S_IFREG=0x8000;
    public static final int S_IFLNK=0xa000;
    public static final int S_IFSOCK=0xc000;
    
    public static final int S_ISUID = 04000; // set user ID on execution
    public static final int S_ISGID = 02000; // set group ID on execution
    public static final int S_ISVTX = 01000; // sticky bit   ****** NOT DOCUMENTED *****

    public static final int S_IRUSR = 00400; // read by owner
    public static final int S_IWUSR = 00200; // write by owner
    public static final int S_IXUSR = 00100; // execute/search by owner
    public static final int S_IREAD = 00400; // read by owner
    public static final int S_IWRITE= 00200; // write by owner
    public static final int S_IEXEC = 00100; // execute/search by owner

    public static final int S_IRGRP = 00040; // read by group
    public static final int S_IWGRP = 00020; // write by group
    public static final int S_IXGRP = 00010; // execute/search by group

    public static final int S_IROTH = 00004; // read by others
    public static final int S_IWOTH = 00002; // write by others
    public static final int S_IXOTH = 00001; // execute/search by others

    private SftpATTRS createSftpATTRS(Buffer buf) throws Exception {        
        Method method = SftpATTRS.class.getDeclaredMethod("getATTR", Buffer.class);
        method.setAccessible(true);
        return (SftpATTRS) method.invoke(null, buf);
    }

    private SftpATTRS createSftpATTRS(int type) throws Exception {        
        Buffer buf = new Buffer();
        buf.putInt(SftpATTRS.SSH_FILEXFER_ATTR_PERMISSIONS);
        buf.putInt(type);
        return createSftpATTRS(buf);
    }
    
    static class DummyPath implements Path {

        private final RelativePath rpath;
        
        public DummyPath(String path) { 
            this.rpath = new RelativePath(path);
        }

        public DummyPath() { 
            this("bla");
        }
        
        @Override
        public FileSystem getFileSystem() {
            return null;
        }

        @Override
        public RelativePath getRelativePath() {
            return rpath;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((rpath == null) ? 0 : rpath.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            DummyPath other = (DummyPath) obj;
            if (rpath == null) {
                if (other.rpath != null)
                    return false;
            } else if (!rpath.equals(other.rpath))
                return false;
            return true;
        }
    }

    @org.junit.Test
    public void testDir() throws Exception {
        SftpATTRS s1 = createSftpATTRS(S_IFDIR);
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertTrue(tmp.isDirectory());
    }
    
    @org.junit.Test
    public void testReg() throws Exception {
        SftpATTRS s1 = createSftpATTRS(S_IFREG);
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertTrue(tmp.isRegularFile());
    }
    
    @org.junit.Test
    public void testLink() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFLNK);
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertTrue(tmp.isSymbolicLink());
    }

    @org.junit.Test
    public void testOther1() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFBLK);        
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertTrue(tmp.isOther());
    }

    @org.junit.Test
    public void testOther2() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFCHR);        
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertTrue(tmp.isOther());
    }
    
    @org.junit.Test
    public void testOther3() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFIFO);        
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertTrue(tmp.isOther());
    }
    
    @org.junit.Test
    public void testOther4() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFSOCK);        
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertTrue(tmp.isOther());
    }
    
    @org.junit.Test
    public void testSize1() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFREG);        
        s1.setSIZE(42);
        
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertEquals(tmp.size(), 42);
    }
    
    @org.junit.Test
    public void testSize2() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFIFO);        
        s1.setSIZE(42);
        
        // Size should return 0 for non-regular files!
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertEquals(tmp.size(), 0);
    }

    @org.junit.Test
    public void testCreationTime() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFREG);        
        s1.setACMODTIME(22, 42);
        
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertEquals(tmp.creationTime(), 42*1000);
    }

    @org.junit.Test
    public void testModifiedTime() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFREG);        
        s1.setACMODTIME(22, 42);
        
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertEquals(tmp.lastModifiedTime(), 42*1000);
    }
    
    @org.junit.Test
    public void testLastAccessTime() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFREG);        
        s1.setACMODTIME(22, 42);
        
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertEquals(tmp.lastAccessTime(), 22*1000);
    }

    @org.junit.Test
    public void testGroup() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFREG);        
        s1.setUIDGID(42, 22);
        
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertEquals(tmp.group(), "" + 22);
    }

    @org.junit.Test
    public void testOwner() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFREG);        
        s1.setUIDGID(42, 22);
        
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertEquals(tmp.owner(), "" + 42);
    }

    @org.junit.Test
    public void testExecutable1() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFREG);        
        s1.setPERMISSIONS(S_IEXEC);
        
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertTrue(tmp.isExecutable());
    }

    @org.junit.Test
    public void testExecutable2() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFREG);        
        
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertFalse(tmp.isExecutable());
    }

    @org.junit.Test
    public void testReadable1() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFREG);        
        s1.setPERMISSIONS(S_IREAD);
        
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertTrue(tmp.isReadable());
    }

    @org.junit.Test
    public void testReadable2() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFREG);        
        
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertFalse(tmp.isReadable());
    }

    
    @org.junit.Test
    public void testWriteable1() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFREG);        
        s1.setPERMISSIONS(S_IWRITE);
        
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertTrue(tmp.isWritable());
    }

    @org.junit.Test
    public void testWriteable2() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFREG);        
        
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath());        
        assertFalse(tmp.isWritable());
    }

    @org.junit.Test
    public void testHidden1() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFREG);        
        
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath(".bla"));
        
        RelativePath r = new RelativePath(".bla"); 
        RelativePath r2 = new RelativePath(".bla"); 
        
        System.err.println(r.startsWith(r));
        System.err.println(r.startsWith(r2));
        System.err.println(r2.startsWith(r));
        
        RelativePath r3 = new RelativePath(".");
        System.err.println(r.startsWith(r3));
        
        assertTrue(tmp.isHidden());       
    }
    
    @org.junit.Test
    public void testHidden2() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFREG);        
        
        SshFileAttributes tmp = new SshFileAttributes(s1, new DummyPath("bla"));        
        assertFalse(tmp.isHidden());
    }

    @org.junit.Test
    public void testHashcode1() throws Exception {       
        SshFileAttributes tmp = new SshFileAttributes(null, null);       

        final int prime = 31;
        int result = 1;
        //noinspection PointlessArithmeticExpression
        result = prime * result + 0;
        //noinspection PointlessArithmeticExpression
        result = prime * result + 0;
        
        assertEquals(tmp.hashCode(), result);
    }

    @org.junit.Test
    public void testHashcode2() throws Exception {
        SftpATTRS s = createSftpATTRS(S_IFREG);
        DummyPath p = new DummyPath("bla");
        
        SshFileAttributes tmp = new SshFileAttributes(s, p);
        
        final int prime = 31;
        int result = 1;
        result = prime * result + s.hashCode();
        result = prime * result + p.hashCode();
        
        assertEquals(tmp.hashCode(), result);
    }
    
    @org.junit.Test
    public void testEquals1() throws Exception {       
        SshFileAttributes tmp1 = new SshFileAttributes(null, null);       
        SshFileAttributes tmp2 = new SshFileAttributes(null, null);       
        assertTrue(tmp1.equals(tmp2));       
    }
    
    @org.junit.Test
    public void testEquals2() throws Exception {       
        SshFileAttributes tmp1 = new SshFileAttributes(null, null);       
        assertFalse(tmp1.equals(null));       
    }
 
    @org.junit.Test
    public void testEquals3() throws Exception {       
        SshFileAttributes tmp1 = new SshFileAttributes(null, null);       
        assertFalse(tmp1.equals("hello"));       
    }

    @org.junit.Test
    public void testEquals4() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFREG);
        SshFileAttributes tmp1 = new SshFileAttributes(s1, new DummyPath());       
        
        SftpATTRS s2 = createSftpATTRS(S_IFREG);
        SshFileAttributes tmp2 = new SshFileAttributes(s2, new DummyPath());
        
        assertTrue(tmp1.equals(tmp2));       
    }

    @org.junit.Test
    public void testEquals5() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFDIR);
        SshFileAttributes tmp1 = new SshFileAttributes(s1, new DummyPath());       
        
        SftpATTRS s2 = createSftpATTRS(S_IFREG);
        SshFileAttributes tmp2 = new SshFileAttributes(s2, new DummyPath());
        
        assertFalse(tmp1.equals(tmp2));       
    }

    
    @org.junit.Test
    public void testEquals6() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFREG);
        SshFileAttributes tmp1 = new SshFileAttributes(s1, new DummyPath("bla"));       
        
        SftpATTRS s2 = createSftpATTRS(S_IFREG);
        SshFileAttributes tmp2 = new SshFileAttributes(s2, new DummyPath("aap"));
        
        assertFalse(tmp1.equals(tmp2));       
    }

    @org.junit.Test
    public void testEquals7() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFREG);
        SshFileAttributes tmp1 = new SshFileAttributes(s1, new DummyPath("bla"));       
        
        SftpATTRS s2 = createSftpATTRS(S_IFREG);
        SshFileAttributes tmp2 = new SshFileAttributes(s2, null);
        
        assertFalse(tmp1.equals(tmp2));       
    }
    
    @org.junit.Test
    public void testToString() throws Exception {       
        SftpATTRS s1 = createSftpATTRS(S_IFREG);
        SshFileAttributes tmp1 = new SshFileAttributes(s1, null);       
        
        assertEquals(tmp1.toString(), s1.toString());       
    }
    
}


