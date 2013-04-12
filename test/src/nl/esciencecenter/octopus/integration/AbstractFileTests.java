package nl.esciencecenter.octopus.integration;

import java.net.URI;
import java.util.Iterator;

import junit.framework.Assert;

import nl.esciencecenter.octopus.Octopus;
import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.DirectoryStream;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.PathAttributesPair;
import nl.esciencecenter.octopus.files.RelativePath;

abstract public class AbstractFileTests {
    
    /** Singleton Engine for the tests */ 
    protected static Octopus octopus=null;
    
    protected static Files getFiles() throws OctopusException {

        // class synchronization:
        synchronized(AbstractFileTests.class) {
            if (octopus==null) {
                octopus=OctopusFactory.newOctopus(null);
            }
            
            return octopus.files(); 
        }
    }

    public static void outPrintf(String format,Object... args) {
        System.out.printf(format,args); 
    }
    
    public static void errorPrintf(String format,Object... args) {
        System.out.printf(format,args); 
    }
    // ========
    // Instance 
    // ======== 
    
    /** The FileSystem to run tests on */ 
    protected FileSystem fileSystem=null;
    
    /**
     * Return test location.
     * Subclasses need to override this. 
     */ 
    abstract public java.net.URI getTestLocation() throws Exception; 
    
    /** 
     * Get actual FileSystem implementation to run test on. 
     * Test this before other tests: 
     */
    protected FileSystem getFileSystem() throws Exception {
        // Use singleton for now, could re-initialize for each test here. 
        synchronized(this) {
            if (fileSystem==null) {
                URI uri=getTestLocation();
                URI fsURI=new URI(uri.getScheme(),uri.getUserInfo(),uri.getHost(), uri.getPort(),"/", null, null);
                fileSystem=getFiles().newFileSystem(fsURI, null,null);
            }
            
            return fileSystem;
        }
    }

    /**
     * Helper method to return current test dir.
     * @throws Exception 
     */ 
    protected AbsolutePath getTestDir() throws Exception {
        FileSystem fs=getFileSystem();
        String testPath=this.getTestLocation().getPath(); 
        return getFiles().newPath(fs, new RelativePath(testPath));
    }

    // =============== 
    // SetUp 
    // ===============
    
    @org.junit.Before
    public void checkTestSetup() throws Exception {
        // Basic Sanity checks of the test environment; 
        // Typically Exceptions should be thrown here if the call fails. 
        URI uri=getTestLocation();
        Assert.assertNotNull("Setup: Can't do tests on a NULL location",uri); 
        Assert.assertNotNull("Setup: The Files() interface is NULL",getFiles()); 
        Assert.assertNotNull("Setup: Actual FileSystem to run tests on is NULL",getFileSystem()); 
    }
        
    // =============== 
    // Actual tests: 
    // ===============
    
    @org.junit.Test
    public void testGetTestDir() throws Exception {
        AbsolutePath path = getTestDir();
        Assert.assertNotNull("TestPath returned NULL",path);
        Assert.assertNotNull("Actual path element of AbsolutePath may not be NULL",path.getPath());
        
        outPrintf("Test location path          =%s\n",path.getPath() ); 
        outPrintf("Test location toString()    =%s\n",path.toString()); 
        outPrintf("Test location getFileName() =%s\n",path.getFileName()); 

        Assert.assertTrue("Root test location must exists (won't create here):"+path.getPath(),getFiles().exists(path));
    }       
       
