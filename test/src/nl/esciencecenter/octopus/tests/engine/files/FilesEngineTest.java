package nl.esciencecenter.octopus.tests.engine.files;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import nl.esciencecenter.octopus.OctopusFactory;
import nl.esciencecenter.octopus.OctopusProperties;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.files.FilesAdaptor;
import nl.esciencecenter.octopus.engine.files.FilesEngine;
import nl.esciencecenter.octopus.engine.files.PathImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.files.Path;

import org.junit.Test;

public class FilesEngineTest {

    @Test
    public void testFilesEngine() throws OctopusException {
        OctopusEngine octopus = mock(OctopusEngine.class);

        FilesEngine engine = new FilesEngine(octopus);

        // TODO verify octopus is in engine
    }

    @Test
    public void testNewPathURI() throws URISyntaxException, OctopusException {
        URI location = new URI("file:///tmp/bla.txt");
        // create stubs, so we don't have to use a real adaptor
        // a real adaptor touches filesystem, uses network, requires credentials etc.
        OctopusEngine octopus = mock(OctopusEngine.class);
        OctopusProperties octopus_properties = new OctopusProperties();
        Path path = new PathImplementation(octopus_properties, location, "mock", octopus);
        Adaptor adaptor = mock(Adaptor.class);
        FilesAdaptor files_adaptor = mock(FilesAdaptor.class);
        when(adaptor.filesAdaptor()).thenReturn(files_adaptor);
        when(octopus.getAdaptorFor("file")).thenReturn(adaptor);
        when(octopus.getCombinedProperties(null)).thenReturn(octopus_properties);
        when(files_adaptor.newPath(octopus_properties, location)).thenReturn(path);

        FilesEngine engine = new FilesEngine(octopus);
        Path newpath = engine.newPath(location);

        assertThat(newpath, is(path));
    }

    @Test
    public void testIsDirectory() {
        fail("Not yet implemented");
    }

    @Test
    public void testNewByteChannelPathOpenOptionArray() {
        fail("Not yet implemented");
    }

    @Test
    public void testNewDirectoryStreamPath() {
        fail("Not yet implemented");
    }

    @Test
    public void testNewAttributesDirectoryStreamPath() {
        fail("Not yet implemented");
    }

    @Test
    public void testNewPathPropertiesURI() {
        fail("Not yet implemented");
    }

    @Test
    public void testCopy() {
        fail("Not yet implemented");
    }

    @Test
    public void testCreateDirectoriesPathSetOfPosixFilePermission() {
        fail("Not yet implemented");
    }

    @Test
    public void testCreateDirectoriesPath() {
        fail("Not yet implemented");
    }

    @Test
    public void testCreateDirectoryPathSetOfPosixFilePermission() {
        fail("Not yet implemented");
    }

    @Test
    public void testCreateDirectoryPath() {
        fail("Not yet implemented");
    }

    @Test
    public void testCreateFile() {
        fail("Not yet implemented");
    }

    @Test
    public void testCreateSymbolicLink() {
        fail("Not yet implemented");
    }

    @Test
    public void testDelete() {
        fail("Not yet implemented");
    }

    @Test
    public void testDeleteIfExists() {
        fail("Not yet implemented");
    }

    @Test
    public void testExists() {
        fail("Not yet implemented");
    }

    @Test
    public void testMove() {
        fail("Not yet implemented");
    }

    @Test
    public void testNewDirectoryStreamPathFilter() {
        fail("Not yet implemented");
    }

    @Test
    public void testNewAttributesDirectoryStreamPathFilter() {
        fail("Not yet implemented");
    }

    @Test
    public void testNewInputStream() {
        fail("Not yet implemented");
    }

    @Test
    public void testNewOutputStream() {
        fail("Not yet implemented");
    }

    @Test
    public void testNewByteChannelPathSetOfPosixFilePermissionOpenOptionArray() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetAttributes() {
        fail("Not yet implemented");
    }

    @Test
    public void testReadSymbolicLink() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetOwner() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetPosixFilePermissions() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetFileTimes() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetAcl() {
        fail("Not yet implemented");
    }

}
