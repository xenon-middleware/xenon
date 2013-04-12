package nl.esciencecenter.octopus.integration;

import java.net.URI;

import org.junit.Assert;

import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.RelativePath;

public class LocalFileTests extends AbstractFileTests
{
    /**
     * Local File Adaptor test location. 
     */ 
    public java.net.URI getTestLocation() throws Exception {
     
        String tmpdir = System.getProperty("java.io.tmpdir");// "/testLocalAdaptor/";
        return new URI("file",null,tmpdir,null); 
    }
 
    // =====================
    // Local Adaptor tests. 
    // =====================
    
    @org.junit.Test
    public void testIsLocal() throws Exception {
        // local file 
        FileSystem fs = getFileSystem(); 
        AbsolutePath cwd = getFiles().newPath(fs, new RelativePath("."));
        Assert.assertTrue("Local Path must return true for isLocal().",cwd.isLocal());

    }
        
}
