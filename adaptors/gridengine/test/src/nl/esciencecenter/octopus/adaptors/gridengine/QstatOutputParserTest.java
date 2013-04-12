package nl.esciencecenter.octopus.adaptors.gridengine;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;

import org.junit.Test;
import org.w3c.dom.Document;

public class QstatOutputParserTest {
    
    @Test
    public void testQstatOutputParser() throws OctopusIOException {
        new QstatOutputParser(false);
    }

    @Test
    public void testCheckVersion() throws Throwable {
        File testFile = new File("adaptors/gridengine/test/fixtures/jobs.xml");
        
        QstatOutputParser parser = new QstatOutputParser(false);
        
        //calls checkversion
        parser.getDocument(testFile);   
    }
    
    @Test(expected=IncompatibleServerException.class)
    public void testCheckVersion_NoSchema_Exception() throws Throwable {
        File testFile = new File("adaptors/gridengine/test/fixtures/jobs-no-schema.xml");
        
        QstatOutputParser parser = new QstatOutputParser(false);
        
        parser.getDocument(testFile);
    }
  
    @Test(expected=IncompatibleServerException.class)
    public void testCheckVersion_WrongSchema_Exception() throws Throwable {
        File testFile = new File("adaptors/gridengine/test/fixtures/jobs-wrong-schema.xml");
        
        QstatOutputParser parser = new QstatOutputParser(false);
        
        parser.getDocument(testFile);
    }
    
    @Test(expected=OctopusIOException.class)
    public void testCheckVersion_EmptyFile_Exception() throws Throwable {
        File testFile = new File("adaptors/gridengine/test/fixtures/jobs-empty.xml");
        
        QstatOutputParser parser = new QstatOutputParser(false);
        
        parser.getDocument(testFile);
    }

    @Test
    public void testGetDocument_OctopusAbsolutePath() throws OctopusException {
        fail("Not yet implemented");
    }

    @Test
    public void testGetDocument_File() throws IncompatibleServerException, OctopusIOException {
        File testFile = new File("adaptors/gridengine/test/fixtures/jobs.xml");
        
        QstatOutputParser parser = new QstatOutputParser(false);
        
        parser.getDocument(testFile);   
    }

    @Test
    public void testGetQueues() throws Exception {
        File testFile = new File("adaptors/gridengine/test/fixtures/queues.xml");
        
        QstatOutputParser parser = new QstatOutputParser(false);
        
        Document document = parser.getDocument(testFile);
        
        String[] result = parser.getQueues(document);
        
        Arrays.sort(result);
     
        assertArrayEquals(new Object[] {"all.q", "das3.q", "disabled.q", "fat.q", "gpu.q"} , result);
    }

    @Test
    public void testGetQueueInfo() throws IncompatibleServerException, OctopusIOException {
        File testFile = new File("adaptors/gridengine/test/fixtures/queues.xml");
        
        QstatOutputParser parser = new QstatOutputParser(false);
        
        Document document = parser.getDocument(testFile);
        
        Map<String, Map<String, String>> result = parser.getQueueInfos(document);
        
        assertEquals(result.size(), 5);
    }

}
