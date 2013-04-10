package nl.esciencecenter.octopus.tests.engine.files;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;

import java.net.URI;
import java.net.URISyntaxException;

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

    /**
     * Create fake Octopus with one file adaptor.
     *
     * @param files_adaptor
     * @param adaptor_name
     * @param scheme_name
     * @return
     * @throws OctopusException
     */
    public OctopusEngine fakeOctopus(FilesAdaptor files_adaptor, String adaptor_name, String scheme_name) throws OctopusException {
        OctopusEngine octopus = mock(OctopusEngine.class);
        addAdaptor2Octopus(octopus, files_adaptor, adaptor_name, scheme_name);
        return octopus;
    }

    /**
     * Add a fake file adaptor to Octopus instance.
     * @param octopus
     * @param files_adaptor
     * @param adaptor_name
     * @param scheme_name
     * @throws OctopusException
     */
    public void addAdaptor2Octopus(OctopusEngine octopus, FilesAdaptor files_adaptor, String adaptor_name, String scheme_name) throws OctopusException {
        Adaptor adaptor = mock(Adaptor.class);
        when(adaptor.getName()).thenReturn(adaptor_name);
        when(adaptor.filesAdaptor()).thenReturn(files_adaptor);
        when(octopus.getAdaptor(adaptor_name)).thenReturn(adaptor);
        when(octopus.getAdaptorFor(scheme_name)).thenReturn(adaptor);
    }

    public Path fakePath(OctopusEngine octopus, URI uri) throws URISyntaxException {
        return fakePath(octopus, uri, "mock");
    }

    public Path fakePath(OctopusEngine octopus, URI uri, String adaptor_name) {
        Path path = mock(PathImplementation.class);
        when(path.getAdaptorName()).thenReturn(adaptor_name);
        return path;
    }

    @Test
    public void testFilesEngine() throws OctopusException {
        OctopusEngine octopus = mock(OctopusEngine.class);

        FilesEngine engine = new FilesEngine(octopus);

        // TODO verify octopus is in engine
    }

    @Test
    public void testNewPathURI() throws URISyntaxException, OctopusException {
        // create stubs, so we don't have to use a real adaptor
        // a real adaptor touches filesystem, uses network, requires credentials etc.
        FilesAdaptor files_adaptor = mock(FilesAdaptor.class);
        OctopusEngine octopus = fakeOctopus(files_adaptor, "mock", "file");

        URI location = new URI("file:///tmp/bla.txt");
        Path path = fakePath(octopus, location);
        OctopusProperties octopus_properties = new OctopusProperties();
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
    public void testCopy_SameAdaptors_MockedAdaptorCopies() throws OctopusException, URISyntaxException {
        FilesAdaptor files_adaptor = mock(FilesAdaptor.class);
        OctopusEngine octopus = fakeOctopus(files_adaptor, "mock", "file");
        FilesEngine engine = new FilesEngine(octopus);
        Path source = fakePath(octopus, new URI("file:///tmp/bar.txt"));
        when(source.isLocal()).thenReturn(false);
        Path target = fakePath(octopus, new URI("file:///tmp/foo.txt"));
        when(target.isLocal()).thenReturn(false);

        engine.copy(source, target);

        verify(files_adaptor).copy(source, target);
    }

    @Test
    public void testCopy_NonEqualNonLocalAdaptors_Exception() throws URISyntaxException, OctopusException {
        FilesAdaptor source_adaptor = mock(FilesAdaptor.class);
        FilesAdaptor target_adaptor = mock(FilesAdaptor.class);
        OctopusEngine octopus = fakeOctopus(source_adaptor, "assh", "ssh");
        addAdaptor2Octopus(octopus, target_adaptor, "agridftp", "gridftp");

        FilesEngine engine = new FilesEngine(octopus);
        Path source = fakePath(octopus, new URI("ssh://localhost/tmp/bar.txt"), "assh");
        when(source.isLocal()).thenReturn(false);
        Path target = fakePath(octopus, new URI("gridftp://somewhere/tmp/foo.txt"), "agridftp");
        when(target.isLocal()).thenReturn(false);

        try {
            engine.copy(source, target);
            fail("No exception thrown");
        } catch (OctopusException e) {
            assertThat(e.getMessage(), is("cannot do inter-scheme third party copy (yet)"));
        }
    }

    @Test
    public void testCopy_SourceIsLocalAdaptor_TargetAdaptorCopies() throws URISyntaxException, OctopusException {
        FilesAdaptor source_adaptor = mock(FilesAdaptor.class);
        FilesAdaptor target_adaptor = mock(FilesAdaptor.class);
        OctopusEngine octopus = fakeOctopus(source_adaptor, "alocal", "file");
        addAdaptor2Octopus(octopus, target_adaptor, "agridftp", "gridftp");

        FilesEngine engine = new FilesEngine(octopus);
        Path source = fakePath(octopus, new URI("file:///tmp/bar.txt"), "alocal");
        when(source.isLocal()).thenReturn(true);
        Path target = fakePath(octopus, new URI("gridftp://somewhere/tmp/foo.txt"), "agridftp");
        when(target.isLocal()).thenReturn(false);

        engine.copy(source, target);

        verify(target_adaptor).copy(source, target);
    }

    @Test
    public void testCopy_TargetIsLocalAdaptor_SourceAdaptorCopies() throws URISyntaxException, OctopusException {
        FilesAdaptor source_adaptor = mock(FilesAdaptor.class);
        FilesAdaptor target_adaptor = mock(FilesAdaptor.class);
        OctopusEngine octopus = fakeOctopus(source_adaptor, "agridftp", "gridftp");
        addAdaptor2Octopus(octopus, target_adaptor, "alocal", "file");

        FilesEngine engine = new FilesEngine(octopus);
        Path source = fakePath(octopus, new URI("gridftp://somewhere/tmp/foo.txt"), "agridftp");
        when(source.isLocal()).thenReturn(false);
        Path target = fakePath(octopus, new URI("file:///tmp/bar.txt"), "alocal");
        when(target.isLocal()).thenReturn(true);

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
    public void testSetOwner_MockedFilesAdaptor_FilesAdaptorSetOwnerCalled() throws URISyntaxException, OctopusException {
        FilesAdaptor files_adaptor = mock(FilesAdaptor.class);
        OctopusEngine octopus = fakeOctopus(files_adaptor, "mock", "file");

        FilesEngine engine = new FilesEngine(octopus);
        Path path = fakePath(octopus, new URI("file:///tmp/bla.txt"));

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