    /** 
     * Test creation and delete of file in one test. 
     * 
     * @throws Exception
     */
    @org.junit.Test
    public void testCreateDeleteEmptyFile() throws Exception {
        AbsolutePath filePath = getTestDir().resolve(new RelativePath("testFile10000"));
        
        FileSystem fs=getFileSystem(); 
        Files files = getFiles(); 
        
        // Previous test run could have gone wrong. Indicate here that a previous test run failed. 
        boolean preExisting=files.exists(filePath); 
        if (preExisting) {
            try {
                // try to delete first ! 
                files.delete(filePath);
            }
            catch (Exception e) {
                
            }
            Assert.assertFalse("exists(): Can't test createFile is previous test file already exists. File should now be deleted, please run test again.",preExisting); 
        }

        AbsolutePath actualPath=files.createFile(filePath); 
        // enforce ? 
        Assert.assertEquals("createFile(): New path is not equal to given path",filePath, actualPath);       
        boolean exists=files.exists(filePath); 
        Assert.assertTrue("exist(): After createFile() exists() reports false.",exists); 
        
        files.delete(filePath);  
        Assert.assertTrue("delet(): After delete, method exist() return true.",exists); 
    }
    
    @org.junit.Test
    public void testCreateDeleteEmptySubdir() throws Exception {
                 
        FileSystem fs=getFileSystem(); 
        Files files = getFiles(); 
        
        AbsolutePath dirPath = getTestDir().resolve(new RelativePath("testSubdir10001"));
        Assert.assertFalse("Previous test directory already exists. Please clean test location.:"+dirPath.getPath(),files.exists(dirPath)); 
        
        AbsolutePath actualPath=getFiles().createDirectory(dirPath); 
        boolean exists=files.exists(dirPath); 
        Assert.assertTrue("After createDirectory(), method exists() reports false.",exists); 
        
        assertDirIsEmpty(files,dirPath); 
        
        files.delete(dirPath);
        exists=files.exists(dirPath); 
        Assert.assertFalse("After delete() on directory, method exists() reports false.",exists); 
    }

    public void assertDirIsEmpty(Files files, AbsolutePath dirPath) throws Exception {
        DirectoryStream<AbsolutePath> dirStream = files.newDirectoryStream(dirPath);
        Iterator<AbsolutePath> iterator = dirStream.iterator();
        Assert.assertFalse("Iterator from empty directory can not have a 'next'.",iterator.hasNext()); 
    }

    @org.junit.Test
    public void testNewDirectoryStream() throws Exception {
        
        AbsolutePath path = getTestDir();
        DirectoryStream<AbsolutePath> dirStream = getFiles().newDirectoryStream(path);
        Iterator<AbsolutePath> iterator = dirStream.iterator();

        while(iterator.hasNext()) { 
            AbsolutePath pathEl = iterator.next();
            outPrintf(" -(AbsolutePath)path='%s'\n",pathEl.getPath());
        }
    }
    
    @org.junit.Test
    public void testNewAttributesDirectoryStream() throws Exception {
        
        AbsolutePath path = getTestDir();
        DirectoryStream<PathAttributesPair> dirStream = getFiles().newAttributesDirectoryStream(path);
        Iterator<PathAttributesPair> iterator = dirStream.iterator();

        while(iterator.hasNext()) { 
            PathAttributesPair pathEl = iterator.next();
            outPrintf(" -(PathAttributesPair)path='%s'\n",pathEl.path().getPath());
        }
    }
      
    @org.junit.Test
    public void testResolveEntryPath() throws Exception {
    
        FileSystem fs=getFileSystem();     
        
        // just test whether it works: 
        RelativePath relEntryPath = fs.getEntryPath(); 
        Assert.assertNotNull("Relative entry Path may not be null.",relEntryPath); 

        // just test whether it works: 
        AbsolutePath absoluteEntryPath=getFiles().newPath(fs, relEntryPath);
        Assert.assertNotNull("Absolute entry Path may not be null.",absoluteEntryPath); 
    }

    @org.junit.Test
    public void testResolveRootPath() throws Exception {
    
        FileSystem fs=getFileSystem();     
        
        // resolve "/", for current filesystems this must equal to "/".
        AbsolutePath rootPath = getFiles().newPath(fs,new RelativePath("/"));
        Assert.assertEquals("Absolute path of resolved path '/' must equal to '/'.","/",rootPath.getPath()); 
    
    }

}
