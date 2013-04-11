package nl.esciencecenter.octopus.engine.files;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;

import java.net.URI;
import java.net.URISyntaxException;

import nl.esciencecenter.octopus.OctopusProperties;
import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.files.FileSystemImplementation;
import nl.esciencecenter.octopus.engine.files.FilesEngine;
import nl.esciencecenter.octopus.engine.files.PathImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.Path;

import org.junit.Test;

public class FilesEngineTest {

    /**
     * Create fake Octopus with one file adaptor.
     *
     * @param files_adaptor
     * @param adaptor_name
     * @param scheme_name
     * @return
     * @throws OctopusException
     */
    public OctopusEngine fakeOctopus(Files files_adaptor, String adaptor_name) throws OctopusException {
        OctopusEngine octopus = mock(OctopusEngine.class);
        addAdaptor2Octopus(octopus, files_adaptor, adaptor_name);
        return octopus;
    }

    /**
     * Add a fake file adaptor to Octopus instance.
     *
     * @param octopus
     * @param files_adaptor
     * @param adaptor_name
     * @param scheme_name
     * @throws OctopusException
     */
    public void addAdaptor2Octopus(OctopusEngine octopus, Files files_adaptor, String adaptor_name) throws OctopusException {
        Adaptor adaptor = mock(Adaptor.class);
        when(adaptor.getName()).thenReturn(adaptor_name);
        when(adaptor.filesAdaptor()).thenReturn(files_adaptor);
        when(octopus.getAdaptor(adaptor_name)).thenReturn(adaptor);
    }

    public FileSystemImplementation getFileSystem(String adaptor_name) throws URISyntaxException {
        String fs_uid = "1";
        URI root_location = new URI("file:///");
        OctopusProperties oprops = new OctopusProperties();
        FileSystemImplementation filesystem = new FileSystemImplementation(adaptor_name, fs_uid, root_location, oprops);
        return filesystem;
    }

    @Test
    public void testFilesEngine() throws OctopusException {
        OctopusEngine octopus = mock(OctopusEngine.class);

        FilesEngine engine = new FilesEngine(octopus);

        // TODO verify octopus is in engine
    }

    @Test
    public void testNewPath_LocalFileSystem_LocalPath() throws URISyntaxException, OctopusException, OctopusIOException {
        String adaptor_name = "Local";
        FileSystemImplementation filesystem = getFileSystem(adaptor_name);
        PathImplementation expected_path = new PathImplementation(filesystem, "file:///tmp/bla.txt");
        // create stubs, so we don't have to use a real adaptor
        // a real adaptor touches filesystem, uses network, requires credentials
        // etc.
        Files files_adaptor = mock(Files.class);
        OctopusEngine octopus = fakeOctopus(files_adaptor, adaptor_name);
        when(files_adaptor.newPath(filesystem, "file:///tmp/bla.txt")).thenReturn(expected_path);

        FilesEngine engine = new FilesEngine(octopus);
        Path newpath = engine.newPath(filesystem, "file:///tmp/bla.txt");

        assertThat((PathImplementation) newpath, is(expected_path));
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
    public void testCopy_SameAdaptors_MockedAdaptorCopies() throws URISyntaxException, OctopusIOException, OctopusException {
        Files files_adaptor = mock(Files.class);
        OctopusEngine octopus = fakeOctopus(files_adaptor, "Local");
        FilesEngine engine = new FilesEngine(octopus);
        FileSystemImplementation source_filesystem = getFileSystem("Local");
        Path source = new PathImplementation(source_filesystem, "file:///tmp/bla.txt");
        FileSystemImplementation target_filesystem = getFileSystem("Local");
        Path target = new PathImplementation(target_filesystem, "file:///tmp/foo.txt");

        engine.copy(source, target);

        verify(files_adaptor).copy(source, target);
    }

    @Test
    public void testCopy_NonEqualNonLocalAdaptors_Exception() throws URISyntaxException, OctopusException {
        Files source_adaptor = mock(Files.class);
        OctopusEngine octopus = fakeOctopus(source_adaptor, "ssh");
        Files target_adaptor = mock(Files.class);
        addAdaptor2Octopus(octopus, target_adaptor, "gridftp");
        FilesEngine engine = new FilesEngine(octopus);
        FileSystemImplementation source_filesystem = getFileSystem("ssh");
        Path source = new PathImplementation(source_filesystem, "ssh://localhost/tmp/bar.txt");
        FileSystemImplementation target_filesystem = getFileSystem("gridftp");
        Path target = new PathImplementation(target_filesystem, "gridftp://somewhere/tmp/foo.txt");

        try {
            engine.copy(source, target);
            fail("No exception thrown");
        } catch (OctopusIOException e) {
            // TODO should throw exception with no adaptor, but with paths
            assertThat(e.getMessage(), is("cannot do inter-scheme third party copy (yet) adaptor: null"));
        }
    }

    @Test
    public void testCopy_SourceIsLocalAdaptor_TargetAdaptorCopies() throws URISyntaxException, OctopusException,
            OctopusIOException {
        Files source_adaptor = mock(Files.class);
        OctopusEngine octopus = fakeOctopus(source_adaptor, "Local");
        Files target_adaptor = mock(Files.class);
        addAdaptor2Octopus(octopus, target_adaptor, "gridftp");
        FilesEngine engine = new FilesEngine(octopus);
        FileSystemImplementation source_filesystem = getFileSystem("Local");
        Path source = new PathImplementation(source_filesystem, "file:///tmp/bar.txt");
        FileSystemImplementation target_filesystem = getFileSystem("gridftp");
        Path target = new PathImplementation(target_filesystem, "gridftp://somewhere/tmp/foo.txt");

        engine.copy(source, target);

        verify(target_adaptor).copy(source, target);
    }

    @Test
    public void testCopy_TargetIsLocalAdaptor_SourceAdaptorCopies() throws URISyntaxException, OctopusException,
            OctopusIOException {
        Files source_adaptor = mock(Files.class);
        OctopusEngine octopus = fakeOctopus(source_adaptor, "ssh");
        Files target_adaptor = mock(Files.class);
        addAdaptor2Octopus(octopus, target_adaptor, "Local");
        FilesEngine engine = new FilesEngine(octopus);
        FileSystemImplementation source_filesystem = getFileSystem("ssh");
        Path source = new PathImplementation(source_filesystem, "ssh://localhost/tmp/bar.txt");
        FileSystemImplementation target_filesystem = getFileSystem("Local");
        Path target = new PathImplementation(target_filesystem, "file:///tmp/bar.txt");

        engine.copy(source, target);

        verify(source_adaptor).copy(source, target);
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
    public void testSetOwner_MockedFiles_FilesSetOwnerCalled() throws URISyntaxException, OctopusException, OctopusIOException {
        FileSystemImplementation filesystem = getFileSystem("Local");
        PathImplementation path = new PathImplementation(filesystem, "file:///tmp/bla.txt");
        // create stubs, so we don't have to use a real adaptor
        // a real adaptor touches filesystem, uses network, requires credentials
        // etc.
        Files files_adaptor = mock(Files.class);
        OctopusEngine octopus = fakeOctopus(files_adaptor, "Local");

        FilesEngine engine = new FilesEngine(octopus);

        engine.setOwner(path, "someone", "somegroup");

        verify(files_adaptor).setOwner(path, "someone", "somegroup");
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
