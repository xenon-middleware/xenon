package nl.esciencecenter.octopus.engine.files;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;

import java.net.URI;
import java.net.URISyntaxException;

import nl.esciencecenter.octopus.engine.Adaptor;
import nl.esciencecenter.octopus.engine.OctopusEngine;
import nl.esciencecenter.octopus.engine.files.FileSystemImplementation;
import nl.esciencecenter.octopus.engine.OctopusProperties;
import nl.esciencecenter.octopus.engine.files.FilesEngine;
import nl.esciencecenter.octopus.engine.files.AbsolutePathImplementation;
import nl.esciencecenter.octopus.exceptions.OctopusException;
import nl.esciencecenter.octopus.exceptions.OctopusIOException;
import nl.esciencecenter.octopus.files.Files;
import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.RelativePath;

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

    public FileSystemImplementation getFileSystem(String adaptor_name, URI root_location) throws URISyntaxException {
        String fs_uid = "1";
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
        FileSystemImplementation filesystem = getFileSystem(adaptor_name, new URI("file:///"));
        AbsolutePathImplementation expected_path = new AbsolutePathImplementation(filesystem, new RelativePath("tmp/bla.txt"));
        // create stubs, so we don't have to use a real adaptor
        // a real adaptor touches filesystem, uses network, requires credentials
        // etc.
        Files files_adaptor = mock(Files.class);
        OctopusEngine octopus = fakeOctopus(files_adaptor, adaptor_name);
        when(files_adaptor.newPath(filesystem, "tmp/bla.txt")).thenReturn(expected_path);

        FilesEngine engine = new FilesEngine(octopus);
        AbsolutePath newpath = engine.newPath(filesystem, "file:///tmp/bla.txt");

        assertThat((AbsolutePathImplementation) newpath, is(expected_path));
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
        FileSystemImplementation source_filesystem = getFileSystem("Local", new URI("file://"));
        AbsolutePath source = new AbsolutePathImplementation(source_filesystem, new RelativePath("tmp/bla.txt"));
        FileSystemImplementation target_filesystem = getFileSystem("Local", new URI("file://"));
        AbsolutePath target = new AbsolutePathImplementation(target_filesystem, new RelativePath("tmp/foo.txt"));

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
        FileSystemImplementation source_filesystem = getFileSystem("ssh", new URI("ssh://localhost"));
        AbsolutePath source = new AbsolutePathImplementation(source_filesystem, new RelativePath("tmp/bar.txt"));
        FileSystemImplementation target_filesystem = getFileSystem("gridftp", new URI("gridftp://somewhere"));
        AbsolutePath target = new AbsolutePathImplementation(target_filesystem, new RelativePath("tmp/foo.txt"));

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

        FileSystemImplementation source_filesystem = getFileSystem("Local", new URI("file://"));
        AbsolutePath source = new AbsolutePathImplementation(source_filesystem, new RelativePath("tmp/bar.txt"));
        FileSystemImplementation target_filesystem = getFileSystem("gridftp", new URI("gridftp://somewhere"));
        AbsolutePath target = new AbsolutePathImplementation(target_filesystem, new RelativePath("tmp/foo.txt"));

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
        FileSystemImplementation source_filesystem = getFileSystem("ssh", new URI("ssh://localhost"));
        AbsolutePath source = new AbsolutePathImplementation(source_filesystem, new RelativePath("tmp/bar.txt"));
        FileSystemImplementation target_filesystem = getFileSystem("Local", new URI("file://"));
        AbsolutePath target = new AbsolutePathImplementation(target_filesystem, new RelativePath("tmp/bar.txt"));

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
        FileSystemImplementation filesystem = getFileSystem("Local", new URI("file://"));
        AbsolutePathImplementation path = new AbsolutePathImplementation(filesystem, new RelativePath("tmp/bla.txt"));
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
