package nl.esciencecenter.octopus.engine.files;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import nl.esciencecenter.octopus.files.AbsolutePath;
import nl.esciencecenter.octopus.files.FileSystem;
import nl.esciencecenter.octopus.files.RelativePath;

import org.junit.Before;
import org.junit.Test;

public class AbsolutePathImplementationTest {
    FileSystem fs;

    @Before
    public void setUp() throws URISyntaxException {
        // reuse same file system for every test
        fs = new FileSystemImplementation("local", "local-fs-0", new URI("file:///"), new RelativePath("/"), null, null);
    }

    @Test
    public void testGetParent_Root_Null() {
        AbsolutePathImplementation path = new AbsolutePathImplementation(fs, new RelativePath("/"));

        AbsolutePath parentPath = path.getParent();

        assertNull(parentPath);
    }

}
